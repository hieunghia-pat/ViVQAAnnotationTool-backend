package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uit.spring.annotation.databases.Image;
import uit.spring.annotation.databases.Subset;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.ImageInterface;
import uit.spring.annotation.interfaces.ResponseInterface;
import uit.spring.annotation.repositories.ImageRepository;
import uit.spring.annotation.repositories.SubsetRepository;

import javax.servlet.ServletContext;
import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static uit.spring.annotation.utils.Mappings.*;

@RestController
@Slf4j
@RequestMapping(IMAGES_API)
public class ImageApiController {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private SubsetRepository subsetRepository;
    @Autowired
    private ServletContext servletContext;

    @GetMapping(GET + IMAGE + "/{imageId}")
    public ResponseEntity<Object> getImage(@PathVariable("imageId") Long imageId) {
        Optional<Image> optionalImage = imageRepository.findById(imageId);
        if (optionalImage.isEmpty()) {
            String message = String.format("Cannot find image %s", imageId);
            log.info(message);
            ErrorInterface response = new ErrorInterface(
                    NOT_FOUND,
                    message
            );
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(response);
        }
        Image image = optionalImage.get();

        log.info(String.format("Loaded image %s successfully", imageId));
        ResponseInterface response = new ResponseInterface(
                OK,
                new ImageInterface(image)
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(GET + SUBSET + "/{subsetId}")
    public ResponseEntity<Object> getImageInterfaces(@PathVariable("subsetId") Long subsetId) {
        Optional<Subset> optionalSubset = subsetRepository.findById(subsetId);
        if (optionalSubset.isEmpty()) {
            String message = String.format("Cannot find subset %s", subsetId);
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
        List<ImageInterface> imageInterfaces = new ArrayList<>();
        for (Image image: subset.getImages()) {
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
}
