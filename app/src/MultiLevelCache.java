import java.util.*;
import java.util.concurrent.*;

public class MultiLevelCache {

    // Video Data class
    static class VideoData {
        String videoId;
        String content; // could be URL or actual content
        public VideoData(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
        }
    }

    // L1: In-memory cache with LRU
    private final LinkedHashMap<String, VideoData> l1Cache;
    private final int L1_CAPACITY = 10_000;

    // L2: SSD-backed cache simulation
    private final ConcurrentHashMap<String, VideoData> l2Cache;
    private final int L2_CAPACITY = 100_000;
    private final Map<String, Integer> accessCountL2;

    // L3: Database simulation
    private final Map<String, VideoData> database;

    // Stats
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0;
    private long l1Time = 0, l2Time = 0, l3Time = 0;

    public MultiLevelCache(Map<String, VideoData> db) {
        this.l1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1_CAPACITY;
            }
        };
        this.l2Cache = new ConcurrentHashMap<>();
        this.accessCountL2 = new ConcurrentHashMap<>();
        this.database = db;
    }

    // Get video
    public VideoData getVideo(String videoId) {
        long start = System.nanoTime();

        // L1 lookup
        synchronized (l1Cache) {
            if (l1Cache.containsKey(videoId)) {
                l1Hits++;
                l1Time += (System.nanoTime() - start);
                return l1Cache.get(videoId); // HIT
            }
        }

        // L2 lookup
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            l2Time += (System.nanoTime() - start);
            incrementAccessCount(videoId);

            // Promote to L1 if frequent
            if (accessCountL2.get(videoId) > 5) {
                synchronized (l1Cache) {
                    l1Cache.put(videoId, l2Cache.get(videoId));
                }
            }

            return l2Cache.get(videoId);
        }

        // L3 database lookup
        l3Hits++;
        l3Time += (System.nanoTime() - start);
        VideoData data = database.get(videoId);

        if (data != null) {
            // Add to L2
            if (l2Cache.size() >= L2_CAPACITY) {
                // simple eviction: remove random (could be improved with LRU)
                Iterator<String> it = l2Cache.keySet().iterator();
                if (it.hasNext()) {
                    String key = it.next();
                    it.remove();
                    accessCountL2.remove(key);
                }
            }
            l2Cache.put(videoId, data);
            accessCountL2.put(videoId, 1);
        }

        return data;
    }

    private void incrementAccessCount(String videoId) {
        accessCountL2.put(videoId, accessCountL2.getOrDefault(videoId, 0) + 1);
    }

    // Stats
    public void getStatistics() {
        int totalHits = l1Hits + l2Hits + l3Hits;
        double overallHitRate = totalHits == 0 ? 0 :
                ((double) (l1Hits + l2Hits) / totalHits) * 100;

        System.out.println("L1: Hit Rate " + (totalHits == 0 ? 0 : (l1Hits * 100.0 / totalHits)) +
                "%, Avg Time: " + (l1Hits == 0 ? 0 : (l1Time / l1Hits) / 1_000_000.0) + "ms");
        System.out.println("L2: Hit Rate " + (totalHits == 0 ? 0 : (l2Hits * 100.0 / totalHits)) +
                "%, Avg Time: " + (l2Hits == 0 ? 0 : (l2Time / l2Hits) / 1_000_000.0) + "ms");
        System.out.println("L3: Hit Rate " + (totalHits == 0 ? 0 : (l3Hits * 100.0 / totalHits)) +
                "%, Avg Time: " + (l3Hits == 0 ? 0 : (l3Time / l3Hits) / 1_000_000.0) + "ms");
        System.out.println("Overall: Hit Rate " + overallHitRate + "%");
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        // Simulate DB
        Map<String, VideoData> db = new HashMap<>();
        for (int i = 1; i <= 1_000_000; i++) {
            db.put("video_" + i, new VideoData("video_" + i, "content_" + i));
        }

        MultiLevelCache cache = new MultiLevelCache(db);

        // Access some videos
        cache.getVideo("video_123"); // L3 -> L2
        cache.getVideo("video_123"); // L2 -> maybe promote L1
        cache.getVideo("video_123"); // L1 hit
        cache.getVideo("video_999"); // L3 -> L2

        cache.getStatistics();
    }
}