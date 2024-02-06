package com.jfahey.notesdemo.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jfahey.notesdemo.model.Note;
import com.jfahey.notesdemo.repository.NoteRepository;
import com.jfahey.notesdemo.service.NotesService;

@Service
public class NotesServiceImpl implements NotesService {

    private NoteRepository noteRepository;

    public NotesServiceImpl(NoteRepository noteRepository){
        this.noteRepository = noteRepository;
    }

    @Override
    public Optional<Note> getNoteById(long id) {
        return noteRepository.findById(id);
    }

    @Override
    public List<Note> getNotesByUsername(String username) {
        return noteRepository.findByUsername(username);
    }

    @Override
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    @Override
    public void deleteNote(Note note) {
        noteRepository.delete(note);
    }
    
}
