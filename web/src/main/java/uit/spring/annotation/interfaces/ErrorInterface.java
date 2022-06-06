package uit.spring.annotation.interfaces;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorInterface {
    private Integer status;
    private String error;

    public ErrorInterface() {
    }

    public ErrorInterface(Integer status, String error) {
        this.status = status;
        this.error = error;
    }

    public ErrorInterface(HttpStatus status, String error) {
        this.status = status.value();
        this.error = error;
    }
}
