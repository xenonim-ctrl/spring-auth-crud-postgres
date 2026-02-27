package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Создаем администратора по умолчанию, если его еще нет
        if (!userRepository.existsByUsername("admino")) {
            User admin = new User();
            admin.setUsername("admino");
            admin.setPassword(passwordEncoder.encode("meadmin123"));
            admin.setRole(Role.ADMIN);
            admin.setFullName("Администратор");
            admin.setPhone("+79999999999");
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            System.out.println("Администратор по умолчанию создан: admino / meadmin123");
        }
    }
}

