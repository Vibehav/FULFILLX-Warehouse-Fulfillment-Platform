package com.fulfillx.auth.config;




import com.fulfillx.auth.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiter {
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    private Bucket createLoginBucket() {
        // 5 attempts per minute per IP
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build();
    }

    public void checkLoginLimit(String ipAddress) {
        Bucket bucket = loginBuckets.computeIfAbsent(ipAddress, ip -> createLoginBucket());

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Too many login attempts. Try again in 1 minute.");
        }

    }
}
