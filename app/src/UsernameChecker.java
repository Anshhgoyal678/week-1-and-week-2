import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UsernameChecker {

    // username -> userId
    private final ConcurrentHashMap<String, Long> usernameToUserId;

    // username -> attempt count
    private final ConcurrentHashMap<String, AtomicInteger> usernameAttempts;

    public UsernameChecker() {
        usernameToUserId = new ConcurrentHashMap<>();
        usernameAttempts = new ConcurrentHashMap<>();
    }

    // Register a username
    public boolean registerUsername(String username, long userId) {
        return usernameToUserId.putIfAbsent(username, userId) == null;
    }

    // Check availability (O(1))
    public boolean checkAvailability(String username) {
        usernameAttempts
                .computeIfAbsent(username, k -> new AtomicInteger(0))
                .incrementAndGet();

        return !usernameToUserId.containsKey(username);
    }

    // Suggest alternatives
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String candidate = username + i;
            if (!usernameToUserId.containsKey(candidate)) {
                suggestions.add(candidate);
            }
        }

        if (username.contains("_")) {
            String dotVersion = username.replace("_", ".");
            if (!usernameToUserId.containsKey(dotVersion)) {
                suggestions.add(dotVersion);
            }
        }

        String prefix = "the_" + username;
        if (!usernameToUserId.containsKey(prefix)) {
            suggestions.add(prefix);
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String maxUser = null;
        int maxCount = 0;

        for (Map.Entry<String, AtomicInteger> entry : usernameAttempts.entrySet()) {
            int count = entry.getValue().get();
            if (count > maxCount) {
                maxCount = count;
                maxUser = entry.getKey();
            }
        }

        return maxUser;
    }

    // Optional: Get attempt count
    public int getAttemptCount(String username) {
        return usernameAttempts.getOrDefault(username, new AtomicInteger(0)).get();
    }

    // Main method
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        checker.registerUsername("john_doe", 1);
        checker.registerUsername("admin", 2);

        System.out.println(checker.checkAvailability("john_doe"));   // false
        System.out.println(checker.checkAvailability("jane_smith")); // true

        System.out.println(checker.suggestAlternatives("john_doe"));

        checker.checkAvailability("admin    ");
        checker.checkAvailability("admin   ");
        checker.checkAvailability("admin");

        System.out.println(checker.getMostAttempted()); // admin
    }
}