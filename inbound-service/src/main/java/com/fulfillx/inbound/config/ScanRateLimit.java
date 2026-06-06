package com.fulfillx.inbound.config;

import com.fulfillx.inbound.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScanRateLimit {

    private final Map<String, Bucket> operatorBuckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket.builder().addLimit(Bandwidth.classic(10,Refill.greedy(10,Duration.ofSeconds(2))))
                .build();
    }

    public void checkScanLimit(String userId){
        Bucket bucket = operatorBuckets.computeIfAbsent(userId, id -> createBucket());

        if(!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                    "Scanning too fast, Relax. Max 10 scans per 2 seconds."
            );
        }

    }
}
