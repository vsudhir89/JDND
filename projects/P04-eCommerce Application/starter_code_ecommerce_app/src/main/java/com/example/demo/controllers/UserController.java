package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @GetMapping("/id/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return ResponseEntity.of(userRepository.findById(id));
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> findByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        boolean isPasswordValid = validatePassword(createUserRequest.getPassword(), createUserRequest.getConfirmPassword());
        if (isPasswordValid) {
            user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
            Cart cart = new Cart();
            cartRepository.save(cart);
            user.setCart(cart);
            userRepository.save(user);
            // TODO: add success log message
            return ResponseEntity.ok(user);
        }
        // TODO: add failure log message
        return ResponseEntity.badRequest().build();
    }

    private boolean validatePassword(String password, String confirmPassword) {
        // not null or empty for both fields and must contain 8 chars and 1 special char
        if (password != null && confirmPassword != null && !password.isEmpty() && !confirmPassword.isEmpty()) {
            Pattern passwordPattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            boolean isPasswordEqualToConfirmPassword = password.equals(confirmPassword);
            if (password.length() >= 8 && isPasswordEqualToConfirmPassword) {
                return passwordPattern.matcher(password).find();
            }
            return false;
        }
        return false;
    }
}
