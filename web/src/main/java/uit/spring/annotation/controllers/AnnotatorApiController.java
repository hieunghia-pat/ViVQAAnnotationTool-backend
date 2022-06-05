package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.*;
import uit.spring.annotation.interfaces.ImageInterface;
import uit.spring.annotation.interfaces.UserInterface;
import uit.spring.annotation.repositories.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        return ResponseEntity.ok().body(annotatorInterfaces);
    }

    @GetMapping(GET + "/{annotatorName}")
    public ResponseEntity<Object> getAnnotator(@PathVariable("annotatorName") String annotatorName) {
        Optional<User> annotator = userRepository.findByUsername(annotatorName);

        if (annotator.isEmpty())
            return ResponseEntity
                    .internalServerError()
                    .body(String.format("Cannot find annotator %s! Make sure this annotator is available", annotatorName));

        return ResponseEntity
                .ok()
                .body(new UserInterface(annotator.get()));
    }

    @GetMapping(GET + IMAGES + "/{annotationName}")
    public ResponseEntity<Object> getImages(@PathVariable("annotationName") String annotationName) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(annotationName);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot not find annotator %s", annotationName);
            log.info(message);
            return ResponseEntity.ok().body(message);
        }
        User annotator = optionalAnnotator.get();

        List<UserSubset> userSubsets = userSubsetRepository.findByUserId(annotator.getId());
        List<ImageInterface> imageInterfaces = new ArrayList<>();
        for(UserSubset userSubset: userSubsets) {
            Optional<Subset> optionalSubset = subsetRepository.findById(userSubset.getSubset().getId());
            if (optionalSubset.isEmpty()) {
                String message = String.format("Cannot find subset %s", userSubset.getSubset().getId());
                log.info(message);
                return ResponseEntity.internalServerError().body(message);
            }
            Subset subset = optionalSubset.get();
            List<Image> images = subset.getImages();
            for (Image image: images)
                imageInterfaces.add(new ImageInterface(image));
        }

        return ResponseEntity.ok().body(imageInterfaces);
    }

    @PostMapping(ADD)
    public ResponseEntity<Object> registerNewAnnotator(@RequestBody User annotator) {
        try {
            userRepository.save(annotator);
        }
        catch (RuntimeException exception) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("%s. Failed to register new annotator", exception.getMessage()));
        }

        return ResponseEntity
                .ok()
                .body(String.format("Registered %s successfully", annotator.getUsername()));
    }

    @PutMapping(path = UPDATE + "/{annotatorName}")
    public ResponseEntity<Object> updateAnnotator(@PathVariable("annotatorName") String annotatorName, @RequestBody User annotator) {
        User currentAnnotator = userRepository.findByUsername(annotatorName).get();
        try {
            userRepository.delete(currentAnnotator);
            userRepository.save(annotator);
        }
        catch (RuntimeException exception) {
            log.info(exception.getMessage());
            if (!(userRepository.existsById(currentAnnotator.getId()))) {
                userRepository.save(currentAnnotator); // rollback transaction
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("%s. Failed to update new annotator", exception.getMessage()));
        }

        return ResponseEntity
                .ok()
                .body(String.format("Updated admin %s successfully", annotatorName));
    }

    @DeleteMapping(path = DELETE + "{annotatorName}")
    public ResponseEntity<Object> deleteAnnotator(@PathVariable("annotatorName") String annotatorName) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(annotatorName);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", annotatorName);
            log.info(message);
            return ResponseEntity.badRequest().body(message);
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
                return ResponseEntity.internalServerError().body(String.format("Failed to delete assignment %s", assignment.getId()));
            }
        }

        // then delete the annotator
        try {
            userRepository.delete(annotator);
        }
        catch(RuntimeException deleteException) {
            log.info(deleteException.getMessage());
            for (UserSubset assignment: assignments) { // rollback transaction when failed to delete this annotator
                userSubsetRepository.save(assignment);
            }

            return ResponseEntity.internalServerError().body(String.format("Failed to delete annotator %s", annotatorName));
        }

        return ResponseEntity.ok().body(String.format("Deleted annotator %s successfully", annotatorName));
    }
}
