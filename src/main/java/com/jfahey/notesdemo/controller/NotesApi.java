package com.jfahey.notesdemo.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jfahey.notesdemo.dto.NoteRequest;
import com.jfahey.notesdemo.model.Note;
import com.jfahey.notesdemo.repository.NoteRepository;
import com.jfahey.notesdemo.security.exception.ResourceNotFoundException;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/notes")
public class NotesApi {

    @Autowired
    private NoteRepository noteRepository;

    @PostMapping
    public ResponseEntity<Note> create(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody @Valid NoteRequest note) {

        if(userDetails == null) 
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Note newNote = new Note(
            note.getTitle(),
            userDetails.getUsername(),
            note.getContent()
        );
        Note savedNote = noteRepository.save(newNote);
        URI noteURI = URI.create("/notes/get/" + savedNote.getId());
        return ResponseEntity.created(noteURI).body(savedNote);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Note> getNote(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable @RequestBody Long id) {

        return noteRepository.findById(id)
            //.filter(this::isUpdateAuthorized)
            .filter(note -> isUpdateAuthorized(userDetails,note))
            .map(ResponseEntity.ok()::body)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Note> updateNote(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable @RequestBody Long id,
        @RequestBody @Valid NoteRequest noteUpdate) {

        return noteRepository.findById(id)
            //.filter(this::isUpdateAuthorized)
            .filter(note -> isUpdateAuthorized(userDetails,note))
            .map(note -> {
                note.setTitle(noteUpdate.getTitle());
                note.setContent(noteUpdate.getContent());
                note.setLastUpdated(LocalDateTime.now());
                Note newNote = noteRepository.save(note);
                return ResponseEntity.ok().body(newNote);
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNote(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable @RequestBody Long id) {

        return noteRepository.findById(id)
            //.filter(this::isUpdateAuthorized)
            .filter(note -> isUpdateAuthorized(userDetails,note))
            .map(note -> {
                noteRepository.delete(note);
                return ResponseEntity.ok().body("Note successfully deleted.");
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
     
    @GetMapping({"/", "/all"})
    public ResponseEntity<List<Note>> list(@AuthenticationPrincipal UserDetails userDetails) {

        if(userDetails == null)
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

        return new ResponseEntity<>(noteRepository.findByUsername(userDetails.getUsername()), HttpStatus.OK);
    }

    /**
     * Check authorization for note updates based on the user's principal
     * from the supplied <code>UserDetails</code> session object.
     * @param userDetails object from which authorization is determined
     * @param note the note being checked for update authorization
     */
    private boolean isUpdateAuthorized(UserDetails userDetails, Note note){
        return userDetails != null && note.getUsername().equals(userDetails.getUsername());
    }

    /**
     * Check authorization for note updates by statically retrieving the currently
     * authenticated user's principal from {@link #getCurrentUsername()}.
     * <p>
     * (Note: this method uses the <code>SecurityContextHolder</code> to obtain user details.)
     * @param note the note being checked for update authorization
     * @return <code>true</code> if currently authenticated principal is the note owner
     */
    private boolean isUpdateAuthorized(Note note){
        Optional<String> username = getCurrentUsername();
        return username.isPresent() && note.getUsername().equals(username.get());
    }

    /**
     * Statically retrieve the currently authenticated user's username from 
     * the Authentication principal via the {@link SecurityContextHolder}.
     * @return <code>Optional</code> containing the username, empty if no user
     * is currently authenticated or the session is anonymous.
     */
    private Optional<String> getCurrentUsername(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return Optional.of(((UserDetails) authentication.getPrincipal()).getUsername());
        }
        return Optional.empty();
    }
}
