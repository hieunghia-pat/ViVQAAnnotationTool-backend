package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.User;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.ResponseInterface;
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

        ResponseInterface response = new ResponseInterface();
        response.setBody(adminInterfaces);
        response.setStatus(OK.value());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(GET + "/{adminName}")
    public ResponseEntity<Object> getAllAdmins(@PathVariable("adminName") String adminName) {
        Optional<User> admin = userRepository.findByUsername(adminName);

        if (admin.isEmpty()) {
            ErrorInterface response = new ErrorInterface();
            response.setError(String.format("Cannot find admin with username %s", adminName));
            response.setStatus(INTERNAL_SERVER_ERROR.value());
            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }

        ResponseInterface response = new ResponseInterface();
        response.setStatus(OK.value());
        response.setBody(new UserInterface(admin.get()));
        return ResponseEntity
                .ok()
                .body(response);
    }

    @PostMapping(ADD)
    public ResponseEntity<Object> registerNewAdmin(@RequestBody User admin) {
        try {
            userRepository.save(admin);
        }
        catch (RuntimeException exception) {
            ErrorInterface response = new ErrorInterface();
            response.setStatus(INTERNAL_SERVER_ERROR.value());
            response.setError(String.format("%s. Failed to register new admin", exception.getMessage()));
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        ResponseInterface response = new ResponseInterface();
        response.setStatus(OK.value());
        response.setBody(String.format("Registered %s successfully", admin.getUsername()));
        return ResponseEntity
                .ok()
                .body(response);
    }

    @PutMapping(path = UPDATE + "/{adminName}")
    public ResponseEntity<Object> updateAdmin(@PathVariable("adminName") String adminName, @RequestBody UserInterface adminInterface) {
        Optional<User> optionalAdmin = userRepository.findByUsername(adminName);
        if (optionalAdmin.isEmpty()) {
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND.value(),
                    String.format("Cannot find admin %s", adminName)
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }

        try {
            userRepository.updateById(
                    adminInterface.getId(),
                    adminInterface.getUsername(),
                    adminInterface.getFirstname(),
                    adminInterface.getLastname(),
                    adminInterface.getPassword()
            );
        }
        catch (RuntimeException exception) {
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR.value(),
                    String.format("Failed to update admin %s", adminName)
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        ResponseInterface response = new ResponseInterface(
                OK.value(),
                String.format("Updated admin %s successfully", adminName)
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @DeleteMapping(path = DELETE + "/{adminName}")
    public ResponseEntity<Object> deleteAdmin(@PathVariable("adminName") String adminName) {
        Optional<User> optionalAdmin = userRepository.findByUsername(adminName);
        if (optionalAdmin.isEmpty()) {
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND.value(),
                    String.format("Cannot find admin %s", adminName)
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        User admin = optionalAdmin.get();

        try {
            userRepository.delete(admin);
        }
        catch (RuntimeException exception) {
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR.value(),
                    String.format("%s. Failed to delete admin %s", exception.getMessage(), adminName)
            );

            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        ResponseInterface response = new ResponseInterface(
                OK.value(),
                String.format("Deleted admin %s successfully", adminName)
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }
}
