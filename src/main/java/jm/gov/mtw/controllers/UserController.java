package jm.gov.mtw.controllers;

import jm.gov.mtw.models.User;
import jm.gov.mtw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsById(user.getTrn())) {
            return ResponseEntity.badRequest().body("User already exists with this TRN.");
        }
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully.");
    }
}
