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

        List<UserSubset> userSubsets = userSubsetRepository.findByUserId(annotator.getId());
        List<SubsetInterface> subsetInterfaces = new ArrayList<>();
        for (UserSubset userSubset: userSubsets) {
            Subset subset = subsetRepository.findById(userSubset.getSubset().getId()).get();
            subsetInterfaces.add(new SubsetInterface(subset));
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                subsetInterfaces
        );
        return ResponseEntity.status(OK).body(response);
    }

}
