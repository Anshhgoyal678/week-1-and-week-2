import java.util.*;

public class PlagiarismDetector {

    // n-gram size
    private final int N = 5;

    // ngram -> set of document IDs
    private final Map<String, Set<String>> ngramIndex;

    // documentId -> list of ngrams
    private final Map<String, List<String>> documentNgrams;

    public PlagiarismDetector() {
        ngramIndex = new HashMap<>();
        documentNgrams = new HashMap<>();
    }

    // Add document to database
    public void addDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        documentNgrams.put(docId, ngrams);

        for (String ngram : ngrams) {
            ngramIndex
                    .computeIfAbsent(ngram, k -> new HashSet<>())
                    .add(docId);
        }
    }

    // Analyze a new document
    public void analyzeDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String ngram : ngrams) {
            Set<String> docs = ngramIndex.get(ngram);
            if (docs != null) {
                for (String existingDoc : docs) {
                    matchCount.put(existingDoc,
                            matchCount.getOrDefault(existingDoc, 0) + 1);
                }
            }
        }

        System.out.println("Extracted " + ngrams.size() + " n-grams");

        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();

            double similarity = (matches * 100.0) / ngrams.size();

            System.out.println("→ Found " + matches +
                    " matching n-grams with \"" + otherDoc + "\"");
            System.out.printf("→ Similarity: %.2f%% ", similarity);

            if (similarity > 60) {
                System.out.println("(PLAGIARISM DETECTED)");
            } else if (similarity > 15) {
                System.out.println("(suspicious)");
            } else {
                System.out.println("(low similarity)");
            }
        }
    }

    // Generate n-grams
    private List<String> generateNgrams(String text) {
        List<String> ngrams = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\W+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }

        return ngrams;
    }

    // Demo
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Existing documents
        detector.addDocument("essay_089.txt",
                "machine learning is a field of artificial intelligence that uses statistical techniques");

        detector.addDocument("essay_092.txt",
                "machine learning is a field of artificial intelligence that uses statistical techniques to give systems ability to learn");

        // New document
        detector.analyzeDocument("essay_123.txt",
                "machine learning is a field of artificial intelligence that uses statistical techniques to learn from data");
    }
}