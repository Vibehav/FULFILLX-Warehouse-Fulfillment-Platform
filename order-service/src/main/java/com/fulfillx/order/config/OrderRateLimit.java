package com.fulfillx.order.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderRateLimit {

    private final Map<String, Bucket> createOrderBuckets = new ConcurrentHashMap<>();


    private Bucket createOrderBucket(){
        // 20 orders per minute per seller
        return Bucket.builder()
                .addLimit(Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1))))
                .build();
    }

    public void checkCreateOrderLimit(String sellerId) {
        Bucket bucket = createOrderBuckets.computeIfAbsent(sellerId, id -> createOrderBucket());

        if (!bucket.tryConsume(1)) {

            throw new RateLimitExceededException(
                    "Too many orders. Max 20 orders per minute per seller."
            );
        }

    }
}
