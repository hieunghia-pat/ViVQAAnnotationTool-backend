package uit.spring.annotation.interfaces;

import lombok.Data;

@Data
public class ReturnInterface {
    private Integer statusCode;
    private String message;
    private Object body = null;

    public ReturnInterface(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public ReturnInterface(Integer statusCode, String message, Object body) {
        this.statusCode = statusCode;
        this.message = message;
        this.body = body;
    }
}
