package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SuffixStrippingAlgorithmTest {

    private final SuffixStrippingAlgorithm algorithm = new SuffixStrippingAlgorithm();

    @Test
    void testStemWithNullInput() {
        assertNull(algorithm.stem(null));
    }

    @Test
    void testStemWithBlankInput() {
        assertNull(algorithm.stem("   "));
    }

    @Test
    void testStemWithWordEndingWithIng() {
        assertEquals("play", algorithm.stem("playing"));
        assertEquals("run", algorithm.stem("running"));
    }

    @Test
    void testStemWithWordEndingWithEd() {
        assertEquals("walk", algorithm.stem("walked"));
        assertEquals("jump", algorithm.stem("jumped"));
    }

    @Test
    void testStemWithWordEndingWithLy() {
        assertEquals("quick", algorithm.stem("quickly"));
        assertEquals("slow", algorithm.stem("slowly"));
    }

    @Test
    void testStemWithWordWithoutSuffix() {
        assertEquals("book", algorithm.stem("book"));
        assertEquals("cat", algorithm.stem("cat"));
    }

    @Test
    void testStemOfIngWithDoubleConsonant() {
        assertEquals("run",  algorithm.stem("running"));
        assertEquals("stop", algorithm.stem("stopping"));
    }

    @Test
    void testStemOfEdWithDoubleConsonant() {
        assertEquals("stop", algorithm.stem("stopped"));
        assertEquals("plan", algorithm.stem("planned"));
        assertEquals("class", algorithm.stem("classed"));
        assertEquals("buzz",  algorithm.stem("buzzed"));
        assertEquals("fill",  algorithm.stem("filled"));
    }

    @Test
    void testStemWithERestoration() {
        assertEquals("make", algorithm.stem("making"));
        assertEquals("ride", algorithm.stem("riding"));
    }
}
