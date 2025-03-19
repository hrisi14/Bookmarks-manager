package bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.HtmlTokenizer;

import java.io.Serializable;
import java.util.Set;

import static bg.sofia.uni.fmi.mjt.bookmarksmanager.api.ShortenLinkAPIHandler.getShortenedLink;

public record Bookmark (String title, String url,
                       Set<String> keywords, String groupName) implements Serializable {

    public static Bookmark of(String url, String groupName, boolean isShortened) {
       if (url == null || url.isEmpty() || url.isBlank() ||
               groupName == null || groupName.isEmpty() ||
               groupName.isBlank()) {
           throw new IllegalArgumentException("Bookmark and group's " +
                   "name can NOT be null or empty!");
       }
       try {
           if (isShortened) {
               url = getShortenedLink(url);
           }
       } catch (IllegalStateException e) {
           throw new RuntimeException("Could not shorten" +
                   " link due to missing API key or a request perform error!");
       }
       HtmlTokenizer tokenizer = new HtmlTokenizer();
       return new Bookmark(tokenizer.getTitle(url), url, tokenizer.getKeywords(url), groupName);
    }

    @Override
    public String toString() {
        return "Bookmark info: title: " + title + System.lineSeparator() +
                "url: " + url + System.lineSeparator() +
                "keywords: " + keywords+ System.lineSeparator() +
                "groupName: " + groupName + System.lineSeparator();
    }
}
