package com.jfahey.notesdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jfahey.notesdemo.model.Note;
import com.jfahey.notesdemo.model.User;
import com.jfahey.notesdemo.repository.NoteRepository;
import com.jfahey.notesdemo.repository.UserRepository;

@Component
public class NotesDemoDBLoader implements CommandLineRunner {

	private final UserRepository userRepository;
	private final NoteRepository noteRepository;

    private final PasswordEncoder encoder;

	@Autowired
	public NotesDemoDBLoader(UserRepository userRepository,
		NoteRepository noteRepository,
        PasswordEncoder encoder) {

		this.userRepository = userRepository;
		this.noteRepository = noteRepository;
        this.encoder = encoder;
	}

	@Override
	public void run(String... strings) throws Exception {
		User user1 = this.userRepository.save(
			new User("admin","admin@gmail.com", encoder.encode("password123")));
		this.noteRepository.save(
			new Note("Welcome Greeting", user1.getUsername(), "Hello! This is a note."));
	}
}
