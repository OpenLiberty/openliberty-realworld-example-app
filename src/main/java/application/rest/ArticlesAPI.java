package application.rest;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.json.JSONObject;

import application.errors.ValidationMessages;
import core.article.Article;
import core.article.CreateArticle;
import core.comments.CreateComment;
import core.comments.Comment;
import core.user.User;
import dao.ArticleDao;
import dao.UserContext;
import dao.UserDao;

@RequestScoped
@Path("/articles")
@RolesAllowed("users")
public class ArticlesAPI {

    @Inject
    private UserContext uc;

    @Inject
    private ArticleDao articleDao;

    @Inject
    private UserDao userDao;

    @Inject
    private JsonWebToken jwt;

    /* List Articles */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @PermitAll
    public Response listArticles(
            @QueryParam("tag") String tag, 
            @QueryParam("author") String author, 
            @QueryParam("favorited") String favoritedBy,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset
    ) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        List<JSONObject> articles = uc.filterArticles(userId, tag, author, favoritedBy, limit, offset);
        return wrapArticlesResponse(articles);
    }

    /* Feed Articles */
    @GET
    @Path("/feed")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response feedArticles(            
            @QueryParam("limit") @DefaultValue("20") int limit, 
            @QueryParam("offset") @DefaultValue("0") int offset
    ) {
        Long userId = jwt.getClaim("id");
        List<JSONObject> articles = uc.grabFeed(userId, limit, offset);
        return wrapArticlesResponse(articles);
    }

    /* Get Article */
    @GET
    @Path("/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @PermitAll
    public Response getArticle(
            @PathParam("slug") String slug,
            @QueryParam("limit") @DefaultValue("20") int limit, 
            @QueryParam("offset") @DefaultValue("0") int offset
    ) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        JSONObject article = uc.findArticleJson(userId, slug);
        return wrapArticleResponse(article);
    }

    /* Create Article */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createArticle(CreateArticle requestBody) {
        Article article = requestBody.getArticle();
        String title = article.getTitle();
        String description = article.getDescription();
        String articleBody = article.getBody();
        
        // Required fields
        if (title == null || description == null || articleBody == null ||
            title.equals("") || description.equals("") || articleBody.equals("")) {
            return Response.status(422)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_REQUIREMENTS_BLANK))
                .build();
        }
        
        User currentUser = userDao.findUser(jwt.getClaim("id"));
        if (currentUser == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.USER_NOT_FOUND))
                .build();
        }

        article.initSlug();
        if (slugExists(article.getSlug())) {
            return Response.status(422)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_SLUG_EXISTS))
                .build();
        }
        article.setAuthor(userDao.findProfile(currentUser.getUsername()));
        articleDao.createArticle(article);

        JSONObject responseBody = article.toJson(currentUser);
        return Response.status(Response.Status.CREATED)
            .entity(new JSONObject().put("article", responseBody).toString())
            .build();
    }

    /* Update Article */
    @PUT
    @Path("/{slug}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateArticle(@PathParam("slug") String slug, CreateArticle requestBody) {
        Article article = articleDao.findArticle(slug);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND))
                .build();
        }
        if (!isArticleOwner(jwt.getClaim("id"), article)) {
            return Response.status(403)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_YOURS))
                .build();
        }
        Article newArticle = requestBody.getArticle();
        newArticle.initSlug();
        if (slugExists(newArticle.getSlug())) {
            return Response.status(422)
            .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_SLUG_EXISTS))
            .build();
        }
        newArticle = articleDao.updateArticle(article, newArticle);
        JSONObject responseBody = uc.findArticleJson(jwt.getClaim("id"), newArticle.getSlug());
        return wrapArticleResponse(responseBody);
    }

    /* Delete Article */
    @DELETE
    @Path("/{slug}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deleteArticle(@PathParam("slug") String slug) {
        Article article = articleDao.findArticle(slug);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND))
                .build();
        }
        if (!isArticleOwner(jwt.getClaim("id"), article)) {
            return Response.status(403)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_YOURS))
                .build();
        }
        articleDao.deleteArticle(article);
        return Response.ok().build();
    }

    /* Add Comments to an Article */
    @POST
    @Path("/{slug}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addComment(@PathParam("slug") String slug, CreateComment createComment) {
        Comment comment = createComment.getComment();

        // Required fields
        String body = comment.getBody();
        if (body == null || body.equals("")) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ValidationMessages.throwError(ValidationMessages.COMMENT_REQUIREMENTS_BLANK))
                .build();
        }
        User currentUser = userDao.findUser(jwt.getClaim("id"));
        if (currentUser == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.USER_NOT_FOUND))
                .build();
        }
        comment.setAuthor(userDao.findProfile(currentUser.getUsername()));
        Long commentId = articleDao.createComment(slug, comment);
        JSONObject responseBody = articleDao.findComment(commentId).toJson(currentUser);
        return Response.status(Response.Status.CREATED)
            .entity(new JSONObject().put("comment", responseBody).toString())
            .build();
    }

    /* Get Comments to an Article */
    @GET
    @Path("/{slug}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComments(@PathParam("slug") String slug) {
        Article article = articleDao.findArticle(slug);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND))
                .build();
        }
        List<Comment> comments = article.getComments();
        return Response.ok(new JSONObject().put("comments", comments).toString()).build();
    }

    /* Delete Comment to an Article */
    @DELETE
    @Path("/{slug}/comments/{id}")
    @Transactional
    public Response deleteComment(@PathParam("slug") String slug, @PathParam("id") String id) {
        Article article = articleDao.findArticle(slug);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND))
                .build();
        }
        Long commentId = Long.parseLong(id);
        Comment comment = articleDao.findComment(commentId);
        if (comment == null) {
            return Response.status(Response.Status.NOT_FOUND)
            .entity(ValidationMessages.throwError(ValidationMessages.COMMENT_NOT_FOUND))
            .build();
        }
        if (!isCommentOwner(jwt.getClaim("id"), comment)) {
            return Response.status(403)
            .entity(ValidationMessages.throwError(ValidationMessages.COMMENT_NOT_YOURS))
            .build();
        }
        article.removeComment(comment);
        articleDao.deleteComment(article.getSlug(), commentId);
        return Response.ok().build();
    }

    /* Favorite Article */
    @POST
    @Path("/{slug}/favorite")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response favorite(@PathParam("slug") String slug) {
        return wrapArticleResponse(uc.favoriteArticleJson(jwt.getClaim("id"), slug));
    }

    /* Unfavorite Article */
    @DELETE
    @Path("/{slug}/favorite")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response unfavorite(@PathParam("slug") String slug) {
        return wrapArticleResponse(uc.unfavoriteArticleJson(jwt.getClaim("id"), slug));
    }

    // Helper Methods
    private Response wrapArticleResponse(JSONObject responseBody) {
        if (responseBody == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND)).build();
        }
        return Response.ok(responseBody.toString()).build();
    }

    private Response wrapArticlesResponse(List<JSONObject> articles) {
        if (articles == null) {     // Empty list is fine, but if articles is null, something's wrong
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND)).build();
        }
        JSONObject responseBody = new JSONObject().put("articles", articles).put("articlesCount", articles.size());
        return Response.ok(responseBody.toString()).build();
    }

    private boolean isArticleOwner(Long userId, Article article) {
        return article.getAuthor().getId().equals(userId);
    }

    private boolean isCommentOwner(Long userId, Comment comment) {
        return comment.getAuthor().getId().equals(userId);
    }

    private boolean slugExists(String slug) {
        return articleDao.findArticle(slug) != null;
    }
}