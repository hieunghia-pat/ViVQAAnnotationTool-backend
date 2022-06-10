package uit.spring.annotation.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uit.spring.annotation.databases.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query(
            value = "SELECT * FROM users WHERE username=?1",
            nativeQuery = true
    )
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE users SET username=?2, firstname=?3, lastname=?4, password=?5 WHERE id=?1",
            nativeQuery = true
    )
    void updateById(UUID id, String username, String firstname, String lastname, String password);
}