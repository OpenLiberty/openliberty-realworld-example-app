package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntityKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class FavoriteRelationshipDAO
        extends AbstractDAO<FavoriteRelationshipEntity, FavoriteRelationshipEntityKey>
        implements FavoriteRelationshipRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public boolean isFavorited(Article article, UUID currentUserId) {
        String jpql = "SELECT COUNT(f) FROM FavoriteRelationship f where f.id = :articleId and f.user.id = :currentUserId";
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        Long qty = query.getSingleResult();
        return !(qty > 0);
    }

    @Override
    public long favoritesCount(Article article) {
        String jpql = "SELECT COUNT(f) FROM FavoriteRelationship f where f.id = :articleId";
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("articleId", article.getId());
        return query.getSingleResult();
    }

    @Override
    public Optional<FavoriteRelationship> findByArticleIdAndUserId(
            UUID articleId, UUID currentUserId) {
        String jpql = "SELECT f FROM FavoriteRelationship f where f.id = :articleId and f.user.id = :currentUserId";
        Query query = em.createQuery(jpql);
        query.setParameter("articleId", articleId);
        query.setParameter("currentUserId", currentUserId);

        List<FavoriteRelationshipEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            FavoriteRelationship f = entityUtils.favoriteRelationship(resultList.get(0));
            return Optional.of(f);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void save(FavoriteRelationship favoriteRelationship) {
        final var userEntity = findUserEntityById(favoriteRelationship.getUser().getId());
        final var articleEntity = findArticleEntityById(favoriteRelationship.getArticle().getId());
        em.persist(new FavoriteRelationshipEntity(userEntity, articleEntity));
    }

    @Override
    public void delete(FavoriteRelationship favoriteRelationship) {
        final var favoriteRelationshipEntity =
                findFavoriteRelationshipEntityByKey(favoriteRelationship);
        em.remove(favoriteRelationshipEntity);
    }
}
