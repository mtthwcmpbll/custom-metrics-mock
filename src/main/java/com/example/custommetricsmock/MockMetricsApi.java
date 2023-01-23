package com.example.custommetricsmock;


import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mock/metrics")
public class MockMetricsApi {

    MeterRegistry registry;

    Map<String, AtomicInteger> gaugeCache;

    public MockMetricsApi(MeterRegistry registry) {
        this.registry = registry;
        gaugeCache = new HashMap<>();
    }

    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setMetric(@RequestBody  MockMetric mockMetric) {
        Tags mockTags = Tags.empty();
        for (Map.Entry<String, String> tag : mockMetric.tags().entrySet()) {
            mockTags = mockTags.and(tag.getKey(), tag.getValue());
        }

        String tagsString = mockTags.stream()
                .map(tag -> tag.getKey()+":"+tag.getValue()) // map to a string format
                .sorted() // ensure that we have a deterministic order for the same tags
                .collect(Collectors.joining("+")); //join into a single tags string
        String mockMetricsCacheKey = mockMetric.name() + "+" + tagsString;

        if (gaugeCache.containsKey(mockMetricsCacheKey)) {
            gaugeCache.get(mockMetricsCacheKey).set(mockMetric.value());
        } else {
            AtomicInteger newGauge = registry.gauge(mockMetric.name(), mockTags, new AtomicInteger(mockMetric.value()));
            gaugeCache.put(mockMetricsCacheKey, newGauge);
        }

        return ResponseEntity.ok("OK");
    }

}
