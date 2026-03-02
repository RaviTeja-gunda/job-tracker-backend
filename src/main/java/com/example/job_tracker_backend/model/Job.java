package com.example.job_tracker_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(name = "job_description", length = 2000)
    private String description;

    private String jobUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private LocalDateTime appliedDate;

    private LocalDateTime postedDate;

    public enum JobStatus {
        RECOMMENDED,
        APPLIED,
        REJECTED,
        INTERVIEWING,
        ACCEPTED
    }
}
