package org.rapidpm.generator;

import org.jboss.resteasy.plugins.providers.atom.*;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Alex Bischof on 12.05.14.
 */
public class FeedGenerator {
    private final static String URI = "http://rapidpm.github.io";
    private Set<String> globalAuthors = new HashSet<>();
    private List<Entry> globalEntries = new ArrayList<>();
    private Map<String, List<Entry>> tagEntryMap = new HashMap<>();

    public Feed buildGlobalFeed() {
        Feed feed = new Feed();
        try {
            feed.setId(new java.net.URI(URI));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        feed.setTitle("RapidPM");
        feed.setUpdated(new Date());

        //adds entries
        globalEntries.forEach(e -> {
            feed.getEntries().add(e);
        });

        //Adds all authors as globals
        feed.getAuthors().addAll(globalAuthors.stream().map(Person::new).collect(Collectors.toList()));

        //Adds all categories
        feed.getCategories().addAll(tagEntryMap.keySet().stream().map(mapCategoryStrings()).collect(Collectors.toList()));
        return feed;
    }

    public List<Feed> buildTagFeeds() throws UnsupportedEncodingException {

        List<Feed> tagFeeds = new ArrayList<>();

        Set<Map.Entry<String, List<Entry>>> entries = tagEntryMap.entrySet();
        for (Map.Entry<String, List<Entry>> entry : entries) {

            String tagName = entry.getKey();
            List<Entry> tagEntries = entry.getValue();

            //Create Feed
            Feed feed = new Feed();
            tagFeeds.add(feed);
            try {
                feed.setId(new java.net.URI(URI));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            tagName = new String(tagName.getBytes("UTF-8"), "UTF-8");
            feed.setTitle(tagName);
            feed.setUpdated(new Date());

            //adds entries
            feed.getEntries().addAll(tagEntries);

            //Adds all authors
            feed.getAuthors().addAll(tagEntries.stream()
                    .map(e -> e.getAuthors())
                    .flatMap(list -> list.stream())
                    .map(p -> p.getName())
                    .distinct()
                    .map(p -> new Person(p))
                    .collect(Collectors.toList()));

            //Adds all categories
            Category cat = new Category();
            cat.setLabel(tagName);
            feed.getCategories().add(cat);
        }
        return tagFeeds;
    }

    public void addEntry(String title, String author, String description, List<String> tags, Date blogDate, String blogarticleStr) {

        //creates entry
        Entry entry = new Entry();
        List<Category> categories = tags.stream().filter(e -> !e.isEmpty()).map(mapCategoryStrings()).collect(Collectors.toList());
        entry.getCategories().addAll(categories);
        entry.setTitle(title);
        entry.setPublished(blogDate);
        Person authorPerson = new Person(author);
        entry.getAuthors().add(authorPerson);
        entry.setSummary(description);

        //Creates content for entry
        Content content = new Content();
        content.setType(MediaType.TEXT_HTML_TYPE);
        content.setText(blogarticleStr);
        entry.setContent(content);

        //add entry to every tag
        categories.stream().map(Category::getLabel)
                .filter(e -> !e.isEmpty())
                .forEach(categoryLabel -> {
                    List<Entry> entries = tagEntryMap.get(categoryLabel);

                    //lazy create
                    if (entries == null) {
                        entries = new ArrayList<>();
                        tagEntryMap.put(categoryLabel, entries);
                    }
                    entries.add(entry);
                });

        //adds global entries
        globalEntries.add(entry);

        //add global authors
        globalAuthors.add(authorPerson.getName());
    }

    private static Function<? super String, ? extends Category> mapCategoryStrings() {
        return e -> {
            Category category = new Category();
            category.setLabel(e);
            return category;
        };
    }
}
