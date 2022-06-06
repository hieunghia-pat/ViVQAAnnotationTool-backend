package uit.spring.annotation.interfaces;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ResponseInterface {
    private Integer status;
    private Object body;

    public ResponseInterface() {
    }

    public ResponseInterface(Integer status, Object body) {
        this.status = status;
        this.body = body;
    }

    public ResponseInterface(HttpStatus status, Object body) {
        this.status = status.value();
        this.body = body;
    }
}
