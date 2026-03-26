public class AccountSearch {

    // ✅ Linear Search (First & Last)
    public static void linearSearch(String[] arr, String target) {
        int first = -1, last = -1, comps = 0;

        for (int i = 0; i < arr.length; i++) {
            comps++;
            if (arr[i].equals(target)) {
                if (first == -1) first = i;
                last = i;
            }
        }

        System.out.println("First: " + first + ", Last: " + last + ", Comparisons: " + comps);
    }

    // ✅ Binary Search + Count
    public static int binarySearch(String[] arr, String target) {
        int low = 0, high = arr.length - 1, comps = 0;

        while (low <= high) {
            comps++;
            int mid = (low + high) / 2;

            if (arr[mid].equals(target)) {
                System.out.println("Found at " + mid + ", Comparisons: " + comps);
                return mid;
            } else if (arr[mid].compareTo(target) < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return -1;
    }
}