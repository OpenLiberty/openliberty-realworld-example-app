package dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

    public Profile findProfileByUsername(Long userId, String username) {
        User currentUser = userDao.findUser(userId);
        Profile profile = userDao.findProfile(username);
        if (profile == null) return null;
        profile.setFollowing(currentUser);
        return profile;
    }

    public Article findArticle(Long userId, String slug) {
        User currentUser = userDao.findUser(userId);
        Article article = articleDao.findArticle(slug);
        if (article == null) return null;
        setFollowingFavourited(currentUser, article);
        return article;
    }

    public List<Article> filterArticles(Long userId, String tag, String author, String favorited, int limit, int offset) {
        User currentUser = userDao.findUser(userId);
        List<Article> articles = em.createQuery("SELECT a FROM Article a ORDER BY a.updatedAt DESC", Article.class)
                .getResultList();

        // If any filter is provided, we filter the list
        if (tag != null || author != null || favorited != null) {
            Profile profile = userDao.findProfile(favorited);
            // Filter in one iteration for tag, author, and favorited
            // For each parameter, accept if param is null or satisfies condition
            articles = articles.stream().filter(a -> 
                (tag == null || a.getTagList().contains(tag)) && 
                (author == null || a.getAuthor().getUsername().equals(author)) && 
                (favorited == null || (profile == null ? false : profile.checkFavorited(a)))).collect(Collectors.toList());
        }
        List<Article> articlesFiletered = articles.stream().skip(offset).limit(limit).collect(Collectors.toList());

        // Set if followed and favourited by the current user
        setFollowingFavourited(currentUser, articlesFiletered);
        
        return articles;
    }

    public List<Article> grabFeed(Long userId, int limit, int offset) {
        User currentUser = userDao.findUser(userId);
        List<Article> articles = em.createQuery("SELECT a FROM Article a ORDER BY a.updatedAt DESC", Article.class)
                .getResultList();
        
        List<Article> articlesFiletered = articles.stream().skip(offset).limit(limit).collect(Collectors.toList());

        // Set if followed by the current user
        setFollowingFavourited(currentUser, articlesFiletered);
        
        return articles;
    }

    public Article favoriteArticleJson(Long userId, String slug) {
        User currentUser = userDao.findUser(userId);
        Article article = articleDao.findArticle(slug);
        if (article == null) return null;

        // If user did not favorite the article yet
        if (!currentUser.checkFavorited(article)) {
            currentUser.favorite(article);
            article.upFavoritesCount();
        }
        setFollowingFavourited(currentUser, article);
        return article;
    }

    public Article unfavoriteArticleJson(Long userId, String slug) {
        User currentUser = userDao.findUser(userId);
        Article article = articleDao.findArticle(slug);
        if (article == null) return null;

        // Only unfavorite if first favorited
        if (currentUser.checkFavorited(article)) {
            currentUser.unfavorite(article);
            article.downFavoritesCount();
        }
        setFollowingFavourited(currentUser, article);
        return article;
    }
    
   private void setFollowingFavourited(User currentUser, Article article) {
       article.getAuthor().setFollowing(currentUser);
       article.setFavorited(currentUser);
   }
   
   private void setFollowingFavourited(User currentUser, List<Article> articles) {
       articles.forEach(a -> {
           setFollowingFavourited(currentUser, a);
       });
   }
}