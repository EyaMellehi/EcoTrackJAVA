package org.example.Services;

public class AiEstimateResult {
    private int score;
    private String priority;
    private String explanation;
    private int ageDays;

    public AiEstimateResult() {
    }

    public AiEstimateResult(int score, String priority, String explanation, int ageDays) {
        this.score = score;
        this.priority = priority;
        this.explanation = explanation;
        this.ageDays = ageDays;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getAgeDays() {
        return ageDays;
    }

    public void setAgeDays(int ageDays) {
        this.ageDays = ageDays;
    }
}