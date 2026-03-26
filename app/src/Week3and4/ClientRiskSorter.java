import java.util.*;

class Client {
    String name;
    int riskScore;
    double accountBalance;

    public Client(String name, int riskScore, double accountBalance) {
        this.name = name;
        this.riskScore = riskScore;
        this.accountBalance = accountBalance;
    }

    @Override
    public String toString() {
        return name + ":" + riskScore + "($" + accountBalance + ")";
    }
}

public class ClientRiskSorter {

    // ✅ Bubble Sort (Ascending Risk Score, with swap visualization)
    public static void bubbleSortAscending(Client[] arr) {
        int n = arr.length;
        int swaps = 0;

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].riskScore > arr[j + 1].riskScore) {

                    // Swap
                    Client temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    swaps++;
                    swapped = true;

                    // 🔍 Visualization
                    System.out.println("Swap: " + arr[j].name + " <-> " + arr[j + 1].name);
                }
            }

            if (!swapped) break; // Early exit
        }

        System.out.println("Total swaps: " + swaps);
    }

    // ✅ Insertion Sort (Risk DESC + Balance ASC)
    public static void insertionSortDesc(Client[] arr) {
        for (int i = 1; i < arr.length; i++) {
            Client key = arr[i];
            int j = i - 1;

            // Sort by:
            // 1. Higher riskScore first (DESC)
            // 2. Lower accountBalance first (ASC for tie)
            while (j >= 0 &&
                    (arr[j].riskScore < key.riskScore ||
                            (arr[j].riskScore == key.riskScore &&
                                    arr[j].accountBalance > key.accountBalance))) {

                arr[j + 1] = arr[j]; // shift
                j--;
            }

            arr[j + 1] = key;
        }
    }

    // ✅ Top N High Risk Clients
    public static List<Client> getTopNHighRiskClients(Client[] arr, int n) {
        List<Client> top = new ArrayList<>();

        for (int i = 0; i < Math.min(n, arr.length); i++) {
            top.add(arr[i]); // already sorted DESC
        }

        return top;
    }

    // ✅ Utility Print
    public static void printArray(Client[] arr) {
        System.out.println(Arrays.toString(arr));
    }

    // ✅ Main (Sample Run)
    public static void main(String[] args) {
        Client[] clients = {
                new Client("clientC", 80, 5000),
                new Client("clientA", 20, 2000),
                new Client("clientB", 50, 3000)
        };

        // 🔹 Bubble Sort Demo
        bubbleSortAscending(clients);
        System.out.print("Bubble Sorted (ASC): ");
        printArray(clients);

        // 🔹 Insertion Sort (DESC)
        insertionSortDesc(clients);
        System.out.print("Insertion Sorted (DESC): ");
        printArray(clients);

        // 🔹 Top Risks
        List<Client> top = getTopNHighRiskClients(clients, 10);
        System.out.println("Top Risks: " + top);
    }
}