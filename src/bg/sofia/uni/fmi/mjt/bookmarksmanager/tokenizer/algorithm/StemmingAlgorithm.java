package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm;

public interface StemmingAlgorithm {

    int MIN_WORD_LENGTH = 3;
    String stem(String word);
}
