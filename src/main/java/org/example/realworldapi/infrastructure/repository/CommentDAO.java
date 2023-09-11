package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.CommentEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Dependent
public class CommentDAO extends AbstractDAO<CommentEntity, UUID>
        implements CommentRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public void save(Comment comment) {
        final var authorEntity = findUserEntityById(comment.getAuthor().getId());
        final var articleEntity = findArticleEntityById(comment.getArticle().getId());
        em.persist(new CommentEntity(authorEntity, articleEntity, comment));
    }

    @Override
    public Optional<Comment> findByIdAndAuthor(UUID commentId, UUID authorId) {
        String jpql = "SELECT c FROM Comment c WHERE c.id = :commentId and author.id = :authorId";
        TypedQuery<CommentEntity> query = em.createQuery(jpql, CommentEntity.class);
        query.setParameter("commentId", commentId);
        query.setParameter("authorId", authorId);

        List<CommentEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Comment c = entityUtils.comment(resultList.get(0));
            return Optional.of(c);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Comment comment) {
        final var commentEntity = findCommentEntityById(comment.getId());
        em.remove(commentEntity);
    }

    @Override
    public List<Comment> findCommentsByArticle(Article article) {
        String jpql = "SELECT c FROM CommentEntity c WHERE article.id = :articleId";
        TypedQuery<CommentEntity> query = em.createQuery(jpql, CommentEntity.class);
        query.setParameter("articleId", article.getId());

        List<CommentEntity> resultList = query.getResultList();
        return resultList.stream().map(entityUtils::comment).collect(Collectors.toList());
    }
}
