package org.example.Services;

import org.example.Entities.PointRecyclage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecyclageRouteOptimizer {

    public RouteResult compute(List<PointRecyclage> points, double capacityKg, double startLat, double startLng) {
        List<PointRecyclage> sorted = new ArrayList<>(points);

        sorted.sort((a, b) -> Integer.compare(
                prioWeight(safePriority(b.getAiPriority())),
                prioWeight(safePriority(a.getAiPriority()))
        ));

        List<PointRecyclage> selected = new ArrayList<>();
        double totalKg = 0.0;

        for (PointRecyclage p : sorted) {
            double q = p.getQuantite();
            if (q <= 0) continue;

            if (totalKg + q <= capacityKg) {
                selected.add(p);
                totalKg += q;
            }
        }

        List<RouteStep> ordered = new ArrayList<>();
        List<PointRecyclage> remaining = new ArrayList<>(selected);

        double curLat = startLat;
        double curLng = startLng;
        double totalKm = 0.0;

        while (!remaining.isEmpty()) {
            final double currentLat = curLat;
            final double currentLng = curLng;

            PointRecyclage next = remaining.stream()
                    .min(Comparator.comparingDouble(p ->
                            haversineKm(currentLat, currentLng, p.getLatitude(), p.getLongitude())))
                    .orElse(null);

            if (next == null) break;

            double bestDist = haversineKm(currentLat, currentLng, next.getLatitude(), next.getLongitude());

            ordered.add(new RouteStep(next, round2(bestDist)));
            totalKm += bestDist;

            curLat = next.getLatitude();
            curLng = next.getLongitude();

            remaining.remove(next);
        }

        RouteResult result = new RouteResult();
        result.setOrdered(ordered);
        result.setTotalKg(round2(totalKg));
        result.setTotalKm(round2(totalKm));
        result.setSelectedCount(selected.size());

        return result;
    }

    private int prioWeight(String p) {
        return switch (p) {
            case "URGENT" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private String safePriority(String p) {
        return p == null ? "" : p.trim().toUpperCase();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}