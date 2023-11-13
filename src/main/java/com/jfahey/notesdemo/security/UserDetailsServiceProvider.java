package com.jfahey.notesdemo.security;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jfahey.notesdemo.model.User;
import com.jfahey.notesdemo.repository.UserRepository;

@Service
public class UserDetailsServiceProvider implements UserDetailsService {

  @Autowired
  UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    
    //TODO: create UserDetails adapter class
    //return UserDetailsAdapter.build(user);

    // for now, we'll use spring security's User to represent the UserDetails principal within authentication
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPassword(),
        true, true, true, true,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }
}
