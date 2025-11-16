package com.stanbic.internMs.intern.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Table(name="rotations")

@AllArgsConstructor
@NoArgsConstructor
public class Rotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="cohort_id")
    private Cohort cohort;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="department_id", nullable = false)
    private Department department;

    @ManyToMany(mappedBy = "rotations")
    private List<User> interns;

    @OneToMany(mappedBy = "rotation", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceReview> performanceReviews;

}
