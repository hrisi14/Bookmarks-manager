package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm;

public class SuffixStrippingAlgorithm implements StemmingAlgorithm {

    private static final String ADVERB_ING_SUFFIX = "ing";
    private static final String ADVERB_ED_SUFFIX = "ed";
    private static final String ADVERB_LY_SUFFIX = "ly";

    @Override
    public String stem(String word) {
        if (word == null || word.isBlank()) {
            return null;
        }
        if (word.endsWith(ADVERB_ING_SUFFIX)) {
            return word.substring(0, word.length() - 3);
        } else {
              if (word.endsWith(ADVERB_ED_SUFFIX)) {
                return word.substring(0, word.length() - 2);
            } else if (word.endsWith(ADVERB_LY_SUFFIX)) {
                return word.substring(0, word.length() - 2);
            }
        }
        return word;
    }
}
