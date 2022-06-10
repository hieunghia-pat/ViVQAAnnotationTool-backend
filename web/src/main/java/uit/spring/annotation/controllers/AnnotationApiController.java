package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.Annotation;
import uit.spring.annotation.databases.Image;
import uit.spring.annotation.databases.User;
import uit.spring.annotation.interfaces.AnnotationInterface;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.IAAInterface;
import uit.spring.annotation.interfaces.ResponseInterface;
import uit.spring.annotation.repositories.AnnotationRepository;
import uit.spring.annotation.repositories.ImageRepository;
import uit.spring.annotation.repositories.UserRepository;
import uit.spring.annotation.repositories.UserSubsetRepository;
import uit.spring.annotation.services.IAAService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;
import static uit.spring.annotation.utils.Mappings.*;

@RestController
@Slf4j
@RequestMapping(ANNOTATIONS_API)
public class AnnotationApiController {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private AnnotationRepository annotationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSubsetRepository userSubsetRepository;

    @GetMapping(GET + IMAGE + "/{imageId}")
    public ResponseEntity<Object> getAnnotations(@PathVariable("imageId") Long imageId) {
        Optional<Image> optionalImage = imageRepository.findById(imageId);
        if (optionalImage.isEmpty()) {
            String message = String.format("Cannot find image %o", imageId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND.value(),
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        Image image = optionalImage.get();

        List<Annotation> annotations = image.getAnnotations();

        List<AnnotationInterface> annotationInterfaces = new ArrayList<>();
        for(Annotation annotation : annotations){
            annotationInterfaces.add(new AnnotationInterface(annotation));
        }

        ResponseInterface response = new ResponseInterface(
                OK.value(),
                annotationInterfaces
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping(GET + ANNOTATION + "/{annotationId}")
    public ResponseEntity<Object> getAnnotation(@PathVariable("annotationId") UUID annotationId) {
        Optional<Annotation> optionalAnnotation = annotationRepository.findById(annotationId);
        if (optionalAnnotation.isEmpty()) {
            String message = String.format("Cannot find annotation %s", annotationId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND.value(),
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(message);
        }

        Annotation annotation = optionalAnnotation.get();
        ResponseInterface response = new ResponseInterface(
                OK.value(),
                new AnnotationInterface(annotation)
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping(GET + ANNOTATION_PER_IMAGE)
    public ResponseEntity<Object> getAnnotationByUserForImage(@RequestParam(name = "username") String username,
                                                              @RequestParam(name = "id") Long imageId) {
        Optional<User> optionalAnnotator = userRepository.findByUsername(username);
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator %s", username);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(message);
        }
        User annotator = optionalAnnotator.get();

        Optional<Image> optionalImage = imageRepository.findById(imageId);
        if (optionalImage.isEmpty()) {
            String message = String.format("Cannot find image with id %s", imageId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }

        ResponseInterface response;
        Optional<Annotation> optionalAnnotation = annotationRepository.findByUserForImage(annotator.getId(), imageId);
        if (optionalAnnotation.isEmpty()) {
            response = new ResponseInterface(
                    OK,
                    new AnnotationInterface(imageId, annotator.getId())
            );
        }
        else {
            Annotation annotation = optionalAnnotation.get();
            response = new ResponseInterface(
                    OK,
                    new AnnotationInterface(annotation)
            );
        }

        return ResponseEntity.status(OK).body(response);
    }

    //Test
    @GetMapping(GET + USER_AGREEMENT + "/{subsetId}")
    public ResponseEntity<Object> getAnnotation(@PathVariable("subsetId") Long subsetId) {
        IAAService iaaService = new IAAService();
        IAAInterface iaaInterface = iaaService.calIAA(subsetId);

        return ResponseEntity
                .status(OK)
                .body(iaaInterface);
    }

    @PostMapping(ADD + "/{imageId}")
    public ResponseEntity<Object> addAnnotation(@PathVariable("imageId") Long imageId, @RequestBody AnnotationInterface annotationInterface) {
        Optional<Image> optionalImage = imageRepository.findById(imageId);
        if (optionalImage.isEmpty()) {
            String message = String.format("Cannot find image %o", imageId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND.value(),
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        Image image = optionalImage.get();

        Optional<User> optionalAnnotator = userRepository.findById(annotationInterface.getUserId());
        if (optionalAnnotator.isEmpty()) {
            String message = String.format("Cannot find annotator with ID %s", annotationInterface.getUserId());
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND.value(),
                    message
            );
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
        User annotator = optionalAnnotator.get();

        Annotation annotation = new Annotation(
                annotationInterface.getQuestion(),
                annotationInterface.getAnswer(),
                annotationInterface.getQuestionType(),
                annotationInterface.getAnswerType(),
                annotationInterface.isTextQA(),
                annotationInterface.isStateQA(),
                annotationInterface.isActionQA(),
                image,
                annotator
        );

        try {
            log.info(String.format("Saving new annotation for image %s", imageId));
            annotationRepository.save(annotation);
        }
        catch(RuntimeException saveException) {
            log.info(saveException.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR.value(),
                    String.format("Cannot save annotation for image %s", imageId)
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        String message = String.format("Saved successfully new annotation for image %s", imageId);
        log.info(message);
        ResponseInterface response = new ResponseInterface(
                OK,
                message
        );
        return ResponseEntity.status(OK).body(response);
    }

    @PutMapping(UPDATE + "/{annotationId}")
    public ResponseEntity<Object> updateAnnotation(@PathVariable("annotationId") UUID annotationId, @RequestBody AnnotationInterface annotationInterface) {
        Optional<Annotation> optionalAnnotation = annotationRepository.findById(annotationId);
        if (optionalAnnotation.isEmpty()) {
            String message = String.format("Cannot find annotation %s", annotationId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(message);
        }

        try {
            log.info(String.format("Updating annotation %s", annotationId));
            log.info(annotationInterface.toString());
            annotationRepository.updateById(
                    annotationInterface.getId(),
                    annotationInterface.getQuestion(),
                    annotationInterface.getAnswer(),
                    annotationInterface.getQuestionType(),
                    annotationInterface.getAnswerType(),
                    annotationInterface.isTextQA(),
                    annotationInterface.isStateQA(),
                    annotationInterface.isActionQA(),
                    annotationInterface.getImageId(),
                    annotationInterface.getUserId()
            );
        }
        catch (RuntimeException exception) {
            log.info(exception.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    String.format("Failed to update for annotation %s", annotationId)
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        String message = String.format("Updated annotation %s successfully", annotationId);
        log.info(message);
        ResponseInterface response = new ResponseInterface(
                OK,
                message
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @DeleteMapping(DELETE + "/{annotationId}")
    public ResponseEntity<Object> deleteAnnotation(@PathVariable("annotationId") UUID annotationId) {
        Optional<Annotation> optionalAnnotation = annotationRepository.findById(annotationId);
        if (optionalAnnotation.isEmpty()) {
            String message = String.format("Cannot find annotation %s", annotationId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        Annotation annotation = optionalAnnotation.get();


        try {
            annotationRepository.delete(annotation);
        }
        catch (RuntimeException exception) {
            log.info(exception.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    String.format("Failed to delete for annotation %s", annotationId)
            );
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        String message = String.format("Deleted annotation %s successfully", annotationId);
        log.info(message);
        ResponseInterface response = new ResponseInterface(
                OK,
                message
        );
        return ResponseEntity
                .status(OK)
                .body(response);
    }
}
