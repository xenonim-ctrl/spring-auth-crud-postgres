package com.example.demo.controller;

import com.example.demo.model.Pet;
import com.example.demo.model.User;
import com.example.demo.repository.PetRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pets")
public class PetController {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listPets(Model model, Authentication authentication) {
        // Получаем текущего пользователя
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Проверяем роль пользователя
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        // Передаем полное имя пользователя для приветствия
        model.addAttribute("userFullName", currentUser.getFullName());
        
        if (isAdmin) {
            // ADMIN видит все записи
            model.addAttribute("pets", petRepository.findAll());
        } else {
            // USER видит только свои записи
            model.addAttribute("pets", petRepository.findByUserId(currentUser.getId()));
        }
        return "list";
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminGetAllRecords(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("pets", petRepository.findAll());
        model.addAttribute("userFullName", currentUser.getFullName());
        return "list";
    }

    @GetMapping("/new")
    public String showForm(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Pet pet = new Pet();
        pet.setOwnerName(currentUser.getFullName()); // Автоматически заполняем имя владельца
        model.addAttribute("pet", pet);
        model.addAttribute("isNew", true); // Флаг для определения, что это новая запись
        return "form";
    }

    @PostMapping
    public String savePet(@ModelAttribute Pet pet, Authentication authentication) {
        // Если это обновление существующего питомца (id не null), проверяем права доступа
        if (pet.getId() != null) {
            // Проверяем, что пользователь имеет роль ADMIN для обновления
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new RuntimeException("У вас нет прав для редактирования питомцев");
            }
            // Проверяем существование записи
            if (!petRepository.existsById(pet.getId())) {
                throw new IllegalArgumentException("Питомец с ID " + pet.getId() + " не найден");
            }
        } else {
            // При создании нового питомца устанавливаем владельца
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            pet.setUser(currentUser);
        }
        petRepository.save(pet);
        return "redirect:/pets";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editPet(@PathVariable Long id, Model model) {
        Pet pet = petRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Неверный ID: " + id));
        model.addAttribute("pet", pet);
        model.addAttribute("isNew", false); // Флаг для определения, что это редактирование
        return "form";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletePet(@PathVariable Long id) {
        petRepository.deleteById(id);
        return "redirect:/pets";
    }
}