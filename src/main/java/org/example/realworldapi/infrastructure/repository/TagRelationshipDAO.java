package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationship;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.entity.TagRelationshipEntity;

import java.util.List;

@Dependent
public class TagRelationshipDAO
        extends AbstractDAO<TagRelationshipEntity, Long>
        implements TagRelationshipRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public void save(TagRelationship tagRelationship) {
        final var articleEntity = findArticleEntityById(tagRelationship.getArticle().getId());
        final var tagEntity = findTagEntityById(tagRelationship.getTag().getId());
        em.persist(new TagRelationshipEntity(articleEntity, tagEntity));
    }

    @Override
    public List<Tag> findArticleTags(Article article) {
        String jpql = "SELECT t FROM TagRelationshipEntity t where t.article.id = :articleId";
        Query query = em.createQuery(jpql);
        query.setParameter("articleId", article.getId());

        List<TagRelationshipEntity> tagEntities = query.getResultList();

        // Map the results to Article objects
        return tagEntities.stream()
                .map(entityUtils::tag)
                .toList();
    }
}
