package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.Subset;
import uit.spring.annotation.databases.User;
import uit.spring.annotation.databases.UserSubset;
import uit.spring.annotation.interfaces.SubsetInterface;
import uit.spring.annotation.interfaces.UserSubsetInterface;
import uit.spring.annotation.repositories.SubsetRepository;
import uit.spring.annotation.repositories.UserRepository;
import uit.spring.annotation.repositories.UserSubsetRepository;

import java.util.*;

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

        return ResponseEntity.ok().body(subsetInterfaces);
    }

    @GetMapping(GET + "/subset" + "/{subsetId}")
    public ResponseEntity<Object> getSubset(@PathVariable("subsetId") Long subsetId) {
        Optional<Subset> subset = subsetRepository.findById(subsetId);

        if (subset.isEmpty())
            return ResponseEntity.badRequest().body(String.format("Cannot found subset %o", subsetId));

        return ResponseEntity.ok().body(new SubsetInterface(subset.get()));
    }

    @GetMapping(GET + "/annotator" + "/{annotatorName}")
    public ResponseEntity<Object> getSubsetByAnnotator(@PathVariable("annotatorName") String annotatorName) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(annotatorName);

        if (optionalAnnotator.isEmpty()) {
            return ResponseEntity.badRequest().body(String.format("Cannot find annotator %s", annotatorName));
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

        return ResponseEntity.ok().body(userSubsetInterfaces);
    }

    @PostMapping(ASSIGNMENT + ADD)
    public ResponseEntity<Object> addAssignment(@RequestBody UserSubsetInterface userSubsetInterface) {
        Optional<User> optionalAnnotator = userRepository.findById(userSubsetInterface.getUserId());
        if (optionalAnnotator.isEmpty()) {
            log.info(String.format("Cannot found annotator with id %s", userSubsetInterface.getUserId().toString()));
            return ResponseEntity
                    .badRequest()
                    .body(String.format("Cannot found annotator with id %s", userSubsetInterface.getUserId().toString()));
        }
        User annotator = optionalAnnotator.get();

        Optional<Subset> optionalSubset = subsetRepository.findById(userSubsetInterface.getSubsetId());
        if (optionalSubset.isEmpty()) {
            log.info(String.format("Cannot found subset with id %s", userSubsetInterface.getSubsetId().toString()));
            return ResponseEntity
                    .badRequest()
                    .body(String.format("Cannot found subset with id %s", userSubsetInterface.getSubsetId().toString()));
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
            return ResponseEntity
                    .internalServerError()
                    .body(String.format("Failed to add assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId()));
        }

        log.info(String.format("Added successfully assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId()));
        return ResponseEntity
                .ok()
                .body(String.format("Added successfully assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId()));
    }

    @PutMapping(ASSIGNMENT + UPDATE + "/{assignmentId}")
    public ResponseEntity<Object> updateUserSubset(@PathVariable("assignmentId") UUID assignmentId, @RequestBody UserSubsetInterface userSubsetInterface) {
        Optional<User> optionalAnnotator = userRepository.findById(userSubsetInterface.getUserId());
        if (optionalAnnotator.isEmpty())
        {
            log.info(String.format("Cannot find annotator with id %s", userSubsetInterface.getUserId()));
            return ResponseEntity.badRequest().body(String.format("Cannot find annotator with id %s", userSubsetInterface.getUserId()));
        }
        User annotator = optionalAnnotator.get();

        Optional<Subset> optionalSubset = subsetRepository.findById(userSubsetInterface.getSubsetId());
        if (optionalSubset.isEmpty())
        {
            log.info(String.format("Cannot find subset id %s", userSubsetInterface.getSubsetId()));
            return ResponseEntity.badRequest().body(String.format("Cannot find subset id %s", userSubsetInterface.getSubsetId()));
        }
        Subset subset = optionalSubset.get();

        Optional<UserSubset> optionalUserSubset = userSubsetRepository.findById(assignmentId);
        if (optionalUserSubset.isEmpty()) {
            log.info(String.format("Cannot find any assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId()));
            return ResponseEntity
                    .badRequest()
                    .body(String.format("Cannot find any assignment for annotator %s with subset %o", annotator.getUsername(), subset.getId()));
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
            return ResponseEntity.internalServerError().body(String.format("Cannot update assignment for annotator %s with subset %o",
                    annotator.getUsername(), userSubsetInterface.getSubsetId()));
        }

        log.info(String.format("Updated assignment successfully for annotator %s with subset %o",
                annotator.getUsername(), userSubsetInterface.getSubsetId()));
        return ResponseEntity.ok().body(String.format("Updated assignment successfully for annotator %s with subset %o",
                annotator.getUsername(), userSubsetInterface.getSubsetId()));
    }

    @DeleteMapping(ASSIGNMENT + DELETE + "/{assignmentId}")
    ResponseEntity<Object> deleteUserSubset(@PathVariable("assignmentId") UUID assignmentId) {
        Optional<UserSubset> optionalUserSubset = userSubsetRepository.findById(assignmentId);
        if (optionalUserSubset.isEmpty()) {
            log.info("There is no such assignment to delete");
            return ResponseEntity
                    .badRequest()
                    .body("There is no such assignment to delete");
        }
        UserSubset userSubset = optionalUserSubset.get();

        Optional<User> optionalAnnotator = userRepository.findById(userSubset.getUser().getId());
        if (optionalAnnotator.isEmpty()) {
            log.info(String.format("Cannot find annotator with id %s", userSubset.getUser().getId()));
            return ResponseEntity
                    .badRequest()
                    .body(String.format("Cannot find annotator with id %s", userSubset.getUser().getId()));
        }
        User annotator = optionalAnnotator.get();

        try {
            log.info(String.format("Deleting assignment for annotator %s with subset %o",
                    annotator.getUsername(), userSubset.getSubset().getId()));
            userSubsetRepository.delete(userSubset);
        }
        catch(RuntimeException deleteException) {
            log.info(deleteException.getMessage());
            if (!userSubsetRepository.findAll().contains(userSubset))
                userSubsetRepository.save(userSubset); // rollback transaction

            return ResponseEntity.internalServerError().body(String.format("Cannot remove assignment for annotator %s with subset %o",
                    annotator.getUsername(), userSubset.getSubset().getId()));
        }

        log.info(String.format("Deleted successfully assignment for annotator %s with subset %o",
                annotator.getUsername(), userSubset.getSubset().getId()));
        return ResponseEntity
                .ok()
                .body(String.format("Deleted successfully assignment for annotator %s with subset %o",
                        annotator.getUsername(), userSubset.getSubset().getId()));
    }

}
