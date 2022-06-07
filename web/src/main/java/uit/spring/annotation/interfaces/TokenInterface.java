package uit.spring.annotation.interfaces;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class TokenInterface {
    private Integer status;
    private String accessToken;
    private String refreshToken;
    private String role;

    public TokenInterface(Integer status, String accessToken, String refreshToken, String role) {
        this.status = status;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public TokenInterface(HttpStatus status, String accessToken, String refreshToken, String role) {
        this.status = status.value();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

}
