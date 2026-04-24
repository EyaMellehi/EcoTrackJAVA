package org.example.Entities;

import java.util.ArrayList;
import java.util.List;

public class SignalementReviewResult {
    private String decision;
    private List<String> reasons = new ArrayList<>();
    private double toxicity;
    private double insult;
    private double threat;
    private Integer imagesCount;
    private Double clipScoreAvg;
    private Double clipScoreMin;
    private String model;

    public boolean isAccepted() {
        return "ACCEPT".equalsIgnoreCase(decision);
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public double getToxicity() {
        return toxicity;
    }

    public void setToxicity(double toxicity) {
        this.toxicity = toxicity;
    }

    public double getInsult() {
        return insult;
    }

    public void setInsult(double insult) {
        this.insult = insult;
    }

    public double getThreat() {
        return threat;
    }

    public void setThreat(double threat) {
        this.threat = threat;
    }

    public Integer getImagesCount() {
        return imagesCount;
    }

    public void setImagesCount(Integer imagesCount) {
        this.imagesCount = imagesCount;
    }

    public Double getClipScoreAvg() {
        return clipScoreAvg;
    }

    public void setClipScoreAvg(Double clipScoreAvg) {
        this.clipScoreAvg = clipScoreAvg;
    }

    public Double getClipScoreMin() {
        return clipScoreMin;
    }

    public void setClipScoreMin(Double clipScoreMin) {
        this.clipScoreMin = clipScoreMin;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}