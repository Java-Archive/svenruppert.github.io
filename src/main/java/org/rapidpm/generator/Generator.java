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
        String index2 = readFile("data/index2.part", StandardCharsets.UTF_8);

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

                                                            String articleHeader = "<article class=\"article clearfix\">\n" +
                                                                    "\t\t\t\t\t\t\t<header class=\"article-header\">\n" +
                                                                    "\t\t\t\t\t\t\t\t<h2 class=\"article-title\"><a href=\"" + blogLink + "\" rel=\"tag\">" + titel + "</a></h2>\n" +
                                                                    "\t\t\t\t\t\t\t\t<p><time datetime=\"" + blogDate + "\">" + blogDate + "</time> <a href=\"" + authorLink +
                                                                    "\" rel=\"author\">from " + author + "</a></p>\n" +
                                                                    "\t\t\t\t\t\t\t</header>\n" +
                                                                    "\t\t\t\t\t\t\t<div class=\"article-content clearfix\">\n" +
                                                                    "\t\t\t\t\t\t\t\t<div class=\"post-thumb\">\n" +
                                                                    "\t\t\t\t\t\t\t\t\t<img src=\"./site/content/post_thumb.jpg\" width=\"\" height=\"\" alt=\"\" />\n" +
                                                                    "\t\t\t\t\t\t\t\t</div>\n" +
                                                                    "\t\t\t\t\t\t\t\t<div class=\"post-excerpt\">";

                                                            String articleFooter = "</div>\n" +
                                                                    "\t\t\t\t\t\t\t</div>\n" +
                                                                    "\t\t\t\t\t\t\t<footer class=\"article-footer clearfix\">\n" +
                                                                    "\t\t\t\t\t\t\t\t<span class=\"post-author\"><a href=\"" + authorLink + "\" rel=\"author\">" + author + "</a>&nbsp;&nbsp;|&nbsp;&nbsp;</span>\n" +
                                                                    "\t\t\t\t\t\t\t\t<span class=\"post-date\"><a href=\"#\" rel=\"date\">" + blogDate + "</a>&nbsp;&nbsp;|&nbsp;&nbsp;</span>\n" +
                                                                    //  "\t\t\t\t\t\t\t\t<span class=\"post-comments-count\"><a href=\"#\">6 Comments</a></span>\n" +
                                                                    "\t\t\t\t\t\t\t</footer>\n" +
                                                                    "\t\t\t\t\t\t</article>";

                                                            String blogarticleStr = articleHeader + readFile(blogarticle.getPath(), StandardCharsets.UTF_8) + articleFooter;


                                                            //Creates RSS-Item
                                                            LocalDate blogLocalDate = LocalDate.of(
                                                                    Integer.valueOf(year.getName()), Integer.valueOf(month.getName()), Integer.valueOf(day.getName()));
                                                            Instant instant = blogLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
                                                            feedGenerator.addEntry(titel, author, description, categories, Date.from(instant), blogarticleStr);

                                                            blogarticlesPerDay.add(blogarticleStr); //ab ins daily
                                                            blogarticlesPerMonth.add(blogarticleStr); // ab ins archiv
                                                            lastNBlogArticles.add(blogarticleStr); //ab in Queue f FrontSeite

                                                            writeContentToHtml(index_main, index2, new File(day, htmlFileName), blogarticleStr);
                                                        }
                                                    }
                                                }
                                            }

                                            //schreibe day blog File
                                            writeContentToHtml(index_main, index2, new File(day, "index.html"), String.join("\n", blogarticlesPerDay));
                                        }
                                    }
                                }  //days bearbeitet

                                //Archiv Seite aufbauen
                                writeContentToHtml(index_main, index2, new File(month, "index.html"), String.join("\n", blogarticlesPerMonth));
                            }
                        }
                    }
                }
            }
        }

        Collections.reverse(lastNBlogArticles);

        //Write main index.html
        writeContentToHtml(index_main, index2, new File("index.html"), String.join("\n", lastNBlogArticles));

        //conferences / talks
        writeContentToHtml(index_main, index2, new File("conferences", "index.html"), readFile("conferences/blogentry.blogarticle", StandardCharsets.UTF_8));

        //publications
        writeContentToHtml(index_main, index2, new File("publications", "index.html"), readFile("publications/blogentry.blogarticle", StandardCharsets.UTF_8));

        //releases
        writeContentToHtml(index_main, index2, new File("releases", "index.html"), readFile("releases/blogentry.blogarticle", StandardCharsets.UTF_8));

//        license
        writeContentToHtml(index_main, index2, new File("license", "index.html"), readFile("license/blogentry.blogarticle", StandardCharsets.UTF_8));

        //contacts
        writeContentToHtml(index_main, index2, new File("contact", "index.html"), readFile("contact/blogentry.blogarticle", StandardCharsets.UTF_8));

        //impressum
        writeContentToHtml(index_main, index2, new File("impressum", "index.html"), readFile("impressum/blogentry.blogarticle", StandardCharsets.UTF_8));



        //Team seite
        //footer fehlt noch ueberall




        //generiere rss feeds auf tag-basis und schreibe Dateien
        buildGlobalFeed(feedGenerator);
        buildTagFeeds(feedGenerator);
    }

    private static void writeContentToHtml(String header, String footer, File file, String content) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(header);
        fw.write(content);
        fw.write(footer);

        fw.flush();
        fw.close();
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
