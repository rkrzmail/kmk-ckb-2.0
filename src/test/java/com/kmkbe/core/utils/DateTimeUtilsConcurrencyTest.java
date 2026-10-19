package com.kmkbe.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeUtilsConcurrencyTest {

    @Test
    @DisplayName("SDF_STANDARD_DATE must format and parse concurrently across threads without corrupting dates")
    void sdfStandardDateConcurrencySafety() throws InterruptedException, ExecutionException {
        int threads = 16;
        int iterationsPerThread = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        List<Callable<Boolean>> tasks = new ArrayList<>();
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final int threadIndex = i;
            tasks.add(() -> {
                // Tiap thread menguji format dan parse tanggal berbeda-beda
                String dateStr = String.format("2024-%02d-%02d", (threadIndex % 12) + 1, (threadIndex % 25) + 1);
                for (int j = 0; j < iterationsPerThread; j++) {
                    Date parsed = DateTimeUtils.SDF_STANDARD_DATE.parse(dateStr);
                    String formatted = DateTimeUtils.SDF_STANDARD_DATE.format(parsed);
                    if (!dateStr.equals(formatted)) {
                        return false;
                    }
                }
                successCounter.incrementAndGet();
                return true;
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(results).hasSize(threads);
        for (Future<Boolean> result : results) {
            assertThat(result.get()).isTrue();
        }
        assertThat(successCounter.get()).isEqualTo(threads);
    }

    @Test
    @DisplayName("SDF_STANDARD_DATE_TIME must handle high concurrent load without throwing exception")
    void sdfStandardDateTimeConcurrencySafety() throws InterruptedException {
        int threads = 16;
        int iterations = 150;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    String timeStr = "2025-05-15 14:30:45";
                    for (int j = 0; j < iterations; j++) {
                        Date d = DateTimeUtils.SDF_STANDARD_DATE_TIME.parse(timeStr);
                        String out = DateTimeUtils.SDF_STANDARD_DATE_TIME.format(d);
                        if (!timeStr.equals(out)) {
                            throw new IllegalStateException("Mismatch in thread: expected " + timeStr + " but got " + out);
                        }
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).isTrue();
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("SDF_STANDARD_RESPONSE_DATE formats correctly and clone creates thread-safe copy")
    void sdfResponseDateAndClone() throws ParseException {
        Date date = DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.parse("25/12/2024");
        String formatted = DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.format(date);
        assertThat(formatted).isEqualTo("25/12/2024");

        Object cloned = DateTimeUtils.SDF_STANDARD_RESPONSE_DATE.clone();
        assertThat(cloned).isInstanceOf(DateTimeUtils.ThreadSafeSimpleDateFormat.class);
    }
}
