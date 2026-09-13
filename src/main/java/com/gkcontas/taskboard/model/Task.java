package com.gkcontas.taskboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Task(String title) {
        this.title = title;
        this.completed = false;
        this.createdAt = Instant.now();
    }

    public void complete() {
        this.completed = true;
    }
}
