package dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import core.article.Article;
import core.comments.Comment;

@RequestScoped
public class ArticleDao {

    @PersistenceContext(name = "realWorld-jpa")
    private EntityManager em;

    public void createArticle(Article article) {
        try {
            em.persist(article);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Article findArticle(String slug) {
        try {
            return em.createQuery("SELECT a FROM Article a WHERE a.slug = :slug", Article.class)
                    .setParameter("slug", slug).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Article updateArticle(Article article, Article updates) {
        if (article == null) return null;
        article.update(updates.getTitle(), updates.getDescription(), updates.getBody());
        return em.merge(article);
    }

    public void deleteArticle(Article article) {
        em.remove(article);
    }

    public List<String> getTags() {
        List<List<String>> tagListList = em.createQuery("SELECT a.tagList FROM Article a").getResultList();
        List<String> tags = tagListList.stream().flatMap(tagList -> tagList.stream()).distinct().collect(Collectors.toList());
        return tags;
    }

    public Long createComment(String slug, Comment comment) {
        try {
            findArticle(slug).addComment(comment);
            em.persist(comment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comment.getId();
    }

    public Comment findComment(Long commentId) {
        try {
            return em.find(Comment.class, commentId);
        } catch (NoResultException e) {
            return null;
        }
    }

    public void deleteComment(String slug, Long commentId) {
        em.remove(findComment(commentId));
    }
}