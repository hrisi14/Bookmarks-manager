package bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.HtmlTokenizer;
import java.util.Set;

import static bg.sofia.uni.fmi.mjt.bookmarksmanager.api.ShortenLinkAPIHandler.getShortenedLink;

public record Bookmark (String title, String url,
                       Set<String> keywords, String groupName) {

    public static Bookmark of(String url, String groupName, boolean isShortened) {
       if (url == null || url.isEmpty() || url.isBlank() ||
               groupName == null || groupName.isEmpty() ||
               groupName.isBlank()) {
           throw new IllegalArgumentException("Bookmark and group's " +
                   "name can NOT be null or empty!");
       }
       if (isShortened) {
           url = getShortenedLink(url);
       }
       HtmlTokenizer tokenizer = new HtmlTokenizer();
       return new Bookmark(tokenizer.getTitle(url), url, tokenizer.getKeywords(url), groupName);
    }
}
