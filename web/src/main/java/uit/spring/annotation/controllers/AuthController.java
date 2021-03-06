package uit.spring.annotation.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.TokenInterface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import static javax.security.auth.callback.ConfirmationCallback.OK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uit.spring.annotation.security.UserRole.ADMIN;
import static uit.spring.annotation.security.UserRole.ANNOTATOR;
import static uit.spring.annotation.utils.Mappings.*;
import static uit.spring.annotation.utils.SecretKeys.TOKEN_SECRET_KEY;

@RestController
@RequestMapping(API)
@Slf4j
@CrossOrigin
public class AuthController {

    private static String getRole(List<String> grantedAuthorities) {
        for (String authority: grantedAuthorities) {
            if (authority.equals(ADMIN.getRole()))
                return ADMIN.getRole();
        }

        return ANNOTATOR.getRole();
    }

    @PostMapping(REFRESH_TOKEN)
    public static void refreshToken(HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refreshToken = authorizationHeader.replace("Bearer ", "");
                Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET_KEY.getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                String username = decodedJWT.getSubject();
                List<String> authorities = decodedJWT.getClaim("authorities").asList(String.class);

                String accessToken = JWT.create()
                        .withSubject(username)
                        .withExpiresAt(new Date(System.currentTimeMillis() + 120*60*1000))
                        .withIssuer(request.getRequestURI().toString())
                        .withClaim("authorities", authorities)
                        .sign(algorithm);

                refreshToken = JWT.create()
                        .withSubject(username)
                        .withExpiresAt(new Date(System.currentTimeMillis() + 180*60*1000))
                        .withIssuer(request.getRequestURI().toString())
                        .withClaim("authorities", authorities)
                        .sign(algorithm);

                String role = getRole(authorities);

                TokenInterface tokens = new TokenInterface(
                        OK,
                        accessToken,
                        refreshToken,
                        role
                );
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            }
            catch (Exception exception) {
                log.error(exception.getMessage());
                ErrorInterface error = new ErrorInterface(
                        UNAUTHORIZED,
                        exception.getMessage()
                );
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        }
        else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
}
