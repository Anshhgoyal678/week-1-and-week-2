import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedRateLimiter {

    // clientId -> TokenBucket
    private final ConcurrentHashMap<String, TokenBucket> buckets;

    private final int MAX_TOKENS = 1000;          // per hour
    private final long REFILL_INTERVAL = 3600_000; // 1 hour in ms

    public DistributedRateLimiter() {
        buckets = new ConcurrentHashMap<>();
    }

    // Token Bucket Class
    static class TokenBucket {
        AtomicInteger tokens;
        long lastRefillTime;

        TokenBucket(int maxTokens) {
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }
    }

    // Check rate limit (O(1))
    public String checkRateLimit(String clientId) {
        long now = System.currentTimeMillis();

        TokenBucket bucket = buckets.computeIfAbsent(
                clientId, k -> new TokenBucket(MAX_TOKENS)
        );

        synchronized (bucket) {

            // Refill tokens if 1 hour passed
            if (now - bucket.lastRefillTime >= REFILL_INTERVAL) {
                bucket.tokens.set(MAX_TOKENS);
                bucket.lastRefillTime = now;
            }

            int currentTokens = bucket.tokens.get();

            if (currentTokens > 0) {
                bucket.tokens.decrementAndGet();
                return "Allowed (" + (currentTokens - 1) + " requests remaining)";
            } else {
                long retryAfter = (REFILL_INTERVAL - (now - bucket.lastRefillTime)) / 1000;
                return "Denied (0 remaining, retry after " + retryAfter + "s)";
            }
        }
    }

    // Get client status
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.get(clientId);

        if (bucket == null) {
            return "No usage yet";
        }

        int used = MAX_TOKENS - bucket.tokens.get();
        long resetTime = bucket.lastRefillTime + REFILL_INTERVAL;

        return "{used: " + used +
                ", limit: " + MAX_TOKENS +
                ", reset: " + resetTime + "}";
    }

    // Demo
    public static void main(String[] args) {
        DistributedRateLimiter limiter = new DistributedRateLimiter();

        String client = "abc123";

        // Simulate requests
        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }

        System.out.println(limiter.getRateLimitStatus(client));
    }
}