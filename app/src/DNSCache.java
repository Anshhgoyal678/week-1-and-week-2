import java.util.*;
import java.util.concurrent.*;

public class DNSCache {

    // DNS Entry class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int capacity;

    // LRU Cache using LinkedHashMap
    private final LinkedHashMap<String, DNSEntry> cache;

    // Stats
    private long hits = 0;
    private long misses = 0;

    // Constructor
    public DNSCache(int capacity) {
        this.capacity = capacity;

        this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {
        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                hits++;
                log("Cache HIT → " + domain + " → " + entry.ipAddress);
                return entry.ipAddress;
            } else {
                cache.remove(domain);
                log("Cache EXPIRED → " + domain);
            }
        }

        // Cache miss
        misses++;
        String ip = queryUpstreamDNS(domain);

        // Store in cache with TTL (example: 5 sec)
        cache.put(domain, new DNSEntry(domain, ip, 5));

        log("Cache MISS → " + domain + " → " + ip);

        long endTime = System.nanoTime();
        log("Lookup time: " + (endTime - startTime) / 1_000_000.0 + " ms");

        return ip;
    }

    // Simulated upstream DNS call
    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // simulate latency (100ms)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    // Cache stats
    public synchronized String getCacheStats() {
        long total = hits + misses;
        double hitRate = (total == 0) ? 0 : (hits * 100.0 / total);
        return "Hit Rate: " + String.format("%.2f", hitRate) + "%, Hits: " + hits + ", Misses: " + misses;
    }

    // Background cleanup thread
    private void startCleanupThread() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, DNSEntry> entry = it.next();
                    if (entry.getValue().isExpired()) {
                        it.remove();
                        log("Removed expired: " + entry.getKey());
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(3);

        System.out.println(dnsCache.resolve("google.com")); // MISS
        System.out.println(dnsCache.resolve("google.com")); // HIT

        Thread.sleep(6000); // wait for TTL expiry

        System.out.println(dnsCache.resolve("google.com")); // EXPIRED → MISS

        System.out.println(dnsCache.getCacheStats());
    }
}