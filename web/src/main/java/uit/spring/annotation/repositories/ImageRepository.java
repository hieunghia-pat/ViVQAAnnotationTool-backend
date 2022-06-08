package uit.spring.annotation.repositories;

import org.springframework.data.jpa.repository.Query;
import uit.spring.annotation.databases.Annotation;
import uit.spring.annotation.databases.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query(
            value = "SELECT * FROM image WHERE subset_id=?1",
            nativeQuery = true
    )
    List<Image> findBySubsetId(Long subsetId);
}
