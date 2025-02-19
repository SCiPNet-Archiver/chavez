package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.review.dto.ReviewRating;
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
        name = "review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "slug"})
)
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserInfoEntity user;

    @Column(nullable = false)
    private String slug;

    @Column
    private ReviewRating rating;

    @Column
    private String comment;
}
