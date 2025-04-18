package com.wellbeing.deviceusage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "daily_limit_minutes", nullable = false)
    private Integer dailyLimitMinutes;

    @Column(name = "days_of_week")
    private String daysOfWeek; // "1,2,3,4,5,6,7" for days of week

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}