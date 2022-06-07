package uit.spring.annotation.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uit.spring.annotation.interfaces.ErrorInterface;
import uit.spring.annotation.interfaces.TokenInterface;
import uit.spring.annotation.security.UserDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uit.spring.annotation.security.UserRole.*;
import static uit.spring.annotation.utils.SecretKeys.*;

@Slf4j
public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        try {
            UsernameAndPasswordAuthenticationRequest authenticationRequest = new ObjectMapper()
                    .readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()
            );

            authentication = authenticationManager.authenticate(authentication);

            if (authentication.isAuthenticated())
                log.info(String.format("User %s is authenticated", authenticationRequest.getUsername()));
            else
                log.info(String.format("User %s is not authenticated", authenticationRequest.getUsername()));

            return authentication;
        }

        catch (IOException exception) {
            throw new RuntimeException((exception));
        }
    }

    private String getRole(List<String> grantedAuthorities) {
        for (String authority: grantedAuthorities) {
            if (authority.equals(ADMIN.getRole()))
                return ADMIN.getRole();
        }

        return ANNOTATOR.getRole();
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        UserDetails user = (UserDetails) authResult.getPrincipal();
        List<String> grantedAuthorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET_KEY.getBytes());
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 120*60*1000))
                .withIssuer(request.getRequestURI().toString())
                .withClaim("authorities", grantedAuthorities)
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 180*60*1000))
                .withIssuer(request.getRequestURI().toString())
                .withClaim("authorities", grantedAuthorities)
                .sign(algorithm);

        String role = getRole(grantedAuthorities);

        TokenInterface tokens = new TokenInterface(
                HttpStatus.OK,
                accessToken,
                refreshToken,
                role
        );
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        UsernameAndPasswordAuthenticationRequest authenticationRequest = new ObjectMapper()
                .readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);

        String message = String.format("Failed to authenticate user %s, please check your username or password again");
        log.error(message);

        ErrorInterface error = new ErrorInterface(
                HttpStatus.UNAUTHORIZED,
                failed.getMessage()
        );
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
