package com.example.demo.service;

import com.example.demo.dto.UserRegistrationDTO;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        // Валидация телефона (русский формат)
        String phone = registrationDTO.getPhone().trim();
        if (!phone.matches("^(\\+7|8)[0-9]{10}$")) {
            throw new RuntimeException("Телефон должен начинаться с +7 или 8 и содержать 11 цифр");
        }

        // Нормализация телефона (приводим к формату +7XXXXXXXXXX)
        if (phone.startsWith("8")) {
            phone = "+7" + phone.substring(1);
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(Role.USER); // Всегда USER для новых регистраций
        user.setFullName(registrationDTO.getFullName());
        user.setPhone(phone);
        user.setEmail(registrationDTO.getEmail().trim().toLowerCase());

        return userRepository.save(user);
    }
}

