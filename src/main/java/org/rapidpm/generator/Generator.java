package org.rapidpm.generator;

import org.jboss.resteasy.plugins.providers.atom.Feed;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Sven Ruppert on 19.04.2014.
 */
public class Generator {

    public static final String BLOGARTICLE = "blogarticle";
    public static final String PROPERTIES = "properties";
    private static final int NUMBER_OF_BLOGS_ON_FIRST_PAGE = 3;

    public static void main(String[] args) throws IOException {
        LimitedQueue<String> lastNBlogArticles = new LimitedQueue<>(NUMBER_OF_BLOGS_ON_FIRST_PAGE);

        File entriesDir = new File(".");
        File[] years = entriesDir.listFiles();

        FeedGenerator feedGenerator = new FeedGenerator();

//    Archive Liste erzeugen
        List<String> archiveElements = new ArrayList<>();
        if (years != null) {
            for (final File year : years) {
                if (year.isDirectory() && year.getName().startsWith("20")) {
                    File[] months = year.listFiles();
                    if (months != null) {
                        for (final File month : months) {
                            String element = year.getName() + "-" + month.getName();
                            System.out.println("element = " + element);
                            archiveElements.add(element);
                        }
                    }
                }
            }
        }

        Collections.sort(archiveElements, (o1, o2) -> o2.compareTo(o1));
        archiveElements.forEach(System.out::println);

        String archiveStr = "";
        for (final String archiveElement : archiveElements) {
            archiveStr = archiveStr + "<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>" + "\n";
        }

        String index_main = readFile("data/index_main.part", StandardCharsets.UTF_8);
//    String index1 = readFile("data/index1.part", StandardCharsets.UTF_8);
        String index2 = readFile("data/index2.part", StandardCharsets.UTF_8);
        String index3 = readFile("data/index3.part", StandardCharsets.UTF_8);

        //lese alle BlogArtikel
        if (years != null) {
            for (final File year : years) {
                if (year.isDirectory() && year.getName().startsWith("20")) {
                    File[] months = year.listFiles();
                    if (months != null) {
                        for (final File month : months) {
                            if (month.isDirectory()) {

                                List<String> blogarticlesPerMonth = new ArrayList<>();

                                File[] days = month.listFiles();
                                if (days != null) {
                                    for (final File day : days) {
                                        if (day.isDirectory()) {

                                            //erzeuge Liste der blogentries reverse order
                                            List<String> blogarticlesPerDay = new ArrayList<>();

                                            File[] blogsOfTheDay = day.listFiles();
                                            if (blogsOfTheDay != null) {

                                                for (int i = blogsOfTheDay.length - 1; i >= 0; i--) {
                                                    File blogFileDir = blogsOfTheDay[i];
                                                    if (blogFileDir.isDirectory()) {
                                                        for (final File blogarticle : blogFileDir.listFiles((dir, name) -> name.endsWith(BLOGARTICLE))) {
                                                            System.out.println("blogarticle.getName() = " + blogarticle.getName());
                                                            //generiere Blogartikel
                                                            String elementName = blogarticle.getName().replace(BLOGARTICLE, PROPERTIES);
                                                            File rssInfo = new File(blogFileDir, elementName);
                                                            System.out.println("rssInfo.getName() = " + rssInfo.getName());
                                                            Properties prop = new Properties();
                                                            prop.load(new FileInputStream(rssInfo));
                                                            String author = prop.getProperty("author");
                                                            String tags = prop.getProperty("tags");
                                                            String titel = prop.getProperty("titel");
                                                            String description = prop.getProperty("description");

                                                            System.out.println(author);
                                                            System.out.println(tags);
                                                            System.out.println(titel);

                                                            //Collects tags for rss categories
                                                            List<String> categories = Stream.of(tags.split(",")).map(String::trim).collect(Collectors.toList());

                                                            String toLowerCase = titel.toLowerCase();
                                                            String htmlFileName = toLowerCase
                                                                    .replace(" ", "-")
                                                                    .replace("?", "-")
                                                                    .replace("/", "-")
                                                                    .replace("®", "-")
                                                                    .replace("´", "")
                                                                    .replace("<", "-")
                                                                    .replace(">", "-")
                                                                    .replace(",", "-")
                                                                    .replace("--", "-")
                                                                    .replace("--", "-")
                                                                    .replace("--", "-")
//                                  .replace("|", "-")
                                                                    .replace(":", "-");

                                                            if (htmlFileName.contains("spmt")) {
                                                                System.out.println("htmlFileName = " + htmlFileName);
                                                            } else {
                                                            }

                                                            if (htmlFileName.length() > 38) {
                                                                htmlFileName = htmlFileName.substring(0, 38);
                                                            } else {
                                                            }
                                                            htmlFileName = htmlFileName + ".html";


                                                            String blogLink = "/" + year.getName() + "/" + month.getName() + "/" + day.getName() + "/" + htmlFileName;
                                                            String blogDate = year.getName() + "-" + month.getName() + "-" + day.getName();
                                                            String authorLink = "/team/" + author.toLowerCase().replace(" ", "-");

                                                            String header = "<div class=\"blog-post\">" + "\n"
                                                                    + "<h2 class=\"blog-post-title\"><a href=" + blogLink + ">" + titel + "</a></h2>" + "\n"
                                                                    + "<p class=\"blog-post-meta\">" + blogDate + " from <a href=\"" + authorLink + "/\">" + author + "</a></p>" + "\n";

                                                            String blogarticleStr = header + readFile(blogarticle.getPath(), StandardCharsets.UTF_8) + "\n" + "</div>";

                                                            //Creates RSS-Item
                                                            LocalDate blogLocalDate = LocalDate.of(
                                                                    Integer.valueOf(year.getName()), Integer.valueOf(month.getName()), Integer.valueOf(day.getName()));
                                                            Instant instant = blogLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
                                                            feedGenerator.addEntry(titel, author, description, categories, Date.from(instant), blogarticleStr);

                                                            blogarticlesPerDay.add(blogarticleStr); //ab ins daily
                                                            blogarticlesPerMonth.add(blogarticleStr); // ab ins archiv
                                                            lastNBlogArticles.add(blogarticleStr); //ab in Queue f FrontSeite

                                                            FileWriter fw = new FileWriter(new File(day, htmlFileName));

//                              fw.write(index1);
                                                            fw.write(index_main);
                                                            fw.write(blogarticleStr);
                                                            fw.write(index2);
                                                            fw.write(archiveStr);
                                                            fw.write(index3);

                                                            fw.flush();
                                                            fw.close();
                                                        }
                                                    }
                                                }
                                            }
                                            //schreibe day blog File
                                            FileWriter fw = new FileWriter(new File(day, "index.html"));

//                      fw.write(index1);
                                            fw.write(index_main);
                                            for (final String blogarticleStr : blogarticlesPerDay) {
                                                fw.write(blogarticleStr + "\n");
                                            }
                                            fw.write(index2);
                                            fw.write(archiveStr);
                                            fw.write(index3);

                                            fw.flush();
                                            fw.close();

                                        }
                                    }
                                }  //days bearbeitet
                                //Archiv Seite aufbauen
                                FileWriter fw = new FileWriter(new File(month, "index.html"));

//                fw.write(index1);
                                fw.write(index_main);
                                for (final String blogarticleStr : blogarticlesPerMonth) {
                                    fw.write(blogarticleStr + "\n");
                                }
                                fw.write(index2);
                                fw.write(archiveStr);
                                fw.write(index3);

                                fw.flush();
                                fw.close();

                            }
                        }
                    }
                }
            }
        }

        FileWriter fw = new FileWriter(new File("index.html"));

        Collections.reverse(lastNBlogArticles);

        fw.write(index_main);
        for (final String blogarticleStr : lastNBlogArticles) {
            fw.write(blogarticleStr + "\n");
        }
        fw.write(index2);
        for (final String archiveElement : archiveElements) {
            fw.write("<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
        }
        fw.write(index3);

        fw.flush();
        fw.close();

        //conferences / talks
//        fw = new FileWriter(new File("conferences", "index.html"));
//        fw.write(index_main);
//        fw.write(readFile("conferences/blogentry.blogarticle", StandardCharsets.UTF_8));
//        fw.write(index2);
//        for (final String archiveElement : archiveElements) {
//            fw.write("<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
//        }
//        fw.write(index3);
//        fw.flush();
//        fw.close();


        //publications
//        fw = new FileWriter(new File("publications", "index.html"));
//        fw.write(index_main);
//        fw.write(readFile("publications/blogentry.blogarticle", StandardCharsets.UTF_8));
//        fw.write(index2);
//        for (final String archiveElement : archiveElements) {
//            fw.write("<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
//        }
//        fw.write(index3);
//        fw.flush();
//        fw.close();

        //license
//      fw = new FileWriter(new File("releases", "index.html"));
//      fw.write(index_main);
//      fw.write(readFile("releases/blogentry.blogarticle", StandardCharsets.UTF_8));
//      fw.write(index2);
//      for (final String archiveElement : archiveElements) {
//        fw.write("<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
//      }
//      fw.write(index3);
//      fw.flush();
//      fw.close();
//
//        release
      fw = new FileWriter(new File("license", "index.html"));
      fw.write(index_main);
      fw.write(readFile("license/blogentry.blogarticle", StandardCharsets.UTF_8));
      fw.write(index2);
      for (final String archiveElement : archiveElements) {
        fw.write("<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
      }
      fw.write(index3);
      fw.flush();
      fw.close();


        //contacts
      fw = new FileWriter(new File("contact", "index.html"));
      fw.write(index_main);
      fw.write(readFile("contact/blogentry.blogarticle", StandardCharsets.UTF_8));
      fw.write(index2);
      for (final String archiveElement : archiveElements) {
        fw.write("<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
      }
      fw.write(index3);
      fw.flush();
      fw.close();


        //impressum
      fw = new FileWriter(new File("impressum", "index.html"));
      fw.write(index_main);
      fw.write(readFile("impressum/blogentry.blogarticle", StandardCharsets.UTF_8));
      fw.write(index2);
      for (final String archiveElement : archiveElements) {
        fw.write("<li><a href=\"/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
      }
      fw.write(index3);
      fw.flush();
      fw.close();


        //generiere rss feeds auf tag-basis und schreibe Dateien
        buildGlobalFeed(feedGenerator);
        buildTagFeeds(feedGenerator);
    }

    private static void buildTagFeeds(FeedGenerator feedGenerator) {

        String tagfeedsname = "tagfeeds";
        Path tagfeeds = Paths.get(tagfeedsname);

        //Deletes tagfeeds directory
        try {
            deleteTagFeeds(tagfeeds);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //Recreates tagfeeds directory
            Files.createDirectory(tagfeeds);

            List<Feed> feeds = feedGenerator.buildTagFeeds();
            for (Feed feed : feeds) {
                try {

                    FileWriter fileWriter = new FileWriter(tagfeeds.getFileName() + File.separator + feed.getTitle().replaceAll(" ", "_"));
                    JAXBContext jaxbContext = JAXBContext.newInstance(Feed.class);
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

                    // output pretty printed
                    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                    jaxbMarshaller.marshal(feed, fileWriter);
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteTagFeeds(Path tagfeeds) throws IOException {
        if (tagfeeds.toFile().exists()) {
            Files.walkFileTree(tagfeeds, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private static void buildGlobalFeed(FeedGenerator feedGenerator) {
        try {
            File file = new File("rsstest.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Feed.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(feedGenerator.buildGlobalFeed(), file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String readFile(Path path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, encoding);
    }

    public static class LimitedQueue<E> extends LinkedList<E> {
        private int limit;

        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            super.add(o);
            while (size() > limit) {
                super.remove();
            }
            return true;
        }
    }


}
