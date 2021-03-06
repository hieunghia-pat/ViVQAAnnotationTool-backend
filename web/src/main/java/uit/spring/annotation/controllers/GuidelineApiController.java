package uit.spring.annotation.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.ResponseInterface;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static uit.spring.annotation.utils.Mappings.*;

@Slf4j
@RestController
@RequestMapping(GUIDELINE_API)
public class GuidelineApiController {

    @GetMapping(GET)
    public ResponseEntity<Object> getGuideline() {
        String data;
        try {
            InputStream inputStream = new FileInputStream("web/src/main/resources/guideline/content.md");
            byte[] rawData = Objects.requireNonNull(inputStream).readAllBytes();
            data = IOUtils.toString(rawData, String.valueOf(StandardCharsets.UTF_8));
        }
        catch (NullPointerException nullPointerException) {
            log.info("inputStream is null");
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    "inputStream is null"
            );
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
        }
        catch(IOException ioException) {
            log.info(ioException.getMessage());
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    ioException.getMessage()
            );
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
        }

        ResponseInterface response = new ResponseInterface(
                OK,
                data
        );

        return ResponseEntity.status(OK).body(response);
    }

    @PutMapping(UPDATE)
    public ResponseEntity<Object> updateGuideline(@RequestBody String guideline) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("web/src/main/resources/guideline/content.md"), StandardCharsets.UTF_8);
            writer.write(guideline);
            writer.close();
        }
        catch (FileNotFoundException fileNotFoundException) {
            log.error("Cannot find resource for content guideline");
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    "Cannot find resource for content guideline"
            );

            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
        }
        catch(IOException ioException) {
            log.error("Error while writing into file");
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    "Error while writing into file"
            );

            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
        }
        catch (NullPointerException nullPointerException) {
            log.info("Cannot open resource file for content guideline");
            ErrorInterface response = new ErrorInterface(
                    INTERNAL_SERVER_ERROR,
                    "Cannot open resource file for content guideline"
            );

            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
        }

        log.info("Updated guideline successfully");
        ResponseInterface response = new ResponseInterface(
                OK,
                "Updated guideline successfully"
        );

        return ResponseEntity.status(OK).body(response);
    }
}
