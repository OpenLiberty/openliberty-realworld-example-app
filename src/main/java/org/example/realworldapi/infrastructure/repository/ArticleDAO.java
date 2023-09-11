package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;

import java.util.*;
import java.util.stream.Collectors;

@Dependent
public class ArticleDAO extends AbstractDAO<ArticleEntity, UUID>
        implements ArticleRepository {

    @Inject
    private EntityUtils entityUtils;

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
        String jpql = "SELECT a FROM ArticleEntity a " +
                "JOIN a.author author " +
                "JOIN author.followedBy followedBy " +
                "WHERE followedBy.user.id = :loggedUserId " +
                "ORDER BY a.createdAt DESC, a.updatedAt DESC";

        // Create the query and set parameters
        Query query = em.createQuery(jpql);
        query.setParameter("loggedUserId", articleFilter.getLoggedUserId());

        // Handle paging
        int offset = articleFilter.getOffset();
        int limit = articleFilter.getLimit();
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        // Execute the query and fetch the results
        List<ArticleEntity> articlesEntity = query.getResultList();

        // Map the results to Article objects
        List<Article> articlesResult = articlesEntity.stream()
                .map(entityUtils::article)
                .collect(Collectors.toList());

        // Calculate the total count (you can create a separate method for this)
        long total = countTotalArticles(articleFilter.getLoggedUserId());

        return new PageResult<>(articlesResult, total);
    }

    private long countTotalArticles(UUID loggedUserId) {
        // Create a count query
        String countJpql = "SELECT COUNT(a) FROM ArticleEntity a " +
                "JOIN a.author author " +
                "JOIN author.followedBy followedBy " +
                "WHERE followedBy.user.id = :loggedUserId";

        // Create the query and set parameters
        Query countQuery = em.createQuery(countJpql);
        countQuery.setParameter("loggedUserId", loggedUserId);

        // Execute the count query
        return (long) countQuery.getSingleResult();
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

        String jpql = findArticlesQueryBuilder.toQueryString();
        jpql = jpql + " ORDER BY articles.createdAt DESC, articles.updatetAt DESC;";
        Query query = em.createQuery(jpql);

        // Handle paging
        int offset = filter.getOffset();
        int limit = filter.getLimit();
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        // Execute the query and fetch the results
        List<ArticleEntity> articlesEntity = query.getResultList();

        final var articlesResult =
                articlesEntity.stream().map(entityUtils::article).toList();
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
//    TODO test this thourouglhy
    public long count(List<String> tags, List<String> authors, List<String> favorited) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(a) FROM ArticleEntity a");

        if (!tags.isEmpty() || !authors.isEmpty() || !favorited.isEmpty()) {
            jpql.append(" WHERE 1=1"); // To start building conditions

            if (!tags.isEmpty()) {
                jpql.append(" AND a.tags.name IN :tags");
            }

            if (!authors.isEmpty()) {
                jpql.append(" AND a.author.username IN :authors");
            }

            if (!favorited.isEmpty()) {
                jpql.append(" AND a.favorites.user.username IN :favorites");
            }
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (!tags.isEmpty()) {
            query.setParameter("tags", tags);
        }

        if (!authors.isEmpty()) {
            query.setParameter("authors", authors);
        }

        if (!favorited.isEmpty()) {
            query.setParameter("favorites", favorited);
        }

        return query.getSingleResult();
    }

    //    TODO refactor not needing code beneath
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
