package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AbstractDAO<ENTITY, ID> {

    @PersistenceContext
    protected EntityManager em;

    protected UserEntity findUserEntityById(UUID id) {
        return em.find(UserEntity.class, id);
    }

    protected TagEntity findTagEntityById(UUID id) {
        return em.find(TagEntity.class, id);
    }

    protected ArticleEntity findArticleEntityById(UUID id) {
        return em.find(ArticleEntity.class, id);
    }

    protected CommentEntity findCommentEntityById(UUID id) {
        return em.find(CommentEntity.class, id);
    }

    protected FavoriteRelationshipEntity findFavoriteRelationshipEntityByKey(
            FavoriteRelationship favoriteRelationship) {

        final var userEntity = findUserEntityById(favoriteRelationship.getUser().getId());
        final var articleEntity = findArticleEntityById(favoriteRelationship.getArticle().getId());

        final var favoriteRelationshipEntityKey = new FavoriteRelationshipEntityKey();
        favoriteRelationshipEntityKey.setUser(userEntity);
        favoriteRelationshipEntityKey.setArticle(articleEntity);

        return em.find(FavoriteRelationshipEntity.class, favoriteRelationshipEntityKey);
    }

    protected boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    protected List<String> toUpperCase(List<String> subjectList) {
        return subjectList.stream().map(String::toUpperCase).collect(Collectors.toList());
    }
}
