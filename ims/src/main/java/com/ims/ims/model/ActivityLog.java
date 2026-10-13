package com.ims.ims.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity to track system events and recent activities
 * (New Order Created, New Product Created, New Supplier Created, New Category Created)
 */
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type; // ORDER, PRODUCT, SUPPLIER, CATEGORY

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ActivityLog() {}

    public ActivityLog(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public ActivityLog(String type, String title, String description, LocalDateTime createdAt) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Formats elapsed time into human-friendly relative label matching reference design:
     * - Less than 1 hour: "Just now"
     * - More than 24 hours: "More than 24 hours ago"
     * - Otherwise: "X hours ago"
     */
    public String getTimeAgo() {
        if (createdAt == null) {
            return "Just now";
        }
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);
        long minutes = duration.toMinutes();
        long hours = duration.toHours();

        if (minutes < 60) {
            return "Just now";
        } else if (hours >= 24) {
            return "More than 24 hours ago";
        } else {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
