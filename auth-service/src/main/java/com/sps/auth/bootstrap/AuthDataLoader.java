package com.sps.auth.bootstrap;

import com.sps.auth.entity.UserEntity;
import com.sps.auth.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthDataLoader implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            userRepository.save(new UserEntity("admin", passwordEncoder.encode("Admin123!"), "ADMIN"));
            userRepository.save(new UserEntity("cliente", passwordEncoder.encode("Cliente123!"), "USER"));
        }
    }
}
