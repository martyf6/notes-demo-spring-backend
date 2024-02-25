package com.jfahey.notesdemo.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfahey.notesdemo.dto.NoteRequest;
import com.jfahey.notesdemo.model.Note;
import com.jfahey.notesdemo.model.User;
import com.jfahey.notesdemo.repository.NoteRepository;
import com.jfahey.notesdemo.repository.UserRepository;
import com.jfahey.notesdemo.security.JwtProvider;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class NotesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void setup(){
        userRepository.deleteAll();
        noteRepository.deleteAll();
    }

    @Test
    public void givenNote_whenGetNoteById_thenReturnNote() throws Exception {
        
        // given: user and user's existing note
        User user = generateTestUser();
        String userToken = generateTestUserToken(user);

        Note note = generateTestNote("Title 1", "Content 1", user);

        // when: request note by id
        ResultActions response = this.mockMvc.perform(get("/notes/get/{id}", note.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken));

        // then: verify note successfully returned
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(note.getTitle())))
                .andExpect(jsonPath("$.content", is(note.getContent())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.lastUpdated", is(not(emptyString()))));
    }
    
    @Test
    public void givenMultipleNotes_whenGetAllNotes_thenReturnAllNotes() throws Exception {

        // given: user and user's multiple notes
        User user = generateTestUser();
        String userToken = generateTestUserToken(user);

        List<Note> notes = generateTestNotes(3, user);
        
        // when: request all user notes
        ResultActions response = this.mockMvc.perform(get("/notes/all")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken));

        // then: verify all notes returned successfully
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(notes.size())))
                .andExpect(jsonPath("$[0].title", is(notes.get(0).getTitle())))
                .andExpect(jsonPath("$[1].username", is(notes.get(1).getUsername())))
                .andExpect(jsonPath("$[2].content", is(notes.get(2).getContent())));
    }

    @Test
    public void givenNote_whenCreateNote_thenReturnSavedNote() throws Exception {

        // given: user and new note request
        User user = generateTestUser();
        String userToken = generateTestUserToken(user);

        NoteRequest note = new NoteRequest("Test Title", "Test Content");

        // when: request to create new note
        ResultActions response = this.mockMvc.perform(post("/notes")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(note)));

        // then: verify note successfully created
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(note.getTitle())))
                .andExpect(jsonPath("$.content", is(note.getContent())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.lastUpdated", is(not(emptyString()))));
    }

    @Test
    public void givenNote_whenUpdateNote_thenReturnSavedNote() throws Exception {

        // given: user and user's existing note
        User user = generateTestUser();
        String userToken = generateTestUserToken(user);

        Note note = generateTestNote("Test Title", "Test Content", user);

        // user's update/modification to existing note
        NoteRequest noteUpdate = new NoteRequest("Updated Title", "Updated Content");

        // when: request update to user's existing note
        ResultActions response = this.mockMvc.perform(put("/notes/update/{id}", note.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(noteUpdate)));

        // then: verify note is successfully updated
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(noteUpdate.getTitle())))
                .andExpect(jsonPath("$.content", is(noteUpdate.getContent())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.lastUpdated", is(not(emptyString()))));
    }

    @Test
    public void givenNote_whenUpdateUnauthorizedNote_thenNotFound() throws Exception {

        // given: user1, user2
        User user1 = generateTestUser("user1","user1@domain.com","password1");
        User user2 = generateTestUser("user2","user2@domain.com","password2");
        String user2Token = generateTestUserToken(user2);
        // given: user1's existing note
        Note user1Note = generateTestNote("Test Title", "Test Content", user1);
        
        // given: user2's update to user1's existing note
        NoteRequest noteUpdate = new NoteRequest("Updated Title", "Updated Content");

        // when: request by user2 to update user1's note
        ResultActions response = this.mockMvc.perform(put("/notes/update/{id}", user1Note.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2Token)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(noteUpdate)));

        // then: verify note not found
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNote_whenDeleteNote_thenOk() throws Exception {

        // given: user and user's existing note
        User user = generateTestUser();
        String userToken = generateTestUserToken(user);

        Note note = generateTestNote("Test Title", "Test Content", user);

        // when: request to delete note
        ResultActions response = this.mockMvc.perform(delete("/notes/delete/{id}", note.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .with(csrf()));

        // then: verify note successfully deleted response
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Note successfully deleted")));
    }

    @Test
    public void givenNote_whenDeleteUnauthorizedNote_thenNotFound() throws Exception {

        // given: user1 and user2
        User user1 = generateTestUser("user1","user1@domain.com","password1");
        User user2 = generateTestUser("user2","user2@domain.com","password2");
        String user2Token = generateTestUserToken(user2);
        // given: user1's existing note
        Note user1Note = generateTestNote("Test Title", "Test Content", user1);

        // when: request from user2 to delete user1's existing note
        ResultActions response = this.mockMvc.perform(delete("/notes/delete/{id}", user1Note.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2Token)
            .with(csrf()));

        // then: verify note not found
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    private User generateTestUser(){
        return generateTestUser("user1", "user1@domain.com", "password1");
    }

    private User generateTestUser(String username, String email, String pw){
        return userRepository.save(new User(
            username,
            email,
            passwordEncoder.encode(pw)
        ));
    }

    private String generateTestUserToken(User user){
        return jwtProvider.generateToken(user.getUsername());
    }

    private Note generateTestNote(String title, String content, User user){
        return noteRepository.save(
            new Note(title, user.getUsername(), content));
    }

    private List<Note> generateTestNotes(int count, User user){
        List<Note> testNotes = new ArrayList<Note>();
        for(int i = 1; i <= count; i++){
            testNotes.add(new Note(
                "Test Note " + i, 
                user.getUsername(),
                "Test Note Content " + i
            ));
        }
        return noteRepository.saveAll(testNotes);
    }
}
