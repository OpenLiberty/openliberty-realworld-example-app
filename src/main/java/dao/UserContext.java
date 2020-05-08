package dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.json.JSONObject;

import core.article.Article;
import core.user.Profile;
import core.user.User;

@RequestScoped
public class UserContext {

    @PersistenceContext(name = "realWorld-jpa")
    private EntityManager em;

    @Inject
    private UserDao userDao;

    @Inject
    private ArticleDao articleDao;

    public void followProfile(Long userId, String username) {
        User currentUser = userDao.findUser(userId);
        Profile celeb = userDao.findProfile(username);
        if (celeb != null) {
            celeb.followedBy(currentUser);
        } else {
            return;
        }
    }

    public void unfollowProfile(Long userId, String username) {
        User currentUser = userDao.findUser(userId);
        Profile celeb = userDao.findProfile(username);
        if (celeb != null) {
            celeb.unfollowedBy(currentUser);
        } else {
            return;
        }
    }

    public JSONObject findProfileByUsernameJson(Long userId, String username) {
        User currentUser = userDao.findUser(userId);
        Profile profile = userDao.findProfile(username);
        if (profile == null) return null;
        return new JSONObject().put("profile", profile.toJson(currentUser));
    }

    public JSONObject findArticleJson(Long userId, String slug) {
        User currentUser = userDao.findUser(userId);
        Article article = articleDao.findArticle(slug);
        return new JSONObject().put("article", article.toJson(currentUser));
    }

    public List<JSONObject> filterArticles(Long userId, String tag, String author, String favorited, int limit, int offset) {
        User currentUser = userDao.findUser(userId);
        List<Article> articles = em.createQuery("SELECT a FROM Article a ORDER BY a.updatedAt DESC", Article.class)
                .setMaxResults(limit).getResultList();

        // If any filter is provided, we filter the list
        if (tag != null || author != null || favorited != null) {
            Profile profile = userDao.findProfile(favorited);
            // Filter in one iteration for tag, author, and favorited
            // For each parameter, accept if param is null or satisfies condition
            articles = articles.stream().filter(a -> 
                (tag == null || a.getTags().contains(tag)) && 
                (author == null || a.getAuthor().getUsername().equals(author)) && 
                (favorited == null || (profile == null ? false : profile.checkFavorited(a)))).collect(Collectors.toList());
        }
        return articles.stream()
            .map(a -> a.toJson(currentUser)).skip(offset).collect(Collectors.toList());
    }

    public List<JSONObject> grabFeed(Long userId, int limit, int offset) {
        User currentUser = userDao.findUser(userId);
        List<Article> articles = em.createQuery("SELECT a FROM Article a ORDER BY a.updatedAt DESC", Article.class)
                .setMaxResults(limit).getResultList();

        // Filter down articles followed by the current user
        return articles.stream()
            .filter(a -> a.getAuthor().checkFollowedBy(currentUser))
            .map(a -> a.toJson(currentUser)).skip(offset).collect(Collectors.toList());
    }

    public JSONObject favoriteArticleJson(Long userId, String slug) {
        User currentUser = userDao.findUser(userId);
        Article article = articleDao.findArticle(slug);
        if (article == null) return null;

        // If user did not favorite the article yet
        if (!currentUser.checkFavorited(article)) {
            currentUser.favorite(article);
            article.upFavoritesCount();
        }
        return new JSONObject().put("article", article.toJson(currentUser));
    }

    public JSONObject unfavoriteArticleJson(Long userId, String slug) {
        User currentUser = userDao.findUser(userId);
        Article article = articleDao.findArticle(slug);
        if (article == null) return null;

        // Only unfavorite if first favorited
        if (currentUser.checkFavorited(article)) {
            currentUser.unfavorite(article);
            article.downFavoritesCount();
        }
        return new JSONObject().put("article", article.toJson(currentUser));
    }
}