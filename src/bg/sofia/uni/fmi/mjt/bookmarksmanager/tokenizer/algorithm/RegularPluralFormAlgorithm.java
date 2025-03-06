package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm;

import java.util.Set;

public class RegularPluralFormAlgorithm implements StemmingAlgorithm {

    private static final String PLURAL_ENDING_REGULAR = "s";
    private static final String PLURAL_ENDING_VOWEL = "es";
    private static final Set<String> exceptionEndings = Set.of("ss", "is", "us");;


    @Override
    public String stem(String word) {
        if (word == null || word.length() < MIN_WORD_LENGTH) {
            return word;
        }
        if (word.length() > MIN_WORD_LENGTH && word.endsWith(PLURAL_ENDING_VOWEL)) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith(PLURAL_ENDING_REGULAR) && !exceptionEndings.contains(word.
                substring(word.length() - 2))) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
