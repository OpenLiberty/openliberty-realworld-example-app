package org.example.realworldapi.infrastructure.repository.hibernate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "FAVORITE_RELATIONSHIP")
@NamedQueries({
        @NamedQuery(name = "FVREisFavorited", query = "select count(f) from FavoriteRelationshipEntity f where f.article.id = :articleId and f.user.id = :currentUserId"),
        @NamedQuery(name = "FVREfavoritesCount", query = "select count(f) from FavoriteRelationshipEntity f where f.article.id = :articleId"),
        @NamedQuery(name = "FVREfindByArticleAndUserID", query = "select f from FavoriteRelationshipEntity f where f.article.id = :articleId and f.user.id = :currentUserId")
})
public class FavoriteRelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private FavoriteRelationshipEntityKey primaryKey;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private ArticleEntity article;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private UserEntity user;

    public FavoriteRelationshipEntity(UserEntity user, ArticleEntity article) {
        final var favoriteRelationshipEntityKey = new FavoriteRelationshipEntityKey();
        favoriteRelationshipEntityKey.setUser(user);
        favoriteRelationshipEntityKey.setArticle(article);
        this.primaryKey = favoriteRelationshipEntityKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FavoriteRelationshipEntity that = (FavoriteRelationshipEntity) o;
        return Objects.equals(primaryKey, that.primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKey);
    }
}
