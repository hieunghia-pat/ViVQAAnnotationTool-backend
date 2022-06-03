package uit.spring.annotation.databases;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Entity(name = "Subset")
@Table(name = "Subset")
@Data
@EqualsAndHashCode(of = "id")
public class Subset {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;
    @OneToMany(mappedBy = "subset")
    private List<Image> images;

    public Subset() {

    }

    public Subset(Long id) {
        this.id = id;
    }
}