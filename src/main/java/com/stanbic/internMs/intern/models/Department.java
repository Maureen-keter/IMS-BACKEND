package com.stanbic.internMs.intern.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="departments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<User> users;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Rotation> rotations;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Cohort> cohorts;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
