package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGES = 5;
    private List<Post> postList = new ArrayList<>();
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.ignoreHttpErrors(true).get();
        return document.select(".basic-section--appearance-vacancy-description").text();
    }

    private void addPost(String fullLink) throws IOException {
        Connection connection = Jsoup.connect(fullLink);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Post post = createPost(row);
            postList.add(post);
        });
    }

    private Post createPost(Element row) {
        Post post = new Post();
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        Element dateElement = row.select(".vacancy-card__date").first();
        Element dateTimeElement = dateElement.child(0);
        String vacancyDate = dateTimeElement.attr("datetime");
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description;
        try {
            description = retrieveDescription(link);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        post.setTitle(vacancyName);
        post.setLink(link);
        post.setCreated(dateTimeParser.parse(vacancyDate));
        post.setDescription(description);
        return post;
    }

    @Override
    public List<Post> list(String link) {
        for (int pageNumber = 1; pageNumber <= PAGES; pageNumber++) {
            try {
                addPost(link.concat("%s%d%s".formatted(PREFIX, pageNumber, SUFFIX)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return postList;
    }
}
