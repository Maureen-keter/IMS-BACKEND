package com.stanbic.internMs.intern.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name="cohorts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cohort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Instant createdAt;

    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy ="cohort", cascade = CascadeType.ALL)
    private List<User> interns;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }
}
