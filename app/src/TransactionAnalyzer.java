import java.util.*;
import java.time.*;

public class TransactionAnalyzer {

    // Transaction class
    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        LocalDateTime timestamp;

        Transaction(int id, double amount, String merchant, String account, LocalDateTime timestamp) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "{id:" + id + ", amount:" + amount + ", merchant:" + merchant + ", account:" + account + "}";
        }
    }

    // Classic Two-Sum: Find pairs summing to target
    public static List<List<Transaction>> findTwoSum(List<Transaction> transactions, double target) {
        List<List<Transaction>> result = new ArrayList<>();
        Map<Double, Transaction> map = new HashMap<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(Arrays.asList(map.get(complement), t));
            }
            map.put(t.amount, t);
        }

        return result;
    }

    // Two-Sum within time window (e.g., 1 hour)
    public static List<List<Transaction>> findTwoSumWithTimeWindow(List<Transaction> transactions,
                                                                   double target, long minutesWindow) {
        List<List<Transaction>> result = new ArrayList<>();
        transactions.sort(Comparator.comparing(t -> t.timestamp));

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t1 = transactions.get(i);
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t2 = transactions.get(j);
                long minutesDiff = Duration.between(t1.timestamp, t2.timestamp).toMinutes();
                if (minutesDiff > minutesWindow) break;

                if (Math.abs(t1.amount + t2.amount - target) < 1e-6) {
                    result.add(Arrays.asList(t1, t2));
                }
            }
        }
        return result;
    }

    // K-Sum using recursive backtracking with memoization
    public static List<List<Transaction>> findKSum(List<Transaction> transactions, int k, double target) {
        List<List<Transaction>> result = new ArrayList<>();
        findKSumHelper(transactions, 0, k, target, new ArrayList<>(), result);
        return result;
    }

    private static void findKSumHelper(List<Transaction> transactions, int start, int k, double target,
                                       List<Transaction> temp, List<List<Transaction>> result) {
        if (k == 0) {
            if (Math.abs(target) < 1e-6) result.add(new ArrayList<>(temp));
            return;
        }

        for (int i = start; i < transactions.size(); i++) {
            temp.add(transactions.get(i));
            findKSumHelper(transactions, i + 1, k - 1, target - transactions.get(i).amount, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    // Detect duplicate payments: same amount, same merchant, different accounts
    public static List<Map<String, Object>> detectDuplicates(List<Transaction> transactions) {
        Map<String, List<String>> map = new HashMap<>(); // key = merchant+amount
        List<Map<String, Object>> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.merchant + "|" + t.amount;
            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(t.account);
        }

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                String[] parts = entry.getKey().split("\\|");
                Map<String, Object> dup = new HashMap<>();
                dup.put("merchant", parts[0]);
                dup.put("amount", Double.parseDouble(parts[1]));
                dup.put("accounts", entry.getValue());
                duplicates.add(dup);
            }
        }

        return duplicates;
    }

    // Demo
    public static void main(String[] args) {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, 500, "Store A", "acc1", LocalDateTime.of(2026,3,18,10,0)),
                new Transaction(2, 300, "Store B", "acc2", LocalDateTime.of(2026,3,18,10,15)),
                new Transaction(3, 200, "Store C", "acc3", LocalDateTime.of(2026,3,18,10,30)),
                new Transaction(4, 500, "Store A", "acc2", LocalDateTime.of(2026,3,18,10,45))
        );

        System.out.println("=== Classic Two-Sum target=500 ===");
        List<List<Transaction>> twoSum = findTwoSum(transactions, 500);
        twoSum.forEach(System.out::println);

        System.out.println("\n=== Two-Sum within 60 minutes, target=500 ===");
        List<List<Transaction>> twoSumWindow = findTwoSumWithTimeWindow(transactions, 500, 60);
        twoSumWindow.forEach(System.out::println);

        System.out.println("\n=== K-Sum (k=3, target=1000) ===");
        List<List<Transaction>> kSum = findKSum(transactions,3,1000);
        kSum.forEach(System.out::println);

        System.out.println("\n=== Detect Duplicates ===");
        List<Map<String,Object>> duplicates = detectDuplicates(transactions);
        duplicates.forEach(System.out::println);
    }
}