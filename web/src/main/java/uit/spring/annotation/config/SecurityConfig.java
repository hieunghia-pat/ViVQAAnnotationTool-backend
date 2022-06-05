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
                        .hasAnyAuthority(ADMIN_WRITE.getPermission(), ANNOTATION_WRITE.getPermission())
                    .antMatchers(HttpMethod.DELETE, ANNOTATOR_DELETE_API)
                        .hasAuthority(ADMIN_WRITE.getPermission())
                    // authorizing for subset apis
                    .antMatchers(HttpMethod.GET, SUBSETS_GET_API)
                        .hasAnyAuthority(ADMIN_READ.getPermission())
                    .antMatchers(HttpMethod.GET, SUBSETS_GET_API + "/subset/*")
                        .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                    .antMatchers(HttpMethod.GET, SUBSETS_GET_API + "/annotator/*")
                        .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                    .antMatchers(HttpMethod.POST, SUBSETS_API + ASSIGNMENT + ADD)
                        .hasAnyAuthority(ADMIN_WRITE.getPermission())
                    .antMatchers(HttpMethod.PUT, SUBSETS_API + ASSIGNMENT + UPDATE + "/*")
                        .hasAnyAuthority(ADMIN_WRITE.getPermission())
                    .antMatchers(HttpMethod.DELETE, SUBSETS_API + ASSIGNMENT + DELETE + "/*")
                        .hasAnyAuthority(ADMIN_WRITE.getPermission())
                    // authorizing for image apis
                .antMatchers(HttpMethod.GET, IMAGES_API + GET + IMAGE + "/*")
                    .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                .antMatchers(HttpMethod.GET, IMAGES_API + GET + SUBSET + "/*")
                    .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                    // authorizing for annotation apis
                .antMatchers(HttpMethod.GET, ANNOTATIONS_API + GET + IMAGE + "/*")
                    .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                .antMatchers(HttpMethod.GET, ANNOTATIONS_API + ANNOTATION + GET + "/*")
                    .hasAnyAuthority(ADMIN_READ.getPermission(), ANNOTATOR_READ.getPermission())
                .antMatchers(HttpMethod.POST, ANNOTATIONS_API + ADD + "/*")
                    .hasAnyAuthority(ADMIN_WRITE.getPermission(), ANNOTATOR_WRITE.getPermission())
                .antMatchers(HttpMethod.PUT, ANNOTATIONS_API + UPDATE + "/*")
                    .hasAnyAuthority(ADMIN_WRITE.getPermission(), ANNOTATOR_WRITE.getPermission())
                .antMatchers(HttpMethod.DELETE, ANNOTATIONS_API + DELETE + "/*")
                    .hasAnyAuthority(ADMIN_WRITE.getPermission(), ANNOTATOR_WRITE.getPermission())

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
