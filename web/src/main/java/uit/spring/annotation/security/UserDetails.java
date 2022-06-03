package uit.spring.annotation.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uit.spring.annotation.databases.User;

import java.util.Collection;

@Slf4j
public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(13);

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetails(User user) {
        this.username = user.getUsername();
        this.password = passwordEncoder.encode(user.getPassword());
        this.authorities = user.getGrantedAuthorities();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
