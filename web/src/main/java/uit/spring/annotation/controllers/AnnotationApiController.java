package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.databases.Annotation;
import uit.spring.annotation.databases.Image;
import uit.spring.annotation.interfaces.AnnotationInterface;
import uit.spring.annotation.repositories.AnnotationRepository;
import uit.spring.annotation.repositories.ImageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uit.spring.annotation.utils.Mappings.*;

@RestController
@Slf4j
@RequestMapping(ANNOTATIONS_API)
public class AnnotationApiController {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private AnnotationRepository annotationRepository;

    @GetMapping(GET + IMAGE + "/{imageId}")
    public ResponseEntity<Object> getAnnotations(@PathVariable("imageId") Long imageId) {
        Optional<Image> optionalImage = imageRepository.findById(imageId);
        if (optionalImage.isEmpty()) {
            String message = String.format("Cannot find image %o", imageId);
            log.info(message);
            return ResponseEntity.badRequest().body(message);
        }
        Image image = optionalImage.get();

        List<Annotation> annotations = image.getAnnotations();

        List<AnnotationInterface> annotationInterfaces = new ArrayList<>();
        for(Annotation annotation : annotations){
            annotationInterfaces.add(new AnnotationInterface(annotation));
        }

        return ResponseEntity
                .ok()
                .body(annotationInterfaces);
    }

    @GetMapping(GET + ANNOTATION + "/{annotationId}")
    public ResponseEntity<Object> getAnnotation(@PathVariable("annotationId") UUID annotationId) {
        Optional<Annotation> optionalAnnotation = annotationRepository.findById(annotationId);
        if (optionalAnnotation.isEmpty()) {
            log.info(String.format("Cannot find annotation %s", annotationId));
            return ResponseEntity.badRequest().body(String.format("Cannot find annotation %s", annotationId));
        }
        Annotation annotation = optionalAnnotation.get();

        return ResponseEntity.ok().body(new AnnotationInterface(annotation));
    }

    @PostMapping(ADD + "/{imageId}")
    public ResponseEntity<Object> addAnnotation(@PathVariable("imageId") Long imageId, @RequestBody AnnotationInterface annotationInterface) {
        Optional<Image> optionalImage = imageRepository.findById(imageId);
        if (optionalImage.isEmpty()) {
            String message = String.format("Cannot find image %o", imageId);
            log.info(message);
            return ResponseEntity.badRequest().body(message);
        }
        Image image = optionalImage.get();

        Annotation annotation = new Annotation(
                annotationInterface.getQuestion(),
                annotationInterface.getAnswer(),
                annotationInterface.getQuestionType(),
                annotationInterface.getAnswerType(),
                annotationInterface.isTextQA(),
                annotationInterface.isStateQA(),
                annotationInterface.isActionQA(),
                image
        );

        try {
            log.info(String.format("Saving new annotation for image %s", imageId));
            annotationRepository.save(annotation);
        }
        catch(RuntimeException saveException) {
            log.info(saveException.getMessage());
            return ResponseEntity.internalServerError().body(String.format("Cannot save annotation for image %s", imageId));
        }

        log.info(String.format("Saved successfully new annotation for image %s", imageId));
        return ResponseEntity.ok().body(String.format("Saved successfully new annotation for image %s", imageId));
    }

    @PutMapping(UPDATE + "{annotationId}")
    public ResponseEntity<Object> updateAnnotation(@PathVariable("annotationId") UUID annotationId, @RequestBody AnnotationInterface annotationInterface) {
        Optional<Annotation> optionalAnnotation = annotationRepository.findById(annotationId);
        if (optionalAnnotation.isEmpty()) {
            log.info(String.format("Cannot find annotation %s", annotationId));
            return ResponseEntity.badRequest().body(String.format("Cannot find annotation %s", annotationId));
        }
        Annotation oldAnnotation = optionalAnnotation.get();

        Annotation newAnnotation = new Annotation(
                annotationInterface.getQuestion(),
                annotationInterface.getAnswer(),
                annotationInterface.getQuestionType(),
                annotationInterface.getAnswerType(),
                annotationInterface.isTextQA(),
                annotationInterface.isStateQA(),
                annotationInterface.isActionQA(),
                oldAnnotation.getImage()
        );
        newAnnotation.setId(oldAnnotation.getId());

        try {
            annotationRepository.delete(oldAnnotation);
            annotationRepository.save(newAnnotation);
        }
        catch (RuntimeException exception) {
            log.info(exception.getMessage());
             if (!annotationRepository.existsById(oldAnnotation.getId())) { // rollback transaction
                 annotationRepository.save(oldAnnotation);
             }

             return ResponseEntity.internalServerError().body(String.format("Failed to update for annotation %s", annotationId));
        }

        log.info(String.format("Updated for annotation %s successfully", annotationId));
        return ResponseEntity.ok().body(String.format("Updated for annotation %s successfully", annotationId));
    }

    @DeleteMapping(DELETE + "{annotationId}")
    public ResponseEntity<Object> deleteAnnotation(@PathVariable("annotationId") UUID annotationId) {
        Optional<Annotation> optionalAnnotation = annotationRepository.findById(annotationId);
        if (optionalAnnotation.isEmpty()) {
            log.info(String.format("Cannot find annotation %s", annotationId));
            return ResponseEntity.badRequest().body(String.format("Cannot find annotation %s", annotationId));
        }
        Annotation annotation = optionalAnnotation.get();


        try {
            annotationRepository.delete(annotation);
        }
        catch (RuntimeException exception) {
            log.info(exception.getMessage());

            return ResponseEntity.internalServerError().body(String.format("Failed to delete for annotation %s", annotationId));
        }

        log.info(String.format("Deleted for annotation %s successfully", annotationId));
        return ResponseEntity.ok().body(String.format("Deleted for annotation %s successfully", annotationId));
    }
}
