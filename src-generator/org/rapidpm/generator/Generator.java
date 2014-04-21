package org.rapidpm.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Sven Ruppert on 19.04.2014.
 */
public class Generator {


  public static final String BLOGARTICLE = "blogarticle";
  public static final String PROPERTIES = "properties";
  private static final int NUMBER_OF_BLOGS_ON_FIRST_PAGE = 3;

  public static void main(String[] args) throws IOException {
    LimitedQueue<String> lastNBlogArticles = new LimitedQueue<>(NUMBER_OF_BLOGS_ON_FIRST_PAGE);

    File entriesDir = new File("entries");
    File[] years = entriesDir.listFiles();

//    Archive Liste erzeugen
    List<String> archiveElements = new ArrayList<>();
    if (years != null) {
      for (final File year : years) {
        if (year.isDirectory()) {
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

    String index1 = readFile("data/index1.part", StandardCharsets.UTF_8);
    String index2 = readFile("data/index2.part", StandardCharsets.UTF_8);
    String index3 = readFile("data/index3.part", StandardCharsets.UTF_8);

    //lese alle BlogArtikel
    if (years != null) {
      for (final File year : years) {
        if (year.isDirectory()) {
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

                              System.out.println(author);
                              System.out.println(tags);
                              System.out.println(titel);

                              String blogarticleStr = readFile(blogarticle.getPath(), StandardCharsets.UTF_8);

                              blogarticlesPerDay.add(blogarticleStr); //ab ins daily
                              blogarticlesPerMonth.add(blogarticleStr); // ab ins archiv
                              lastNBlogArticles.add(blogarticleStr); //ab in Queue f FrontSeite

                              FileWriter fw = new FileWriter(new File(blogFileDir, elementName.replace(PROPERTIES, "html")));

                              fw.write(index1);
                              fw.write(blogarticleStr);
                              fw.write(index2);
                              for (final String archiveElement : archiveElements) {
                                fw.write("<li><a href=\"/entries/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
                              }
                              fw.write(index3);

                              fw.flush();
                              fw.close();
                            }
                          }
                        }
                      }
                      //schreibe day blog File
                      FileWriter fw = new FileWriter(new File(day, "index.html"));

                      fw.write(index1);
                      for (final String blogarticleStr : blogarticlesPerDay) {
                        fw.write(blogarticleStr + "\n");
                      }
                      fw.write(index2);
                      for (final String archiveElement : archiveElements) {
                        fw.write("<li><a href=\"/entries/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
                      }
                      fw.write(index3);

                      fw.flush();
                      fw.close();

                    }
                  }
                }  //days bearbeitet
                //Archiv Seite aufbauen
                FileWriter fw = new FileWriter(new File(month, "index.html"));

                fw.write(index1);
                for (final String blogarticleStr : blogarticlesPerMonth) {
                  fw.write(blogarticleStr + "\n");
                }
                fw.write(index2);
                for (final String archiveElement : archiveElements) {
                  fw.write("<li><a href=\"/entries/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
                }
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

    fw.write(index1);
    for (final String blogarticleStr : lastNBlogArticles) {
      fw.write(blogarticleStr + "\n");
    }
    fw.write(index2);
    for (final String archiveElement : archiveElements) {
      fw.write("<li><a href=\"/entries/" + archiveElement.replace("-", "/") + "\">" + archiveElement + "</a></li>");
    }
    fw.write(index3);

    fw.flush();
    fw.close();
    //generiere rss feeds auf tag-basis
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
      while (size() > limit) { super.remove(); }
      return true;
    }
  }

}
