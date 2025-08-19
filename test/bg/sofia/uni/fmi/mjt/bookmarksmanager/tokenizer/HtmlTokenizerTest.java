package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class HtmlTokenizerTest {

    private static final int MAX_KEYWORDS_NUMBER = 15;

    private HtmlTokenizer newTokenizer() {
        return new HtmlTokenizer(Set.of("the", "and", "a", "an", "of", "to", "are", "is", "be"));
    }

    @Test
    void testConstructionWithValidStopWords() {
        HtmlTokenizer tokenizer = new HtmlTokenizer(Set.of("the", "and"));
        assertNotNull(tokenizer);
    }

    @Test
    void testConstructionWithNullStopWordsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HtmlTokenizer((Set<String>) null));
    }

    @Test
    void testConstructionWithEmptyStopWordsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HtmlTokenizer(Set.of()));
    }

    @Test
    void testGetStringStreamFiltersStopWords() {
        HtmlTokenizer tokenizer = newTokenizer();
        String input = "  The   cats, and the dogs!  ";

        List<String> result = tokenizer.getStringStream(input).toList();

        assertFalse(result.contains("the"));
        assertFalse(result.contains("and"));
        assertTrue(result.contains("cat"));
        assertTrue(result.contains("dog"));
    }

    @Test
    void testGetStringStreamWithBlankOrNullThrows() {
        HtmlTokenizer tokenizer = newTokenizer();
        assertThrows(IllegalArgumentException.class, () -> tokenizer.getStringStream(null));
        assertThrows(IllegalArgumentException.class, () -> tokenizer.getStringStream(""));
        assertThrows(IllegalArgumentException.class, () -> tokenizer.getStringStream("   "));
    }

    @Test
    void testTokenizeWithStemmer() {
        HtmlTokenizer tokenizer = newTokenizer();

        String text = "The cats and dogs are running, stopped, quickly; " +
                "classed buzzed filled making riding played.";

        Set<String> keywords = tokenizer.tokenize(text);

        System.out.println(keywords);
        assertTrue(keywords.contains("cat"));
        assertTrue(keywords.contains("dog"));
        assertTrue(keywords.contains("run") || keywords.contains("runn"));
        assertTrue(keywords.contains("stop"));
        assertTrue(keywords.contains("quick"));
        assertTrue(keywords.contains("class"));
        assertTrue(keywords.contains("buzz"));
        assertTrue(keywords.contains("fill"));
        assertTrue(keywords.contains("make"));
        assertTrue(keywords.contains("ride"));
        assertTrue(keywords.contains("play"));
    }

    @Test
    void testTokenizeLimitsToMaxNumberKeywords() {
        HtmlTokenizer tokenizer = newTokenizer();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_KEYWORDS_NUMBER + 5; i++) {
            sb.append("word").append(i).append(' ');
        }
        Set<String> keywords = tokenizer.tokenize(sb.toString());

        assertTrue(keywords.size() <= MAX_KEYWORDS_NUMBER);
    }
}
