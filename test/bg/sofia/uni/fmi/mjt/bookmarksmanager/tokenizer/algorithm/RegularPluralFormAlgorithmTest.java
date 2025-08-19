package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RegularPluralFormAlgorithmTest {
    private final RegularPluralFormAlgorithm algorithm = new RegularPluralFormAlgorithm();

    @Test
    void testNullInput() {
        assertNull(algorithm.stem(null));
    }

    @Test
    void testShortWord() {
        assertEquals("hi", algorithm.stem("hi"));
    }

    @Test
    void testRegularPluralEndingWithS() {
        assertEquals("cat", algorithm.stem("cats"));
        assertEquals("book", algorithm.stem("books"));
    }

    @Test
    void testPluralEndingWithEs() {
        assertEquals("box", algorithm.stem("boxes"));
        assertEquals("match", algorithm.stem("matches"));
    }

    @Test
    void testExceptionEndingsRemainUnchanged() {
        assertEquals("boss", algorithm.stem("boss"));
        assertEquals("this", algorithm.stem("this"));
        assertEquals("virus", algorithm.stem("virus"));
    }

    @Test
    void testWordWithoutPluralEnding() {
        assertEquals("dog", algorithm.stem("dog"));
    }

    @Test
    void testLongerWords() {
        assertEquals("analysis", algorithm.stem("analysis"));
        assertEquals("focus", algorithm.stem("focus"));
    }
}
