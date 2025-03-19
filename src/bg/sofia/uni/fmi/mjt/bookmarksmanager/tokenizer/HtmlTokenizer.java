package bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm.RegularPluralFormAlgorithm;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.algorithm.SuffixStrippingAlgorithm;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlTokenizer {

    private static final String DEFAULT_STOPWORDS_FILE = "src" +
            File.separator + "stopwords.txt";
    private static final int MAX_KEYWORDS_NUMBER = 15;
    private static final String CSS_QUERY = "p, h1, h2, h3, h4, h5, h6, li, div";
    private static final RegularPluralFormAlgorithm pluralStemmer =
            new RegularPluralFormAlgorithm();
    private static final SuffixStrippingAlgorithm suffixStemmer =
            new SuffixStrippingAlgorithm();

    private final Set<String> stopwords;


    public HtmlTokenizer() {
        try (var br = new BufferedReader(new FileReader(DEFAULT_STOPWORDS_FILE))) {
            stopwords = br.lines().collect(Collectors.toSet());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not load dataset", ex);
        }
    }

    public HtmlTokenizer(Reader stopwordsReader) {
        try (var br = new BufferedReader(stopwordsReader)) {
            stopwords = br.lines().collect(Collectors.toSet());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not load dataset", ex);
        }
    }

    public HtmlTokenizer(Set<String> stopwords) {  //I used this constructor mainly for testing purposes
        if (stopwords == null || stopwords.isEmpty()) {
            throw new IllegalArgumentException("Initialization set " +
                    "of stop words must not be null or empty!");
        }
        this.stopwords = stopwords;
    }

    public String getTitle(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.title().strip().replaceAll(" +", "-");
        } catch (IOException e) {
            ExceptionsLogger.logClientException(e);
        }
        return null;
    }

    public Set<String> getKeywords(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String pageText = getText(doc);
            return tokenize(pageText);
        } catch (IOException e) {
            ExceptionsLogger.logClientException(e);
        }
        return null;
    }

    public Set<String> tokenize(String input) {
        System.out.println("Input:" + input);
        Stream<String> derivedWords = getStringStream(input);

        Map<String, Long> wordsOccurrences = derivedWords.collect(
                Collectors.groupingBy(word -> word, Collectors.counting()));

       return wordsOccurrences.entrySet().stream().sorted(Map.Entry.<String,
                               Long>comparingByValue().
                       reversed()).limit(Math.min(MAX_KEYWORDS_NUMBER,
                       wordsOccurrences.size()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Stream<String> getStringStream(String input) {
        if (input == null || input.isEmpty() || input.isBlank()) {
            throw new IllegalArgumentException("String passed for" +
                    " tokenization must not be null, blank or empty!");
        }
        String refactoredInput = input.trim().replaceAll("\\p{Punct}",
                "").replaceAll("\\s+", " ").toLowerCase();


        return Arrays.stream(refactoredInput.split(" ")).filter(word ->
                !stopwords.contains(word)).map(HtmlTokenizer::applyAlgorithms);
    }


    private static String getText(Document doc) {
        Elements elements = doc.select(CSS_QUERY);

        StringBuilder textBuilder = new StringBuilder();
        for (Element element : elements) {
            String text = element.text();
            textBuilder.append(text).append(" ");
        }
        return textBuilder.toString();
    }

    private static String applyAlgorithms(String word) {
        return suffixStemmer.stem(pluralStemmer.stem(word));
    }
}
