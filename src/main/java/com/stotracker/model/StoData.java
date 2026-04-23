package com.stotracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sto_data")
public class StoData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer dilithium = 0;

    @Column(nullable = false)
    private Integer credits = 0;

    @Column(name = "recruitment_time")
    private LocalDateTime recruitmentTime;

    @Column(name = "refining_time")
    private LocalDateTime refiningTime;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StoData() {
    }

    public StoData(String name) {
        this.name = name;
        this.dilithium = 0;
        this.credits = 0;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDilithium() {
        return dilithium;
    }

    public void setDilithium(Integer dilithium) {
        this.dilithium = dilithium;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public LocalDateTime getRecruitmentTime() {
        return recruitmentTime;
    }

    public void setRecruitmentTime(LocalDateTime recruitmentTime) {
        this.recruitmentTime = recruitmentTime;
    }

    public LocalDateTime getRefiningTime() {
        return refiningTime;
    }

    public void setRefiningTime(LocalDateTime refiningTime) {
        this.refiningTime = refiningTime;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}