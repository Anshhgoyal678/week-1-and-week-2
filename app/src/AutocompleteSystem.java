import java.util.*;

public class AutocompleteSystem {

    // Global frequency map: query -> frequency
    private final Map<String, Integer> frequencyMap;

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        List<String> topQueries = new ArrayList<>(); // top suggestions at this node
    }

    private final TrieNode root;

    private final int TOP_K = 10;

    public AutocompleteSystem() {
        frequencyMap = new HashMap<>();
        root = new TrieNode();
    }

    // Insert query into Trie
    private void insert(String query) {
        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            // Maintain top K queries at each node
            if (!node.topQueries.contains(query)) {
                node.topQueries.add(query);
            }

            node.topQueries.sort((a, b) ->
                    frequencyMap.get(b) - frequencyMap.get(a));

            if (node.topQueries.size() > TOP_K) {
                node.topQueries.remove(node.topQueries.size() - 1);
            }
        }
    }

    // Update frequency
    public void updateFrequency(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
        insert(query); // update Trie rankings
    }

    // Search prefix → return top suggestions
    public List<String> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        return node.topQueries;
    }

    // Basic typo correction (edit distance 1)
    public List<String> suggestWithTypo(String input) {
        List<String> results = new ArrayList<>();

        for (String query : frequencyMap.keySet()) {
            if (isEditDistanceOne(input, query)) {
                results.add(query);
            }
        }

        results.sort((a, b) -> frequencyMap.get(b) - frequencyMap.get(a));
        return results.size() > TOP_K ? results.subList(0, TOP_K) : results;
    }

    // Check edit distance = 1
    private boolean isEditDistanceOne(String a, String b) {
        int m = a.length(), n = b.length();
        if (Math.abs(m - n) > 1) return false;

        int i = 0, j = 0, edits = 0;

        while (i < m && j < n) {
            if (a.charAt(i) != b.charAt(j)) {
                if (edits++ == 1) return false;

                if (m > n) i++;
                else if (m < n) j++;
                else {
                    i++;
                    j++;
                }
            } else {
                i++;
                j++;
            }
        }

        return true;
    }

    // Demo
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        // Add queries
        system.updateFrequency("java tutorial");
        system.updateFrequency("javascript");
        system.updateFrequency("java download");
        system.updateFrequency("java tutorial");
        system.updateFrequency("java tutorial");

        // Search prefix
        System.out.println("Search 'jav':");
        List<String> results = system.search("jav");

        int rank = 1;
        for (String res : results) {
            System.out.println(rank++ + ". " + res +
                    " (" + system.frequencyMap.get(res) + " searches)");
        }

        // Typo suggestion
        System.out.println("\nTypo suggestions for 'jvaa':");
        System.out.println(system.suggestWithTypo("jvaa"));
    }
}