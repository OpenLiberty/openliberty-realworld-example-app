package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.panache.utils.SimpleQueryBuilder;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class ArticleDAO extends AbstractDAO<ArticleEntity, UUID>
        implements ArticleRepository {

    private final EntityUtils entityUtils;

    @Override
    public boolean existsBySlug(String slug) {
        String jpql = "SELECT a FROM ArticleEntity a WHERE UPPER(a.slug) = :slug";
        TypedQuery<ArticleEntity> query = em.createQuery(jpql, ArticleEntity.class);
        query.setParameter("slug", slug.toUpperCase().trim());

        List<ArticleEntity> resultList = query.getResultList();
        return !resultList.isEmpty();
    }

    @Override
    public void save(Article article) {
        final var author = findUserEntityById(article.getAuthor().getId());
        em.persist(new ArticleEntity(article, author));
        em.flush();
    }

    @Override
    public Optional<Article> findArticleById(UUID id) {
        ArticleEntity a = findArticleEntityById(id);
        if (a != null) {
            Article art = entityUtils.article(a);
            return Optional.of(art);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Article> findBySlug(String slug) {

        String jpql = "SELECT a FROM ArticleEntity a WHERE UPPER(a.slug) = :slug";
        TypedQuery<ArticleEntity> query = em.createQuery(jpql, ArticleEntity.class);
        query.setParameter("slug", slug.toUpperCase().trim());

        List<ArticleEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Article article = entityUtils.article(resultList.get(0));
            return Optional.of(article);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void update(Article article) {
        final var articleEntity = findArticleEntityById(article.getId());
        articleEntity.update(article);
    }

    @Override
    public Optional<Article> findByAuthorAndSlug(UUID authorId, String slug) {
        String jpql = "SELECT a FROM ArticleEntity a WHERE UPPER(a.slug) = :slug and author.id = :authorId";
        TypedQuery<ArticleEntity> query = em.createQuery(jpql, ArticleEntity.class);
        query.setParameter("slug", slug.toUpperCase().trim());
        query.setParameter("authorId", authorId);

        List<ArticleEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Article article = entityUtils.article(resultList.get(0));
            return Optional.of(article);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Article article) {
        final var articleEntity = findArticleEntityById(article.getId());
        em.remove(articleEntity);
    }

    @Override
    public PageResult<Article> findMostRecentArticlesByFilter(ArticleFilter articleFilter) {
        final var articlesEntity =
                find(
                        "select articles from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId",
                        Sort.descending("createdAt").and("updatedAt").descending(),
                        Parameters.with("loggedUserId", articleFilter.getLoggedUserId()))
                        .page(Page.of(articleFilter.getOffset(), articleFilter.getLimit()))
                        .list();
        final var articlesResult =
                articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());
        final var total = count(articleFilter.getLoggedUserId());
        return new PageResult<>(articlesResult, total);
    }

    @Override
    public PageResult<Article> findArticlesByFilter(ArticleFilter filter) {
        Map<String, Object> params = new LinkedHashMap<>();
        SimpleQueryBuilder findArticlesQueryBuilder = new SimpleQueryBuilder();
        findArticlesQueryBuilder.addQueryStatement("select articles from ArticleEntity as articles");
        configFilterFindArticlesQueryBuilder(
                findArticlesQueryBuilder,
                filter.getTags(),
                filter.getAuthors(),
                filter.getFavorited(),
                params);
        final var articlesEntity =
                find(
                        findArticlesQueryBuilder.toQueryString(),
                        Sort.descending("createdAt").and("updatedAt").descending(),
                        params)
                        .page(Page.of(filter.getOffset(), filter.getLimit()))
                        .list();
        final var articlesResult =
                articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());
        final var total = count(filter.getTags(), filter.getAuthors(), filter.getFavorited());
        return new PageResult<>(articlesResult, total);
    }

    public long count(UUID loggedUserId) {
//        TODO fix this query
        String jpql = "from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId";
        TypedQuery<ArticleEntity> query = em.createQuery(jpql, ArticleEntity.class);
        query.setParameter("loggedUserId", loggedUserId);

        List<ArticleEntity> resultList = query.getResultList();
        return resultList.size();
    }

    @Override
    public long count(List<String> tags, List<String> authors, List<String> favorited) {
        Map<String, Object> params = new LinkedHashMap<>();
        SimpleQueryBuilder countArticlesQueryBuilder = new SimpleQueryBuilder();
        countArticlesQueryBuilder.addQueryStatement("from ArticleEntity as articles");
        configFilterFindArticlesQueryBuilder(
                countArticlesQueryBuilder, tags, authors, favorited, params);
        return count(countArticlesQueryBuilder.toQueryString(), params);
    }

    private void configFilterFindArticlesQueryBuilder(
            SimpleQueryBuilder findArticlesQueryBuilder,
            List<String> tags,
            List<String> authors,
            List<String> favorited,
            Map<String, Object> params) {

        findArticlesQueryBuilder.updateQueryStatementConditional(
                isNotEmpty(tags),
                "inner join articles.tags as tags inner join tags.primaryKey.tag as tag",
                "upper(tag.name) in (:tags)",
                () -> params.put("tags", toUpperCase(tags)));

        findArticlesQueryBuilder.updateQueryStatementConditional(
                isNotEmpty(authors),
                "inner join articles.author as authors",
                "upper(authors.username) in (:authors)",
                () -> params.put("authors", toUpperCase(authors)));

        findArticlesQueryBuilder.updateQueryStatementConditional(
                isNotEmpty(favorited),
                "inner join articles.favorites as favorites inner join favorites.primaryKey.user as user",
                "upper(user.username) in (:favorites)",
                () -> params.put("favorites", toUpperCase(favorited)));
    }
}
