package org.example.Services;

public class ChatbotResult {
    private String reply;
    private String draft;

    public ChatbotResult() {
    }

    public ChatbotResult(String reply, String draft) {
        this.reply = reply;
        this.draft = draft;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getDraft() {
        return draft;
    }

    public void setDraft(String draft) {
        this.draft = draft;
    }
}