package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.Subset;
import uit.spring.annotation.databases.User;
import uit.spring.annotation.databases.UserSubset;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.ResponseInterface;
import uit.spring.annotation.interfaces.SubsetInterface;
import uit.spring.annotation.interfaces.UserSubsetInterface;
import uit.spring.annotation.repositories.SubsetRepository;
import uit.spring.annotation.repositories.UserRepository;
import uit.spring.annotation.repositories.UserSubsetRepository;

import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static uit.spring.annotation.utils.Mappings.*;

@Slf4j
@RestController
@RequestMapping(SUBSETS_API)
@CrossOrigin
public class SubsetApiController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSubsetRepository userSubsetRepository;
    @Autowired
    private SubsetRepository subsetRepository;

    @GetMapping(GET)
    public ResponseEntity<Object> getSubsets() {
        List<Subset> subsets = subsetRepository.findAll();

        List<SubsetInterface> subsetInterfaces = new ArrayList<>();
        for (Subset subset: subsets) {
            subsetInterfaces.add(new SubsetInterface(subset));
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                subsetInterfaces
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping(GET + "/subset" + "/{subsetId}")
    public ResponseEntity<Object> getSubset(@PathVariable("subsetId") Long subsetId) {
        Optional<Subset> optionalSubset = subsetRepository.findById(subsetId);
        if (optionalSubset.isEmpty()) {
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    String.format("Cannot found subset %o", subsetId)
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        Subset subset = optionalSubset.get();

        ResponseInterface response = new ResponseInterface(
                OK,
                new SubsetInterface(subset)
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping(GET + "/annotator" + "/{annotatorName}")
    public ResponseEntity<Object> getSubsetByAnnotator(@PathVariable("annotatorName") String annotatorName) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(annotatorName);

        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", annotatorName);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }

        User annotator = optionalAnnotator.get();
        List<Subset> subsets = subsetRepository.findAll();
        List<UserSubsetInterface> userSubsetInterfaces = new ArrayList<>();
        for (Subset subset: subsets) {
            Optional<UserSubset> optionalUserSubset = userSubsetRepository.findByUserIdAndSubsetId(annotator.getId(), subset.getId());
            if (optionalUserSubset.isPresent()) { // this subset was assigned to this user
                UserSubset userSubset = optionalUserSubset.get();
                UserSubsetInterface newUserSubsetInterface = new UserSubsetInterface(userSubset);
                newUserSubsetInterface.setAssigned(true);
                userSubsetInterfaces.add(newUserSubsetInterface);
            }
            else {
                List<UserSubset> userSubsetsBySubset = userSubsetRepository.findBySubsetId(subset.getId());
                if (userSubsetsBySubset.size() == 0) { // this subset was not assigned to anyone
                UserSubsetInterface newUserSubsetInterface = new UserSubsetInterface(subset);
                newUserSubsetInterface.setAssigned(false);
                newUserSubsetInterface.setUserId(annotator.getId());
                userSubsetInterfaces.add(newUserSubsetInterface);
                }
            }
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                userSubsetInterfaces
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @PostMapping(ASSIGNMENT + ADD)
    public ResponseEntity<Object> addAssignment(@RequestBody UserSubsetInterface userSubsetInterface) {
        Optional<User> optionalAnnotator = userRepository.findById(userSubsetInterface.getUserId());
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot found annotator with id %s", userSubsetInterface.getUserId().toString()); 
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

        Optional<Subset> optionalSubset = subsetRepository.findById(userSubsetInterface.getSubsetId());
        if (optionalSubset.isEmpty()) {
            String message = String.format("Cannot found subset with id %s", userSubsetInterface.getSubsetId().toString());
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(OK)
                    .body(response);
        }
        Subset subset = optionalSubset.get();

        UserSubset userSubset = new UserSubset(
            annotator,
            subset,
            userSubsetInterface.getAssignedDate(),
            userSubsetInterface.getFinishDate(),
            userSubsetInterface.isValidation()
        );

        try {
            log.info(String.format("Adding assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId()));
            userSubsetRepository.save(userSubset);
        }
        catch (RuntimeException saveException) {
            log.info(saveException.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    String.format("Failed to add assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId())
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        String message = String.format("Added successfully assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId());
        log.info(message);
        ResponseInterface response = new ResponseInterface(
                OK,
                message
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @PutMapping(ASSIGNMENT + UPDATE + "/{assignmentId}")
    public ResponseEntity<Object> updateUserSubset(@PathVariable("assignmentId") UUID assignmentId, @RequestBody UserSubsetInterface userSubsetInterface) {
        Optional<User> optionalAnnotator = userRepository.findById(userSubsetInterface.getUserId());
        if (optionalAnnotator.isEmpty())
        {
            String message = String.format("Cannot find annotator with id %s", userSubsetInterface.getUserId());
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

        Optional<Subset> optionalSubset = subsetRepository.findById(userSubsetInterface.getSubsetId());
        if (optionalSubset.isEmpty())
        {
            String message = String.format("Cannot find subset id %s", userSubsetInterface.getSubsetId());
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        Subset subset = optionalSubset.get();

        Optional<UserSubset> optionalUserSubset = userSubsetRepository.findById(assignmentId);
        if (optionalUserSubset.isEmpty()) {
            String message = String.format("Cannot find any assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId());
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }

        try {
            log.info(String.format("Updating assignment for annotator %s with subset %o",
                    annotator.getUsername(), userSubsetInterface.getSubsetId()));
            userSubsetRepository.updateById(
                    assignmentId,
                    annotator.getId(),
                    subset.getId(),
                    userSubsetInterface.getAssignedDate(),
                    userSubsetInterface.getFinishDate(),
                    userSubsetInterface.isValidation()
            );
        }
        catch(RuntimeException exception) {
            log.info(exception.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    String.format("Cannot update assignment for annotator %s with subset %o",
                            annotator.getUsername(), userSubsetInterface.getSubsetId())
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        String message = String.format("Updated assignment successfully for annotator %s with subset %o",
                annotator.getUsername(), userSubsetInterface.getSubsetId());
        log.info(message);
        ResponseInterface response = new ResponseInterface(
                OK,
                message
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @DeleteMapping(ASSIGNMENT + DELETE + "/{assignmentId}")
    ResponseEntity<Object> deleteUserSubset(@PathVariable("assignmentId") UUID assignmentId) {
        Optional<UserSubset> optionalUserSubset = userSubsetRepository.findById(assignmentId);
        if (optionalUserSubset.isEmpty()) {
            String message = "There is no such assignment to delete";
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        UserSubset userSubset = optionalUserSubset.get();

        Optional<User> optionalAnnotator = userRepository.findById(userSubset.getUser().getId());
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator with id %s", userSubset.getUser().getId());
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
            log.info(String.format("Deleting assignment for annotator %s with subset %o",
                    annotator.getUsername(), userSubset.getSubset().getId()));
            userSubsetRepository.delete(userSubset);
        }
        catch(RuntimeException deleteException) {
            log.info(deleteException.getMessage());
            String message = String.format("Cannot remove assignment for annotator %s with subset %o",
                    annotator.getUsername(), userSubset.getSubset().getId());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    message
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        String message = String.format("Deleted successfully assignment for annotator %s with subset %o",
                annotator.getUsername(), userSubset.getSubset().getId());
        log.info(message);
        ResponseInterface response = new ResponseInterface(
                OK,
                message
        );
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(response);
    }

}
