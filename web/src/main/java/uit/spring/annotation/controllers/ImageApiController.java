package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uit.spring.annotation.databases.Image;
import uit.spring.annotation.databases.Subset;
import uit.spring.annotation.interfaces.ImageInterface;
import uit.spring.annotation.interfaces.SubsetInterface;
import uit.spring.annotation.repositories.ImageRepository;
import uit.spring.annotation.repositories.SubsetRepository;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            return ResponseEntity.badRequest().body(message);
        }
        Image image = optionalImage.get();

        if (image.getUrl() == null) {
            HttpHeaders headers = new HttpHeaders();
            String imageFile = String.format("WEB-INF/images/subsets/subset_%s/%s", image.getSubset().getId(), image.getFilename());
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(imageFile);
            try {
                assert in != null;
                log.info(String.format("Loading image %s", imageId));
                byte[] media = IOUtils.toByteArray(in);
                headers.setCacheControl(CacheControl.noCache().getHeaderValue());
                ResponseEntity<Object> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);

                log.info(String.format("Loaded image %s successfully", imageId));
                return responseEntity;
            }
            catch (IOException exception) {
                log.info(exception.getMessage());
                return ResponseEntity.internalServerError().body(String.format("Error occurred while loading image %s", imageId));
            }
        }

        log.info(String.format("Loaded image %s successfully", imageId));
        return ResponseEntity.ok().body(new ImageInterface(image));
    }

    @GetMapping(GET + SUBSET + "/{subsetId}")
    public ResponseEntity<Object> getImageInterfaces(@PathVariable("subsetId") Long subsetId) {
        Optional<Subset> optionalSubset = subsetRepository.findById(subsetId);
        if (optionalSubset.isEmpty()) {
            String message = String.format("Cannot find subset %s", subsetId);
            log.info(message);
            return ResponseEntity.badRequest().body(message);
        }
        Subset subset = optionalSubset.get();
        List<ImageInterface> imageInterfaces = new ArrayList<>();
        for (Image image: subset.getImages()) {
            imageInterfaces.add(new ImageInterface(image));
        }

        return ResponseEntity.ok().body(imageInterfaces);
    }
}
