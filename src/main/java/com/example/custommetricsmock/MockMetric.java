package com.example.custommetricsmock;

import io.micrometer.core.instrument.Tags;

import java.util.Map;

public record MockMetric(String name, Map<String, String> tags, int value) {}
