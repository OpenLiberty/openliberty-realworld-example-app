package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationship;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntityKey;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class TagRelationshipDAO
        extends AbstractDAO<TagRelationshipEntity, TagRelationshipEntityKey>
        implements TagRelationshipRepository {

    private final EntityUtils entityUtils;

    @Override
    public void save(TagRelationship tagRelationship) {
        final var articleEntity = findArticleEntityById(tagRelationship.getArticle().getId());
        final var tagEntity = findTagEntityById(tagRelationship.getTag().getId());
        em.persist(new TagRelationshipEntity(articleEntity, tagEntity));
    }

    @Override
    public List<Tag> findArticleTags(Article article) {
        String jpql = "SELECT t FROM TagRelationshipEntity t where t.primaryKey.article.id = :articleId";
        Query query = em.createQuery(jpql);
        query.setParameter("articleId", article.getId());

        List<TagEntity> tagEntities = query.getResultList();

        // Map the results to Article objects
        return tagEntities.stream()
                .map(entityUtils::tag)
                .toList();
    }
}
