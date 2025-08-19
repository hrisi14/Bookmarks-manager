package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm;

import java.util.Set;

public class SuffixStrippingAlgorithm implements StemmingAlgorithm {

    private static final String ADVERB_ING_SUFFIX = "ing";
    private static final String ADVERB_ED_SUFFIX  = "ed";
    private static final String ADVERB_LY_SUFFIX  = "ly";

    private static final int ING_LENGTH = 3;
    private static final int SUFFIX_LENGTH = 2;

    private static final Set<Character> LEAVE_DOUBLE_ENDINGS_SET = Set.of('s', 'z', 'l');  // exception cases like
                                                                                          // class, buzz, fill

    @Override
    public String stem(String word) {
        if (word == null || word.isBlank()) {
            return null;
        }

        String w = word;

        if (w.endsWith(ADVERB_ING_SUFFIX) && w.length() > ING_LENGTH) {
            w = w.substring(0, w.length() - ING_LENGTH);
            w = removeDoubleLetters(w);
            w = restoreSilentE(w, ADVERB_ING_SUFFIX);
        } else if (w.endsWith(ADVERB_ED_SUFFIX) && w.length() > SUFFIX_LENGTH) {
            w = w.substring(0, w.length() - SUFFIX_LENGTH);
            w = removeDoubleLetters(w);
            w = restoreSilentE(w, ADVERB_ED_SUFFIX);
        } else if (w.endsWith(ADVERB_LY_SUFFIX) && w.length() > SUFFIX_LENGTH) {
            w = w.substring(0, w.length() - SUFFIX_LENGTH);
            w = removeDoubleLetters(w);
        }
        return w;
    }

    private static String removeDoubleLetters(String stem) {
        if (stem == null || stem.length() < 2) return stem;

        char last = stem.charAt(stem.length() - 1);
        char prev = stem.charAt(stem.length() - 2);

        if (last == prev && isConsonant(last)) {

            boolean cvcDoublingPattern = false;
            if (stem.length() >= ING_LENGTH) {
                char beforePair = stem.charAt(stem.length() - ING_LENGTH);
                cvcDoublingPattern = isVowel(beforePair);
            }

            if (cvcDoublingPattern && !LEAVE_DOUBLE_ENDINGS_SET.contains(last)) {
                return stem.substring(0, stem.length() - 1);
            }
        }
        return stem;
    }

    private static String restoreSilentE(String stem, String suffix) {
        if (stem == null || stem.length() < SUFFIX_LENGTH) {
            return stem;
        }
        char last = stem.charAt(stem.length() - 1);
        char prev = stem.charAt(stem.length() - 2);

        if (isConsonant(last) && isVowel(prev) && stem.length() == ING_LENGTH &&
                (stem.endsWith("k") || stem.endsWith("s") || stem.endsWith("d"))) {
            return stem + "e";
        }
        return stem;
    }

    private static boolean isVowel(char c) {
        char ch = Character.toLowerCase(c);
        return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
    }

    private static boolean isConsonant(char c) {
        return Character.isLetter(c) && !isVowel(c);
    }
}
