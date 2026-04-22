package org.example.Services;

import java.util.ArrayList;
import java.util.List;

public class RouteResult {
    private List<RouteStep> ordered = new ArrayList<>();
    private double totalKg;
    private double totalKm;
    private int selectedCount;

    public List<RouteStep> getOrdered() {
        return ordered;
    }

    public void setOrdered(List<RouteStep> ordered) {
        this.ordered = ordered;
    }

    public double getTotalKg() {
        return totalKg;
    }

    public void setTotalKg(double totalKg) {
        this.totalKg = totalKg;
    }

    public double getTotalKm() {
        return totalKm;
    }

    public void setTotalKm(double totalKm) {
        this.totalKm = totalKm;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(int selectedCount) {
        this.selectedCount = selectedCount;
    }
}