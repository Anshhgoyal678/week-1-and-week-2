import java.util.*;

class Transaction {
    String id;
    double fee;
    String timestamp; // HH:mm format

    public Transaction(String id, double fee, String timestamp) {
        this.id = id;
        this.fee = fee;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return id + ":" + fee + "@" + timestamp;
    }
}

public class TransactionSorter {

    // ✅ Bubble Sort (Stable, Optimized)
    public static void bubbleSortByFee(List<Transaction> list) {
        int n = list.size();
        int passes = 0, swaps = 0;

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            passes++;

            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).fee > list.get(j + 1).fee) {
                    Collections.swap(list, j, j + 1);
                    swaps++;
                    swapped = true;
                }
            }

            // Early termination
            if (!swapped) break;
        }

        System.out.println("Bubble Sort → Passes: " + passes + ", Swaps: " + swaps);
    }

    // ✅ Insertion Sort (Stable, Fee + Timestamp)
    public static void insertionSortByFeeAndTimestamp(List<Transaction> list) {
        for (int i = 1; i < list.size(); i++) {
            Transaction key = list.get(i);
            int j = i - 1;

            // Compare by fee first, then timestamp
            while (j >= 0 &&
                    (list.get(j).fee > key.fee ||
                            (list.get(j).fee == key.fee &&
                                    list.get(j).timestamp.compareTo(key.timestamp) > 0))) {

                list.set(j + 1, list.get(j)); // shift right
                j--;
            }

            list.set(j + 1, key);
        }
    }

    // ✅ Outlier Detection
    public static List<Transaction> findHighFeeOutliers(List<Transaction> list) {
        List<Transaction> outliers = new ArrayList<>();
        for (Transaction t : list) {
            if (t.fee > 50) {
                outliers.add(t);
            }
        }
        return outliers;
    }

    // ✅ Driver Method
    public static void processTransactions(List<Transaction> transactions) {
        int size = transactions.size();

        if (size <= 100) {
            bubbleSortByFee(transactions);
            System.out.println("Sorted by Fee (Bubble Sort): " + transactions);
        } else if (size <= 1000) {
            insertionSortByFeeAndTimestamp(transactions);
            System.out.println("Sorted by Fee + Timestamp (Insertion Sort): " + transactions);
        } else {
            System.out.println("Large dataset → Use advanced sort (not required here)");
        }

        // Outliers
        List<Transaction> outliers = findHighFeeOutliers(transactions);
        System.out.println("High-fee outliers (>50): " + outliers);
    }

    // ✅ Main (Sample Run)
    public static void main(String[] args) {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction("id1", 10.5, "10:00"));
        transactions.add(new Transaction("id2", 25.0, "09:30"));
        transactions.add(new Transaction("id3", 5.0, "10:15"));

        processTransactions(transactions);
    }
}