package com.jfahey.notesdemo.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfahey.notesdemo.model.Note;
import com.jfahey.notesdemo.security.JwtFilter;
import com.jfahey.notesdemo.security.JwtProvider;
import com.jfahey.notesdemo.security.UserDetailsServiceProvider;
import com.jfahey.notesdemo.service.NotesService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@Import({JwtFilter.class, JwtProvider.class})
@WebMvcTest(NotesApi.class)
public class NotesApiTest {
     
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotesService notesService;

    @MockBean
    private UserDetailsServiceProvider userDetailsServiceProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username="user")
    public void givenNoteId_whenGetNoteById_thenReturnNote() throws Exception {

        long noteId = 1l;
        Note note = new Note("Title", "user", "Content");

        // given: (mock) existing user note
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));

        // when: request note by id
        ResultActions response = this.mockMvc.perform(get("/notes/get/{id}", noteId));

        // then: verify note successfully returned
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(note.getTitle())))
                .andExpect(jsonPath("$.content", is(note.getContent())));
    }

    @Test
    @WithMockUser(username="user")
    public void givenInvalidNoteId_whenGetNoteById_thenReturnEmpty() throws Exception {

        long noteId = 1l;
        //Note note = new Note("Title", "user", "Content");

        // given: (mock) no existing note for user
        when(notesService.getNoteById(noteId)).thenReturn(Optional.empty());

        // when: request note by id
        ResultActions response = mockMvc.perform(get("/notes/get/{id}", noteId));

        // then: verify not found returned
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="user")
    public void givenNotes_whenGetAllNotes_thenReturnNotes() throws Exception {

        Note note1 = new Note("Title1", "user", "Content1");
        Note note2 = new Note("Title2", "user", "Content2");
        Note note3 = new Note("Title3", "user", "Content3");
        List<Note> notes = List.of(note1, note2, note3);

        // given: (mock) existing user's multiple notes
        when(notesService.getNotesByUsername("user")).thenReturn(notes);

        // when: request get all user notes
        ResultActions response = this.mockMvc.perform(get("/notes/all"));

        // then: verify all user notes successfully returned
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",
                            is(notes.size())))
                .andExpect(jsonPath("$[0].title", is(note1.getTitle())))
                .andExpect(jsonPath("$[1].username", is(note2.getUsername())))
                .andExpect(jsonPath("$[2].content", is(note3.getContent())));
    }

    @Test
    @WithMockUser(username="user")
    public void givenNote_whenCreateNote_thenReturnSavedNote() throws Exception {

        Note note = new Note("Title", "user", "Content");

        // given: (mock)
        //when(notesService.saveNote(note)).thenReturn(note);
        when(notesService.saveNote(any(Note.class)))
                .thenAnswer((invocation)-> invocation.getArgument(0));

        // when: request create new note
        ResultActions response = this.mockMvc.perform(post("/notes")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(note)));

        // then: verify new note successfully returned
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(note.getTitle())))
                .andExpect(jsonPath("$.content", is(note.getContent())))
                .andExpect(jsonPath("$.username", is(note.getUsername())))
                .andExpect(jsonPath("$.lastUpdated", is(not(emptyString()))));
    }

    @Tag("validation")
    @Test
    @WithMockUser(username="user")
    public void givenNullNoteTitle_whenCreateNote_thenReturnBadRequest() throws Exception {

        // given: note with null title
        Note note = new Note(null, "user", "Content.");

        // given: (mock)
        //when(notesService.saveNote(note)).thenReturn(note);
        when(notesService.saveNote(any(Note.class)))
                .thenAnswer((invocation)-> invocation.getArgument(0));

        // when: request create new note
        ResultActions response = this.mockMvc.perform(post("/notes")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(note)));

        // then: verify bad request
        response.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="user")
    public void givenNote_whenUpdateNoteId_thenReturnSavedNote() throws Exception {

        long noteId = 1l;
        Note note = new Note("Title", "user", "Content");
        Note updatedNote = new Note("New Title", "user", "New Content");

        // given: (mock) existing user's note
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));
        when(notesService.saveNote(any(Note.class)))
            .thenAnswer((invocation)-> invocation.getArgument(0));

        // when: request update note by id
        ResultActions response = this.mockMvc.perform(put("/notes/update/{id}", noteId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatedNote)));

        // then: verify updated note successfully returned
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updatedNote.getTitle())))
                .andExpect(jsonPath("$.content", is(updatedNote.getContent())))
                .andExpect(jsonPath("$.username", is(updatedNote.getUsername())))
                .andExpect(jsonPath("$.lastUpdated", is(not(emptyString()))));
    }

    @Test
    @WithMockUser(username="user")
    public void givenNote_whenUpdateInvalidNoteId_thenReturnNotFound() throws Exception {

        long noteId = 1l;
        Note note = new Note("Title", "user", "Content");
        long invalidNoteId = 2l;

        // given: (mock) existing user's note
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));
        when(notesService.saveNote(any(Note.class)))
            .thenAnswer((invocation)-> invocation.getArgument(0));

        // when: request update on note with invalid id
        ResultActions response = this.mockMvc.perform(put("/notes/update/{id}", invalidNoteId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(note)));

        // then: verify not found returned
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Tag("validation")
    @Test
    @WithMockUser(username="user")
    public void givenNullNoteTitle_whenUpdateNoteId_thenReturnBadRequest() throws Exception {

        // given: note with null title
        long noteId = 1l;
        Note note = new Note(null, "user", "Content.");

        // given: (mock) existing user's note
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));
        when(notesService.saveNote(any(Note.class)))
            .thenAnswer((invocation)-> invocation.getArgument(0));

        // when: request update on note with null title
        ResultActions response = this.mockMvc.perform(put("/notes/update/{id}", noteId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(note)));

        // then: verify bad request
        response.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="user")
    public void givenNote_whenUpdateUnauthorizedNoteId_thenReturnNotFound() throws Exception {

        long noteId = 1l;
        Note note = new Note("Title", "admin", "Content");

        // given: (mock) existing user note
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));
        when(notesService.saveNote(any(Note.class)))
            .thenAnswer((invocation)-> invocation.getArgument(0));

        // when: request update on another user's note
        ResultActions response = this.mockMvc.perform(put("/notes/update/{id}", noteId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(note)));

        // then: verify not found returned
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="user")
    public void givenNoteId_whenDeleteNoteById_thenReturnSuccess() throws Exception {

        long noteId = 1l;
        Note note = new Note("Title", "user", "Content");

        // given: (mock) existing user note
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));

        // when: request delete by note id
        ResultActions response = mockMvc.perform(delete("/notes/delete/{id}", noteId)
            .with(csrf()));

        // then: verify note successfully deleted
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Note successfully deleted")));
    }

    @Test
    @WithMockUser(username="user")
    public void givenInvalidNoteId_whenDeleteNoteById_thenReturnNotFound() throws Exception {

        long noteId = 1l;
        Note note = new Note("Title", "user", "Content");
        long invalidNoteId = 2l;

        // given: (mock) existing user note
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));

        // when: request delete with invalid note id
        ResultActions response = mockMvc.perform(delete("/notes/delete/{id}", invalidNoteId)
            .with(csrf()));

        // then: verify not found returned
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="user")
    public void givenNoteId_whenDeleteUnauthorizedNoteById_thenReturnNotFound() throws Exception {

        long noteId = 1l;
        Note note = new Note("Title", "admin", "Content");

        // given: (mock) existing note by another user
        when(notesService.getNoteById(noteId)).thenReturn(Optional.of(note));

        // when: request delete on another user's note
        ResultActions response = mockMvc.perform(delete("/notes/delete/{id}", noteId)
            .with(csrf()));

        // then: verify not found returned
        response.andDo(print())
                .andExpect(status().isNotFound());
    }
}
