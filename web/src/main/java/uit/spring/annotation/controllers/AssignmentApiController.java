package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.*;
import uit.spring.annotation.interfaces.*;
import uit.spring.annotation.repositories.AnnotationRepository;
import uit.spring.annotation.repositories.SubsetRepository;
import uit.spring.annotation.repositories.UserRepository;
import uit.spring.annotation.repositories.UserSubsetRepository;
import vn.corenlp.postagger.PosTagger;
import vn.corenlp.wordsegmenter.WordSegmenter;
import vn.pipeline.Sentence;
import vn.pipeline.Word;

import java.io.IOException;
import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uit.spring.annotation.utils.Mappings.*;

@Slf4j
@RestController
@RequestMapping(ASSIGNMENT_API)
public class AssignmentApiController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubsetRepository subsetRepository;
    @Autowired
    private UserSubsetRepository userSubsetRepository;
    @Autowired
    private AnnotationRepository annotationRepository;
//    @Autowired
    private PosTagger posTagger;
//    @Autowired
    private WordSegmenter wordSegmenter;

    @GetMapping(GET + "/{annotatorName}")
    public ResponseEntity<Object> getAssignmentByUsername(@PathVariable("annotatorName") String annotatorName) {
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
                UserSubsetInterface newUserSubsetInterface = new UserSubsetInterface(subset);
                newUserSubsetInterface.setAssigned(false);
                newUserSubsetInterface.setUserId(annotator.getId());
                userSubsetInterfaces.add(newUserSubsetInterface);
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

    @GetMapping(GET + ASSIGNED + "/{annotatorName}")
    public ResponseEntity<Object> getAssignedAssignment(@PathVariable("annotatorName") String annotatorName) {
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
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                userSubsetInterfaces
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping(GET + STATISTICS + SUBSET)
    public ResponseEntity<Object> getStatisticsPerSubset(@RequestParam(name = "username") String username,
                                                @RequestParam(name = "subset-id") Long subsetId) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(username);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", username);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        User annotator = optionalAnnotator.get();

        Optional<Subset> optionalSubset = subsetRepository.findById(subsetId);
        if (optionalSubset.isEmpty()) {
            String message = String.format("Cannot find subset %s", subsetId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        Subset subset = optionalSubset.get();

        List<Image> images = subset.getImages();
        Integer totalImages = images.size();
        Integer totalTextQA = 0;
        Integer totalStateQA = 0;
        Integer totalActionQA = 0;
        Integer totalAnnotatedImages = 0;
        Integer totalDeletedImages = 0;
        List<Integer> questionLengths = new ArrayList<>();
        List<Integer> answerLengths = new ArrayList<>();
        for (Image image: images) {
            Optional<Annotation> optionalAnnotation = annotationRepository.findByUserForImage(annotator.getId(), image.getId());
            if (optionalAnnotation.isPresent()) {
                Annotation annotation = optionalAnnotation.get();
                if (!annotation.getQuestion().equals("") && !annotation.getAnswer().equals("")) {
                    totalAnnotatedImages += 1;
                    questionLengths.add(annotation.getQuestion().replaceAll("\\s+", " ").split(" ").length);
                    answerLengths.add(annotation.getQuestion().replaceAll("\\s+", " ").split(" ").length);
                }
                else
                    totalDeletedImages += 1;
                totalTextQA += annotation.isTextQA() ? 1 : 0;
                totalStateQA += annotation.isStateQA() ? 1 : 0;
                totalActionQA += annotation.isActionQA() ? 1 : 0;
            }
        }

        StatisticsInterface statisticsInterface = new StatisticsInterface(
                annotator.getId(),
                subsetId,
                totalImages,
                totalTextQA,
                totalStateQA,
                totalActionQA,
                totalAnnotatedImages,
                totalDeletedImages,
                questionLengths,
                answerLengths
        );
        ResponseInterface response = new ResponseInterface(
                OK,
                statisticsInterface
        );

        return ResponseEntity.status(OK).body(response);
    }
    
    @GetMapping(GET + POS + SUBSET)
    public ResponseEntity<Object> getPosTaggingPerSubset(@RequestParam(name = "username") String username,
                                                 @RequestParam(name = "subset-id") Long subsetId) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(username);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", username);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        User annotator = optionalAnnotator.get();

        Optional<Subset> optionalSubset = subsetRepository.findById(subsetId);
        if (optionalSubset.isEmpty()) {
            String message = String.format("Cannot find subset %s", subsetId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        Subset subset = optionalSubset.get();

        List<Image> images = subset.getImages();
        Sentence question;
        Sentence answer;
        Map<String, Integer> objects = new HashMap<>();
        Map<String, Integer> verbs = new HashMap<>();
        for (Image image: images) {
            Optional<Annotation> optionalAnnotation = annotationRepository.findByUserForImage(annotator.getId(), image.getId());
            if (optionalAnnotation.isPresent()) {
                Annotation annotation = optionalAnnotation.get();
                log.info(String.format("Processing for annotation %s", annotation.toString()));
                try {
                    question = new Sentence(annotation.getQuestion(), wordSegmenter, posTagger);
                    answer = new Sentence(annotation.getAnswer(), wordSegmenter, posTagger);
                    // collecting objects or verbs in question
                    log.info("Collecting objects or verbs from question");
                    for (Word word : question.getWords()) {
                        if (word.getPosTag().equals("N")) {
                            if (objects.containsKey(word.getForm())) {
                                String token = word.getForm();
                                objects.put(token, objects.get(token) + 1);
                            } else {
                                String token = word.getForm();
                                objects.put(token, 1);
                            }
                        }
                        if (word.getPosTag().equals("V")) {
                            if (verbs.containsKey(word.getForm())) {
                                String token = word.getForm();
                                verbs.put(token, verbs.get(token) + 1);
                            } else {
                                String token = word.getForm();
                                verbs.put(token, 1);
                            }
                        }
                    }
                    // collecting objects or verbs in answer
                    log.info("Collecting objects or verbs from question");
                    for (Word word : answer.getWords()) {
                        if (word.getPosTag().equals("N")) {
                            if (objects.containsKey(word.getForm())) {
                                String token = word.getForm();
                                objects.put(token, objects.get(token) + 1);
                            } else {
                                String token = word.getForm();
                                objects.put(token, 1);
                            }
                        }
                        if (word.getPosTag().equals("V")) {
                            if (verbs.containsKey(word.getForm())) {
                                String token = word.getForm();
                                verbs.put(token, verbs.get(token) + 1);
                            } else {
                                String token = word.getForm();
                                verbs.put(token, 1);
                            }
                        }
                    }
                }
                catch (IOException ioException) {
                    ErrorInterface response = new ErrorInterface(
                            INTERNAL_SERVER_ERROR,
                            ioException.getMessage()
                    );
                    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
                }
            }
        }

        PosInterface posInterface = new PosInterface(
                annotator.getId(),
                subsetId,
                objects,
                verbs
        );
        ResponseInterface response = new ResponseInterface(
                OK,
                posInterface
        );

        return ResponseEntity.status(OK).body(response);
    }

    @GetMapping(GET + STATISTICS + SUBSETS)
    public ResponseEntity<Object> getStatistics(@RequestParam("username") String username) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(username);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", username);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        User annotator = optionalAnnotator.get();

        List<UserSubset> assignments = userSubsetRepository.findByUserId(annotator.getId());
        List<Subset> subsets = assignments.stream().map(UserSubset::getSubset).toList();

        Integer totalImages = 0;
        Integer totalTextQA = 0;
        Integer totalStateQA = 0;
        Integer totalActionQA = 0;
        Integer totalAnnotatedImages = 0;
        Integer totalDeletedImages = 0;
        List<Integer> questionLengths = new ArrayList<>();
        List<Integer> answerLengths = new ArrayList<>();
        for (Subset subset: subsets) {
            List<Image> images = subset.getImages();
            totalImages += images.size();
            for (Image image: images) {
                Optional<Annotation> optionalAnnotation = annotationRepository.findByUserForImage(annotator.getId(), image.getId());
                if (optionalAnnotation.isPresent()) {
                    Annotation annotation = optionalAnnotation.get();
                    if (!annotation.getQuestion().equals("") && !annotation.getAnswer().equals("")) {
                        totalAnnotatedImages += 1;
                        questionLengths.add(annotation.getQuestion().replaceAll("\\s+", " ").split(" ").length);
                        answerLengths.add(annotation.getQuestion().replaceAll("\\s+", " ").split(" ").length);
                    }
                    else
                        totalDeletedImages += 1;
                    totalTextQA += annotation.isTextQA() ? 1 : 0;
                    totalStateQA += annotation.isStateQA() ? 1 : 0;
                    totalActionQA += annotation.isActionQA() ? 1 : 0;
                }
            }
        }

        StatisticsInterface statisticsInterface = new StatisticsInterface(
                annotator.getId(),
                null,
                totalImages,
                totalTextQA,
                totalStateQA,
                totalActionQA,
                totalAnnotatedImages,
                totalDeletedImages,
                questionLengths,
                answerLengths
        );
        ResponseInterface response = new ResponseInterface(
                OK,
                statisticsInterface
        );

        return ResponseEntity.status(OK).body(response);
    }

    @GetMapping(GET + POS + SUBSETS)
    public ResponseEntity<Object> getPosTagging(@RequestParam("username") String username) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(username);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", username);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        User annotator = optionalAnnotator.get();

        List<UserSubset> assignments = userSubsetRepository.findByUserId(annotator.getId());
        List<Subset> subsets = assignments.stream().map(UserSubset::getSubset).toList();

        Map<String, Integer> objects = new HashMap<>();
        Map<String, Integer> verbs = new HashMap<>();
        for (Subset subset : subsets) {
            List<Image> images = subset.getImages();
            for (Image image : images) {
                Optional<Annotation> optionalAnnotation = annotationRepository.findByUserForImage(annotator.getId(), image.getId());
                if (optionalAnnotation.isPresent()) {
                    Annotation annotation = optionalAnnotation.get();
                    Sentence question;
                    Sentence answer;
                    try {
                        question = new Sentence(annotation.getQuestion(), wordSegmenter, posTagger);
                        answer = new Sentence(annotation.getAnswer(), wordSegmenter, posTagger);
                        // collecting objects or verbs in question
                        for (Word word : question.getWords()) {
                            if (word.getPosTag().equals("N")) {
                                if (objects.containsKey(word.getForm())) {
                                    String token = word.getForm();
                                    objects.put(token, objects.get(token) + 1);
                                } else {
                                    String token = word.getForm();
                                    objects.put(token, 1);
                                }
                            }
                            if (word.getPosTag().equals("V")) {
                                if (verbs.containsKey(word.getForm())) {
                                    String token = word.getForm();
                                    verbs.put(token, verbs.get(token) + 1);
                                } else {
                                    String token = word.getForm();
                                    verbs.put(token, 1);
                                }
                            }
                        }
                        // collecting words or objects in answer
                        for (Word word : answer.getWords()) {
                            if (word.getPosTag().equals("N")) {
                                if (objects.containsKey(word.getForm())) {
                                    String token = word.getForm();
                                    objects.put(token, objects.get(token) + 1);
                                } else {
                                    String token = word.getForm();
                                    objects.put(token, 1);
                                }
                            }
                            if (word.getPosTag().equals("V")) {
                                if (verbs.containsKey(word.getForm())) {
                                    String token = word.getForm();
                                    verbs.put(token, verbs.get(token) + 1);
                                } else {
                                    String token = word.getForm();
                                    verbs.put(token, 1);
                                }
                            }
                        }
                    } catch (IOException ioException) {
                        log.info(ioException.getMessage());
                        String message = String.format("Failed to perform POS tagging on annotations of annotator %s", username);
                        ErrorInterface response = new ErrorInterface(
                                INTERNAL_SERVER_ERROR,
                                message
                        );
                        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
                    }
                }
            }
        }

        PosInterface posInterface = new PosInterface(
                annotator.getId(),
                null,
                objects,
                verbs
        );
        ResponseInterface response = new ResponseInterface(
                OK,
                posInterface
        );

        return ResponseEntity.status(OK).body(response);
    }

    @PostMapping(ADD)
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

    @PutMapping(UPDATE + "/{assignmentId}")
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

    @DeleteMapping(DELETE + "/{assignmentId}")
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
