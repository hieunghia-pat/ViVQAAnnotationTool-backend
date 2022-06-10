package uit.spring.annotation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uit.spring.annotation.jwt.JwtTokenVerifier;
import uit.spring.annotation.jwt.JwtUsernameAndPasswordAuthenticationFilter;
import uit.spring.annotation.services.UserDetailsService;

import java.util.Arrays;

import static org.springframework.security.config.http.SessionCreationPolicy.*;
import static uit.spring.annotation.security.UserPermission.*;
import static uit.spring.annotation.utils.Mappings.*;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .authorizeRequests()
                    // authorizing for admin apis
                    .antMatchers(API_LOGIN, API_REFRESH_TOKEN)
                        .permitAll()
                    .antMatchers(HttpMethod.GET, ADMIN_GET_API)
                        .hasAuthority(ADMIN_READ.getPermission())
                    .antMatchers(HttpMethod.GET, ADMIN_GET_API + "/*")
                        .hasAuthority(ADMIN_READ.getPermission())
                    .antMatchers(HttpMethod.POST, ADMIN_ADD_API)
                        .hasAuthority(ADMIN_WRITE.getPermission())
                    .antMatchers(HttpMethod.PUT, ADMIN_UPDATE_API)
                        .hasAuthority(ADMIN_WRITE.getPermission())
                    .antMatchers(HttpMethod.DELETE, ADMIN_DELETE_API)
                        .hasAuthority(ADMIN_WRITE.getPermission())
                    .antMatchers(HttpMethod.GET, ADMIN_GET_SUBSET_API + "/*")
                        .hasAuthority(ADMIN_READ.getPermission())
                    .antMatchers(HttpMethod.PUT, ADMIN_UPDATE_SUBSET_API + "/*")
                        .hasAuthority(ADMIN_WRITE.getPermission())
                    // authorizing for annotator apis
                    .antMatchers(HttpMethod.GET, ANNOTATOR_GET_API)
                        .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                    .antMatchers(HttpMethod.GET, ANNOTATOR_GET_API + "/*")
                        .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                    .antMatchers(HttpMethod.POST, ANNOTATOR_ADD_API)
                        .hasAuthority(ADMIN_WRITE.getPermission())
                    .antMatchers(HttpMethod.PUT, ANNOTATOR_UPDATE_API)
                        .hasAnyAuthority(ADMIN_WRITE.getPermission(), ANNOTATOR_WRITE.getPermission())
                    .antMatchers(HttpMethod.DELETE, ANNOTATOR_DELETE_API)
                        .hasAuthority(ADMIN_WRITE.getPermission())
                    // authorizing for subset apis
                    .antMatchers(HttpMethod.GET, SUBSETS_GET_API)
                        .hasAuthority(SUBSET_READ.getPermission())
                    .antMatchers(HttpMethod.GET, SUBSETS_GET_API + "/subset/*")
                        .hasAuthority(SUBSET_READ.getPermission())
                    .antMatchers(HttpMethod.GET, SUBSETS_GET_API + "/annotator/*")
                        .hasAuthority(SUBSET_READ.getPermission())
                    // authorizing for image apis
                .antMatchers(HttpMethod.GET, IMAGES_API + GET + IMAGE + "/*")
                    .hasAuthority(IMAGE_READ.getPermission())
                .antMatchers(HttpMethod.GET, IMAGES_API + GET + SUBSET + "/*")
                    .hasAuthority(IMAGE_READ.getPermission())
                    // authorizing for annotation apis
                .antMatchers(HttpMethod.GET, ANNOTATIONS_API + GET + IMAGE + "/*")
                    .hasAuthority(ANNOTATION_READ.getPermission())
                .antMatchers(HttpMethod.GET, ANNOTATIONS_API + ANNOTATION + GET + "/*")
                    .hasAuthority(ANNOTATION_READ.getPermission())
                .antMatchers(HttpMethod.POST, ANNOTATIONS_API + ADD + "/*")
                    .hasAuthority(ANNOTATION_WRITE.getPermission())
                .antMatchers(HttpMethod.PUT, ANNOTATIONS_API + UPDATE + "/*")
                    .hasAuthority(ANNOTATION_WRITE.getPermission())
                .antMatchers(HttpMethod.DELETE, ANNOTATIONS_API + DELETE + "/*")
                    .hasAuthority(ANNOTATION_WRITE.getPermission())
                // authorizing for assignment apis
                .antMatchers(HttpMethod.GET, ASSIGNMENT_API + GET + "/*")
                    .hasAuthority(ASSIGNMENT_READ.getPermission())
                .antMatchers(HttpMethod.POST, ASSIGNMENT_API + ADD)
                    .hasAuthority(ASSIGNMENT_WRITE.getPermission())
                .antMatchers(HttpMethod.PUT, ASSIGNMENT_API + UPDATE + "/*")
                    .hasAuthority(ASSIGNMENT_WRITE.getPermission())
                .antMatchers(HttpMethod.DELETE, ASSIGNMENT_API + DELETE + "/*")
                    .hasAuthority(ASSIGNMENT_WRITE.getPermission())
                .antMatchers(HttpMethod.GET, ASSIGNMENT_API + GET + STATISTICS + SUBSET)
                    .hasAnyAuthority(ASSIGNMENT_READ.getPermission())
                .antMatchers(HttpMethod.GET, ASSIGNMENT_API + GET + STATISTICS + SUBSETS)
                    .hasAnyAuthority(ASSIGNMENT_READ.getPermission())

                .anyRequest()
                    .authenticated()
                .and()
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager()))
                .addFilterBefore(new JwtTokenVerifier(), JwtUsernameAndPasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
