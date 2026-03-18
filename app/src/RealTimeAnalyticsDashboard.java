import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RealTimeAnalyticsDashboard {

    // pageUrl -> total visits
    private final ConcurrentHashMap<String, AtomicInteger> pageViews;

    // pageUrl -> unique users
    private final ConcurrentHashMap<String, Set<String>> uniqueVisitors;

    // source -> count
    private final ConcurrentHashMap<String, AtomicInteger> trafficSources;

    public RealTimeAnalyticsDashboard() {
        pageViews = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        trafficSources = new ConcurrentHashMap<>();

        startDashboardUpdater();
    }

    // Event structure
    static class Event {
        String url;
        String userId;
        String source;

        Event(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // Process incoming event (O(1))
    public void processEvent(Event event) {

        // Update page views
        pageViews
                .computeIfAbsent(event.url, k -> new AtomicInteger(0))
                .incrementAndGet();

        // Update unique visitors
        uniqueVisitors
                .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                .add(event.userId);

        // Update traffic source
        trafficSources
                .computeIfAbsent(event.source, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    // Get Top 10 pages
    private List<Map.Entry<String, AtomicInteger>> getTopPages() {
        PriorityQueue<Map.Entry<String, AtomicInteger>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(e -> e.getValue().get()));

        for (Map.Entry<String, AtomicInteger> entry : pageViews.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, AtomicInteger>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> b.getValue().get() - a.getValue().get());

        return result;
    }

    // Dashboard output
    public void getDashboard() {
        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        List<Map.Entry<String, AtomicInteger>> topPages = getTopPages();

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, AtomicInteger> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue().get();
            int unique = uniqueVisitors.get(url).size();

            System.out.println(rank++ + ". " + url +
                    " - " + views + " views (" + unique + " unique)");
        }

        // Traffic sources
        int totalTraffic = trafficSources.values().stream()
                .mapToInt(AtomicInteger::get).sum();

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, AtomicInteger> entry : trafficSources.entrySet()) {
            double percentage = (entry.getValue().get() * 100.0) / totalTraffic;
            System.out.printf("%s: %.2f%%\n", entry.getKey(), percentage);
        }
    }

    // Auto update dashboard every 5 seconds
    private void startDashboardUpdater() {
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(this::getDashboard,
                5, 5, TimeUnit.SECONDS);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalyticsDashboard dashboard =
                new RealTimeAnalyticsDashboard();

        // Simulate streaming events
        dashboard.processEvent(new Event("/article/breaking-news", "user_1", "google"));
        dashboard.processEvent(new Event("/article/breaking-news", "user_2", "facebook"));
        dashboard.processEvent(new Event("/sports/championship", "user_3", "direct"));
        dashboard.processEvent(new Event("/sports/championship", "user_1", "google"));
        dashboard.processEvent(new Event("/article/breaking-news", "user_1", "google"));

        // Keep app running to see updates
        Thread.sleep(15000);
    }
}