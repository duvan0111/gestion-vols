package com.bluesky.gestionvols.service;

import com.bluesky.gestionvols.model.User;
import com.bluesky.gestionvols.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initDefaultUsers() {
        // Création de l'utilisateur hôtesse si non existant
        Optional<User> hostessOpt = userRepository.findByEmail("hostess@bluesky.com");
        if (hostessOpt.isEmpty()) {
            User hostess = new User();
            hostess.setName("Hôtesse");
            hostess.setEmail("hostess@bluesky.com");
            hostess.setPassword(passwordEncoder.encode("password"));
            hostess.setRole("HOSTESS");
            userRepository.save(hostess);
        }

        // Création de l'utilisateur responsable de vols si non existant
        Optional<User> managerOpt = userRepository.findByEmail("manager@bluesky.com");
        if (managerOpt.isEmpty()) {
            User manager = new User();
            manager.setName("Responsable de vols");
            manager.setEmail("manager@bluesky.com");
            manager.setPassword(passwordEncoder.encode("password"));
            manager.setRole("MANAGER");
            userRepository.save(manager);
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
    }
}
