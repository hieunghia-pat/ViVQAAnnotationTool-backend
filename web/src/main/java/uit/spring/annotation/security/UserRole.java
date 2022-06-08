package uit.spring.annotation.security;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static uit.spring.annotation.security.UserPermission.*;

@Slf4j
public enum UserRole {

    ADMIN(Sets.newHashSet(ADMIN_READ, ADMIN_WRITE, ANNOTATOR_READ, ANNOTATOR_WRITE, IMAGE_READ, IMAGE_WRITE,
            ANNOTATION_READ, ANNOTATION_WRITE, SUBSET_READ, SUBSET_WRITE, ASSIGNMENT_READ, ASSIGNMENT_WRITE)),
    ANNOTATOR(Sets.newHashSet(ANNOTATOR_READ, IMAGE_READ, ANNOTATION_READ, ANNOTATION_WRITE, SUBSET_READ));

    private final Set<UserPermission> permissions;

    private UserRole(Set<UserPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<UserPermission> getPermissions() {
        return permissions;
    }

    public String getRole() {
        return String.format("%s_%s", "ROLE", this.name());
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> permissions = this.permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());

        permissions.add(new SimpleGrantedAuthority(String.format("%s_%s", "ROLE", this.name())));

        return permissions;
    }
}
