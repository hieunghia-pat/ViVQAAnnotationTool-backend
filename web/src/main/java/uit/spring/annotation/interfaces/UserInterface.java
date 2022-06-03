package uit.spring.annotation.interfaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;
import uit.spring.annotation.databases.User;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@Component
public class UserInterface {
    private UUID id;
    private String username;
    private String firstname;
    private String lastname;
    private String role;

    public UserInterface() {
    }

    public UserInterface(UUID id, String username, String firstname, String lastname, String role) {
        this.id = id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
    }

    public UserInterface(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.role = user.getRole();
    }
}
