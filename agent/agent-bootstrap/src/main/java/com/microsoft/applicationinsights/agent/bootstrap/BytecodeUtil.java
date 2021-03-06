/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.microsoft.applicationinsights.agent.bootstrap;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.opentelemetry.instrumentation.api.aisdk.MicrometerUtil;
import io.opentelemetry.instrumentation.api.aisdk.MicrometerUtil.MicrometerUtilDelegate;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

// supporting all properties of event, metric, remote dependency and page view telemetry
public class BytecodeUtil {

    private static BytecodeUtilDelegate delegate;

    public static void setDelegate(final BytecodeUtilDelegate delegate) {
        if (BytecodeUtil.delegate == null) {
            BytecodeUtil.delegate = delegate;
            MicrometerUtil.setDelegate(new MicrometerUtilDelegate() {
                @Override
                public void trackMetric(String name, double value, Integer count, Double min, Double max, Map<String, String> properties) {
                    delegate.trackMetric(name, value, count, min, max, null, properties, Collections.emptyMap());
                }
            });
        }
    }

    public static void trackEvent(String name, Map<String, String> properties, Map<String, String> tags, Map<String, Double> metrics) {
        if (delegate != null) {
            delegate.trackEvent(name, properties, tags, metrics);
        }
    }

    public static void trackMetric(String name, double value, Integer count, Double min, Double max, Double stdDev,
                                   Map<String, String> properties, Map<String, String> tags) {
        if (delegate != null) {
            delegate.trackMetric(name, value, count, min, max, stdDev, properties, tags);
        }
    }

    public static void trackDependency(String name, String id, String resultCode, Long totalMillis, boolean success,
                                       String commandName, String type, String target, Map<String, String> properties,
                                       Map<String, String> tags, Map<String, Double> metrics) {
        if (delegate != null) {
            delegate.trackDependency(name, id, resultCode, totalMillis, success, commandName, type, target, properties,
                    tags, metrics);
        }
    }

    public static void trackPageView(String name, URI uri, long totalMillis, Map<String, String> properties, Map<String, String> tags,
                                     Map<String, Double> metrics) {
        if (delegate != null) {
            delegate.trackPageView(name, uri, totalMillis, properties, tags, metrics);
        }
    }

    public static void trackTrace(String message, int severityLevel, Map<String, String> properties, Map<String, String> tags) {
        if (delegate != null) {
            delegate.trackTrace(message, severityLevel, properties, tags);
        }
    }

    public static void trackRequest(String id, String name, URL url, Date timestamp, Long duration, String responseCode, boolean success,
                                    String source, Map<String, String> properties, Map<String, String> tags) {
        if (delegate != null) {
            delegate.trackRequest(id, name, url, timestamp, duration, responseCode, success, source, properties, tags);
        }
    }

    public static void trackException(Exception exception, Map<String, String> properties, Map<String, String> tags, Map<String, Double> metrics) {
        if (delegate != null) {
            delegate.trackException(exception, properties, tags, metrics);
        }
    }

    public static void flush() {
        if (delegate != null) {
            delegate.flush();
        }
    }

    public static void logErrorOnce(Throwable t) {
        if (delegate != null) {
            delegate.logErrorOnce(t);
        }
    }

    public static long getTotalMilliseconds(long days, int hours, int minutes, int seconds, int milliseconds) {
        return DAYS.toMillis(days)
                + HOURS.toMillis(hours)
                + MINUTES.toMillis(minutes)
                + SECONDS.toMillis(seconds)
                + milliseconds;
    }

    // basically the same as SDK MapUtil.copy()
    public static void copy(Map<String, String> source, Map<String, String> target) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }

        if (source == null || source.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : source.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isEmpty()) {
                continue;
            }

            if (!target.containsKey(key)) {
                if (target instanceof ConcurrentHashMap && entry.getValue() == null) {
                    continue;
                } else {
                    target.put(key, entry.getValue());
                }
            }
        }
    }

    public interface BytecodeUtilDelegate {

        void trackEvent(String name, Map<String, String> properties, Map<String, String> tags, Map<String, Double> metrics);

        void trackMetric(String name, double value, Integer count, Double min, Double max,
                         Double stdDev, Map<String, String> properties, Map<String, String> tags);

        void trackDependency(String name, String id, String resultCode, Long totalMillis,
                             boolean success, String commandName, String type, String target,
                             Map<String, String> properties, Map<String, String> tags, Map<String, Double> metrics);

        void trackPageView(String name, URI uri, long totalMillis, Map<String, String> properties, Map<String, String> tags,
                           Map<String, Double> metrics);

        void trackTrace(String message, int severityLevel, Map<String, String> properties, Map<String, String> tags);

        void trackRequest(String id, String name, URL url, Date timestamp, Long duration, String responseCode, boolean success,
                          String source, Map<String, String> properties, Map<String, String> tags);

        void trackException(Exception exception, Map<String, String> properties, Map<String, String> tags, Map<String, Double> metrics);

        void flush();

        void logErrorOnce(Throwable t);
    }
}
