package com.jfahey.notesdemo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "notes")
public class Note {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
     
    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 50)
    private String username;

    @Lob
    @Column
    private String content;

    @Column
    private LocalDateTime lastUpdated;
    
    public Note() {}

    public Note(String title, String username, String content){
        this.title = title;
        this.username = username;
        this.content = content;
        this.lastUpdated = LocalDateTime.now();
    }

    public Note(String title, String username, String content, LocalDateTime lastUpdated){
        this.title = title;
        this.username = username;
        this.content = content;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
