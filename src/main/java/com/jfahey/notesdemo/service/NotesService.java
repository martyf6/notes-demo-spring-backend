package com.jfahey.notesdemo.service;

import java.util.List;
import java.util.Optional;

import com.jfahey.notesdemo.model.Note;

public interface NotesService {
    Optional<Note> getNoteById(long id);
    List<Note> getNotesByUsername(String username);
    Note saveNote(Note note);
    void deleteNote(Note note);
}
