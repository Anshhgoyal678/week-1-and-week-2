public class RiskLookup {

    // ✅ Linear Search
    public static boolean linearSearch(int[] arr, int target) {
        int comps = 0;
        for (int x : arr) {
            comps++;
            if (x == target) {
                System.out.println("Found with " + comps + " comparisons");
                return true;
            }
        }
        System.out.println("Not found, comparisons: " + comps);
        return false;
    }

    // ✅ Binary Floor & Ceiling
    public static void floorCeiling(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        Integer floor = null, ceil = null;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (arr[mid] == target) {
                floor = ceil = arr[mid];
                break;
            } else if (arr[mid] < target) {
                floor = arr[mid];
                low = mid + 1;
            } else {
                ceil = arr[mid];
                high = mid - 1;
            }
        }

        System.out.println("Floor: " + floor + ", Ceiling: " + ceil);
    }
}