package uit.spring.annotation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uit.spring.annotation.databases.UserSubset;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubsetRepository extends JpaRepository<UserSubset, UUID> {
    @Query(
            value = "SELECT * FROM users_subset AS us WHERE us.user_id = ?1",
            nativeQuery = true
    )
    List<UserSubset> findByUserId(UUID userId);
    @Query(
            value = "SELECT * FROM users_subset AS us WHERE us.subset_id = ?1",
            nativeQuery = true
    )
    List<UserSubset> findBySubsetId(Long subsetId);

    @Query(
            value = "SELECT * FROM users_subset AS us WHERE us.user_id = ?1 AND us.subset_id = ?2",
            nativeQuery = true
    )
    Optional<UserSubset> findByUserIdAndSubsetId(UUID userId, Long subsetId);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE users_subset SET user_id=?2, subset_id=?3, assign_date=?4, finish_date=?5, is_validation=?6 WHERE id=?1",
            nativeQuery = true
    )
    void updateById(UUID id, UUID annotatorId, Long subsetId, String assignedDate, String finishDate, boolean isValidation);

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM users_subset WHERE id=?1",
            nativeQuery = true
    )
    void deleteById(UUID id);

}
