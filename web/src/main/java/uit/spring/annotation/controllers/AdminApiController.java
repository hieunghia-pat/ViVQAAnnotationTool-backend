package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.User;
import uit.spring.annotation.interfaces.UserInterface;
import uit.spring.annotation.repositories.UserRepository;
import uit.spring.annotation.repositories.UserSubsetRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static uit.spring.annotation.security.UserRole.*;
import static uit.spring.annotation.utils.Mappings.*;

@Slf4j
@RestController
@RequestMapping(ADMIN_API)
@CrossOrigin
public class AdminApiController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSubsetRepository userSubsetRepository;

    @GetMapping(GET)
    public ResponseEntity<Object> getAllAdmins() {
        List<User> admins = userRepository.findByRole(ADMIN.getRole());

        List<UserInterface> adminInterfaces = new ArrayList<>();
        for (User admin: admins)
            adminInterfaces.add(new UserInterface(admin));

        return ResponseEntity.ok().body(adminInterfaces);
    }

    @GetMapping(GET + "/{adminName}")
    public ResponseEntity<Object> getAllAdmins(@PathVariable("adminName") String adminName) {
        Optional<User> admin = userRepository.findByUsername(adminName);

        if (admin.isEmpty())
            return ResponseEntity
                    .internalServerError()
                    .body(String.format("Cannot find admin with username %s", adminName));

        return ResponseEntity
                .ok()
                .body(new UserInterface(admin.get()));
    }

    @PostMapping(ADD)
    public ResponseEntity<Object> registerNewAdmin(@RequestBody User admin) {
        try {
            userRepository.save(admin);
        }
        catch (RuntimeException exception) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(String.format("%s. Failed to register new admin", exception.getMessage()));
        }

        return ResponseEntity
                .ok()
                .body(String.format("Registered %s successfully", admin.getUsername()));
    }

    @PutMapping(path = UPDATE + "/{adminName}")
    public ResponseEntity<Object> updateAdmin(@PathVariable("adminName") String adminName, @RequestBody User admin) {
        User currentAdmin = userRepository.findByUsername(adminName).get();
        try {
            userRepository.delete(currentAdmin);
            userRepository.save(admin);
        }
        catch (RuntimeException exception) {
            if (!(userRepository.existsById(currentAdmin.getId()))) {
                userRepository.save(currentAdmin); // rollback transaction
            }

            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(String.format("%s. Failed to update new admin", exception.getMessage()));
        }

        return ResponseEntity
                .ok().
                body(String.format("Updated admin %s successfully", adminName));
    }

    @DeleteMapping(path = DELETE + "/{adminName}")
    public ResponseEntity<Object> deleteAdmin(@PathVariable("adminName") String adminName) {
        User userToDelete = userRepository.findByUsername(adminName).get();
        try {
            userRepository.delete(userToDelete);
        }
        catch (RuntimeException exception) {
            if (!(userRepository.existsById(userToDelete.getId()))) {
                userRepository.save(userToDelete); // rollback transaction
            }

            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(String.format("%s. Failed to delete admin %s", exception.getMessage(), adminName));
        }

        return ResponseEntity
                .ok()
                .body(String.format("Deleted admin %s successfully", adminName));
    }
}
