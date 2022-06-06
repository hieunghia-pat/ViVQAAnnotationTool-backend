package uit.spring.annotation.databases;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static uit.spring.annotation.security.UserRole.*;

@Slf4j
@Data
@EqualsAndHashCode(of = "id")
@Entity(name = "User")
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "firstname", nullable = false)
    private String firstname;
    @Column(name = "lastname", nullable = false)
    private String lastname;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "role", nullable = false)
    private String role;
    @OneToMany(mappedBy = "annotator")
    Set<Annotation> annotations;

    public Collection<? extends GrantedAuthority> getGrantedAuthorities() {

        if (!(Objects.equals(this.role, "ROLE_ADMIN") || (Objects.equals(this.role, "ROLE_ANNOTATOR"))))
        {
            log.info("There is no valid role available");
            return null;
        }

        if (Objects.equals(this.role, "ROLE_ADMIN"))
        {
            log.info(String.format("%s has admin role", this.username));
            return ADMIN.getGrantedAuthorities();
        }

        log.info(String.format("%s has annotator role", this.username));
        return ANNOTATOR.getGrantedAuthorities();
    }

    public User() {

    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
