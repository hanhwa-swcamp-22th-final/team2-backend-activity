package com.team2.activity.entity;

import com.team2.activity.entity.converter.ActivityTypeConverter;
import com.team2.activity.entity.converter.PriorityConverter;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.entity.enums.Priority;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "po_id", length = 30)
    private String poId;

    @Column(name = "activity_author_id", nullable = false)
    private Long activityAuthorId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Convert(converter = ActivityTypeConverter.class)
    @Column(name = "activity_type", nullable = false, length = 20)
    private ActivityType activityType;

    @Column(name = "activity_title", nullable = false, length = 100)
    private String activityTitle;

    @Column(name = "activity_content", columnDefinition = "TEXT")
    private String activityContent;

    @Convert(converter = PriorityConverter.class)
    @Column(name = "activity_priority", length = 10)
    private Priority activityPriority;

    @Column(name = "activity_schedule_from")
    private LocalDate activityScheduleFrom;

    @Column(name = "activity_schedule_to")
    private LocalDate activityScheduleTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Activity(Long activityId, Long clientId, String poId, Long activityAuthorId, LocalDate activityDate,
                     ActivityType activityType, String activityTitle, String activityContent,
                     Priority activityPriority, LocalDate activityScheduleFrom, LocalDate activityScheduleTo) {
        this.activityId = activityId;
        this.clientId = clientId;
        this.poId = poId;
        this.activityAuthorId = activityAuthorId;
        this.activityDate = activityDate;
        this.activityType = activityType;
        this.activityTitle = activityTitle;
        this.activityContent = activityContent;
        this.activityPriority = activityPriority;
        this.activityScheduleFrom = activityScheduleFrom;
        this.activityScheduleTo = activityScheduleTo;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(ActivityType activityType, String activityTitle, String activityContent,
                       LocalDate activityDate, Long activityAuthorId, String poId,
                       Priority activityPriority, LocalDate activityScheduleFrom, LocalDate activityScheduleTo) {
        this.activityType = activityType;
        this.activityTitle = activityTitle;
        this.activityContent = activityContent;
        this.activityDate = activityDate;
        this.activityAuthorId = activityAuthorId;
        this.poId = poId;
        this.activityPriority = activityPriority;
        this.activityScheduleFrom = activityScheduleFrom;
        this.activityScheduleTo = activityScheduleTo;
    }
}
