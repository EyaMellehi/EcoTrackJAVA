package org.example.Entities;

import java.time.LocalDateTime;

public class Media {
    private int id;
    private String filename;
    private String type;
    private String url;
    private LocalDateTime createdAt;
    private Integer userId;
    private Integer signalementId;
    private Integer rapportSignalementId;
    private Integer annonceId;
    private Integer eventId;

    public Media() {
    }

    public Media(String filename, String type, String url, LocalDateTime createdAt,
                 Integer userId, Integer signalementId, Integer rapportSignalementId,
                 Integer annonceId, Integer eventId) {
        this.filename = filename;
        this.type = type;
        this.url = url;
        this.createdAt = createdAt;
        this.userId = userId;
        this.signalementId = signalementId;
        this.rapportSignalementId = rapportSignalementId;
        this.annonceId = annonceId;
        this.eventId = eventId;
    }

    public Media(int id, String filename, String type, String url, LocalDateTime createdAt,
                 Integer userId, Integer signalementId, Integer rapportSignalementId,
                 Integer annonceId, Integer eventId) {
        this.id = id;
        this.filename = filename;
        this.type = type;
        this.url = url;
        this.createdAt = createdAt;
        this.userId = userId;
        this.signalementId = signalementId;
        this.rapportSignalementId = rapportSignalementId;
        this.annonceId = annonceId;
        this.eventId = eventId;
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getSignalementId() {
        return signalementId;
    }

    public Integer getRapportSignalementId() {
        return rapportSignalementId;
    }

    public Integer getAnnonceId() {
        return annonceId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setSignalementId(Integer signalementId) {
        this.signalementId = signalementId;
    }

    public void setRapportSignalementId(Integer rapportSignalementId) {
        this.rapportSignalementId = rapportSignalementId;
    }

    public void setAnnonceId(Integer annonceId) {
        this.annonceId = annonceId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }
}