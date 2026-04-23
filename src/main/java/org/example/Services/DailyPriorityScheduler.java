package org.example.Services;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyPriorityScheduler {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final PointRecyclageService pointService = new PointRecyclageService();

    public void start() {
        runOnceAtStartup();

        long initialDelay = computeInitialDelayToMidnight();
        long period = TimeUnit.DAYS.toMillis(1);

        executor.scheduleAtFixedRate(this::runSafely, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private void runOnceAtStartup() {
        runSafely();
    }

    private void runSafely() {
        try {
            pointService.refreshDailyPriorityScores();
            System.out.println("Daily priority refresh executed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error while refreshing daily priority scores: " + e.getMessage());
        }
    }

    private long computeInitialDelayToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, nextMidnight).toMillis();
    }

    public void stop() {
        executor.shutdownNow();
    }
}