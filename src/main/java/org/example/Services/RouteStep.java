package org.example.Services;

import org.example.Entities.PointRecyclage;

public class RouteStep {
    private final PointRecyclage point;
    private final double distanceFromPrevKm;

    public RouteStep(PointRecyclage point, double distanceFromPrevKm) {
        this.point = point;
        this.distanceFromPrevKm = distanceFromPrevKm;
    }

    public PointRecyclage getPoint() {
        return point;
    }

    public double getDistanceFromPrevKm() {
        return distanceFromPrevKm;
    }
}