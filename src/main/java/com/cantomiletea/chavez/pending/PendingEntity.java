package com.cantomiletea.chavez.pending;


import com.cantomiletea.chavez.user.UserInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "pending",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "slug"})
)
public class PendingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserInfoEntity user;

    @Column(nullable = false)
    private String slug;
}
