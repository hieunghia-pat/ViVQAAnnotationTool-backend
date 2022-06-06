package uit.spring.annotation.interfaces;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class TokenInterface {
    private Integer status;
    private String accessToken;
    private String refreshToken;

    public TokenInterface(Integer status, String accessToken, String refreshToken) {
        this.status = status;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public TokenInterface(HttpStatus status, String accessToken, String refreshToken) {
        this.status = status.value();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
