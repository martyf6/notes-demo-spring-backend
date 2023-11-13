package com.jfahey.notesdemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jfahey.notesdemo.model.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {
    
    List<Note> findByUsername(String username);

    List<Note> findByIdAndUsername(Long id, String username);
    
}
