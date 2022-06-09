package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.*;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.ImageInterface;
import uit.spring.annotation.interfaces.ResponseInterface;
import uit.spring.annotation.interfaces.UserInterface;
import uit.spring.annotation.repositories.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;
import static uit.spring.annotation.security.UserRole.ANNOTATOR;
import static uit.spring.annotation.utils.Mappings.*;

@Slf4j
@RestController
@RequestMapping(ANNOTATOR_API)
@CrossOrigin
public class AnnotatorApiController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private SubsetRepository subsetRepository;
    @Autowired
    private UserSubsetRepository userSubsetRepository;

    @GetMapping(GET)
    public ResponseEntity<Object> getAllAnnotators() {
        List<User> annotators = userRepository.findByRole(ANNOTATOR.getRole());

        List<UserInterface> annotatorInterfaces = new ArrayList<>();
        for (User annotator: annotators)
            annotatorInterfaces.add(new UserInterface(annotator));

        ResponseInterface response = new ResponseInterface(
                OK,
                annotatorInterfaces
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping(GET + "/{annotatorName}")
    public ResponseEntity<Object> getAnnotator(@PathVariable("annotatorName") String annotatorName) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(annotatorName);
        if (optionalAnnotator.isEmpty()) {
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    String.format("Cannot find annotator %s! Make sure this annotator is available", annotatorName)
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        User annotator = optionalAnnotator.get();

        ResponseInterface response = new ResponseInterface(
                OK,
                new UserInterface(annotator)
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping(GET + IMAGES + "/{annotationName}")
    public ResponseEntity<Object> getImages(@PathVariable("annotationName") String annotationName) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(annotationName);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot not find annotator %s", annotationName);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        User annotator = optionalAnnotator.get();

        List<UserSubset> userSubsets = userSubsetRepository.findByUserId(annotator.getId());
        List<ImageInterface> imageInterfaces = new ArrayList<>();
        for(UserSubset userSubset: userSubsets) {
            Optional<Subset> optionalSubset = subsetRepository.findById(userSubset.getSubset().getId());
            if (optionalSubset.isEmpty()) {
                String message = String.format("Cannot find subset %s", userSubset.getSubset().getId());
                log.info(message);
                log.info(message);
                ErrorInterface response = new ErrorInterface(
                        NOT_FOUND,
                        message
                );
                return ResponseEntity.status(NOT_FOUND).body(response);
            }
            Subset subset = optionalSubset.get();
            List<Image> images = subset.getImages();
            for (Image image: images)
                imageInterfaces.add(new ImageInterface(image));
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                imageInterfaces
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @PostMapping(ADD)
    public ResponseEntity<Object> registerNewAnnotator(@RequestBody User annotator) {
        try {
            userRepository.save(annotator);
        }
        catch (RuntimeException exception) {
            log.info(exception.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    String.format("%s. Failed to register new annotator", exception.getMessage())
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                String.format("Registered %s successfully", annotator.getUsername())
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @PutMapping(path = UPDATE + "/{annotatorId}")
    public ResponseEntity<Object> updateAnnotator(@PathVariable("annotatorId") UUID annotatorId, @RequestBody UserInterface annotatorInterface) {
        Optional<User> optionalAnnotator = userRepository.findById(annotatorId);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator with id %s", annotatorId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        User annotator = optionalAnnotator.get();

        try {
            log.info(String.format("Updating annotator with id %s", annotatorId));
            userRepository.updateById(
                    annotatorInterface.getId(),
                    annotatorInterface.getUsername(),
                    annotatorInterface.getFirstname(),
                    annotatorInterface.getLastname(),
                    annotatorInterface.getPassword() == null ? annotator.getPassword() : annotatorInterface.getPassword());
        }
        catch (RuntimeException exception) {
            log.info(exception.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    String.format("Failed to update annotator with id %s", annotatorId)
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        String message = String.format("Updated annotator with id %s successfully", annotatorId);
        log.info(message);
        ResponseInterface response = new ResponseInterface(
                OK,
                String.format("Updated annotator with id %s successfully", annotatorId)
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @DeleteMapping(path = DELETE + "/{annotatorName}")
    public ResponseEntity<Object> deleteAnnotator(@PathVariable("annotatorName") String annotatorName) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(annotatorName);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", annotatorName);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        User annotator = optionalAnnotator.get();

        // delete all assignments belong to this annotator
        List<UserSubset> assignments = userSubsetRepository.findByUserId(annotator.getId());
        for (UserSubset assignment: assignments) {
            try {
                userSubsetRepository.delete(assignment);
            }
            catch(RuntimeException deleteException) {
                log.info(deleteException.getMessage());
                ErrorInterface response = new ErrorInterface(
                        INTERNAL_SERVER_ERROR,
                        String.format("Failed to delete assignment %s", assignment.getId())
                );
                return ResponseEntity
                        .status(INTERNAL_SERVER_ERROR)
                        .body(response);
            }
        }

        // then delete the annotator
        try {
            userRepository.delete(annotator);
        }
        catch(RuntimeException deleteException) {
            log.info(deleteException.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    String.format("Failed to delete annotator %s", annotatorName)
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                String.format("Deleted annotator %s successfully", annotatorName)
        );
        return ResponseEntity.ok().body(response);
    }
}
