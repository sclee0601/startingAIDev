package com.app.stock.controller;

import com.app.stock.dto.UserDto;
import com.app.stock.entity.User;
import com.app.stock.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto request) {
        User user = new User(request.getUsername(), request.getEmail());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(new UserDto(saved.getId(), saved.getUsername(), saved.getEmail()));
    }
}
