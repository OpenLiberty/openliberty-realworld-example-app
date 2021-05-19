package application.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import application.errors.ValidationMessages;
import core.article.Article;
import core.article.CreateArticle;
import core.comments.Comment;
import core.comments.CreateComment;
import core.user.User;
import dao.ArticleDao;
import dao.UserContext;
import dao.UserDao;

@RequestScoped
@Path("/articles")
@RolesAllowed("users")
public class ArticlesEndpoint {

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
    @PermitAll
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "List Articles", description = "Returns most recent articles globally by default, provide tag, author or favorited query parameter to filter results")
    @APIResponse(responseCode = "200", description = "Articles retrieved", content = @Content(examples = @ExampleObject(ref = "Articles_returned")))
    public Response listArticles(
            @QueryParam("tag") String tag,
            @QueryParam("author") String author,
            @QueryParam("favorited") String favoritedBy,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        List<Article> articles = uc.filterArticles(userId, tag, author, favoritedBy, limit, offset);
        return wrapArticlesResponse(articles);
    }

    /* Feed Articles */
    @GET
    @Path("/feed")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(ref = "Articles")
    @Operation(summary = "Feed Articles", description = "Can also take limit and offset query parameters like List Articles")
    @SecurityRequirement(name = "Authentication")
    @APIResponse(responseCode = "200", description = "Articles retrieved", content = @Content(examples = @ExampleObject(ref = "Articles_returned")))
    public Response feedArticles(
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {
        Long userId = jwt.getClaim("id");
        List<Article> articles = uc.grabFeed(userId, limit, offset);
        return wrapArticlesResponse(articles);
    }

    /* Get Article */
    @GET
    @Path("/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Get Article", description = "Will return single article")
    @APIResponse(responseCode = "200", description = "Article retrieved", content = @Content(examples = @ExampleObject(ref = "Article_returned")))
    public Response getArticle(@PathParam("slug") @Parameter(example = "how-to-train-your-dragon") String slug) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        Article article = uc.findArticle(userId, slug);
        return wrapArticleResponse(article);
    }

    /* Create Article */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Create Article", description = "Returns an Article\n\nRequired fields: title, description, body\nOptional fields: tagList as an array of Strings")
    @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(ref = "Article_create")))
    @APIResponse(responseCode = "200", description = "Article created", content = @Content(examples = @ExampleObject(ref = "Article_returned")))
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

        return Response.status(Response.Status.CREATED)
                .entity(Collections.singletonMap("article", article))
                .build();
    }

    /* Update Article */
    @PUT
    @Path("/{slug}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Update an Article", description = "Updates an Article\n\nOptional fields: title, description, body")
    @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(ref = "Article_update")))
    @APIResponse(responseCode = "200", description = "Article updated", content = @Content(examples = @ExampleObject(ref = "Article_returned")))
    public Response updateArticle(@PathParam("slug") @Parameter(example = "how-to-train-your-dragon") String slug, CreateArticle requestBody) {
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
        Article responseBody = uc.findArticle(jwt.getClaim("id"), newArticle.getSlug());
        return wrapArticleResponse(responseBody);
    }

    /* Delete Article */
    @DELETE
    @Path("/{slug}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Delete Article", description = "Deletes an Article")
    @APIResponse(responseCode = "200", description = "Article deleted")
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
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Add comment", description = "Adds a comment to an Article\n\nRequired field: body")
    @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(ref = "Comment_create")))
    @APIResponse(responseCode = "200", description = "Comment added", content = @Content(examples = @ExampleObject(ref = "Comment_returned")))
    public Response addComment(@PathParam("slug") @Parameter(example = "how-to-train-your-dragon") String slug, CreateComment createComment) {
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
        Comment responseBody = articleDao.findComment(commentId);
        return Response.status(Response.Status.CREATED)
                .entity(Collections.singletonMap("comment", responseBody))
                .build();
    }

    /* Get Comments to an Article */
    @GET
    @Path("/{slug}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Get comments", description = "Get Comments from an Article")
    @APIResponse(responseCode = "200", description = "Comments retrieved", content = @Content(mediaType = "application/json", examples = @ExampleObject(ref = "Comments_returned")))
    public Response getComments(@PathParam("slug") @Parameter(example = "how-to-train-your-dragon") String slug) {
        Article article = articleDao.findArticle(slug);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND))
                    .build();
        }
        List<Comment> comments = article.getComments();

        return Response.ok(Collections.singletonMap("comments", comments)).build();
    }

    /* Delete Comment to an Article */
    @DELETE
    @Path("/{slug}/comments/{id}")
    @Transactional
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Delete comment", description = "Delete a Comment from an Article")
    @APIResponse(responseCode = "200", description = "Comment deleted")
    public Response deleteComment(@PathParam("slug") @Parameter(example = "how-to-train-your-dragon") String slug, @PathParam("id") String id) {
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
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Favorite Article", description = "Favorite an Article")
    @APIResponse(responseCode = "200", description = "Article facourited", content = @Content(examples = @ExampleObject(ref = "Article_favourited_returned")))
    public Response favorite(@PathParam("slug") @Parameter(example = "how-to-train-your-dragon") String slug) {
        return wrapArticleResponse(uc.favoriteArticleJson(jwt.getClaim("id"), slug));
    }

    /* Unfavorite Article */
    @DELETE
    @Path("/{slug}/favorite")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "Articles")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Favorite Article", description = "Favorite an Article")
    @APIResponse(responseCode = "200", description = "Article unfacourited", content = @Content(examples = @ExampleObject(ref = "Article_returned")))
    public Response unfavorite(@PathParam("slug") @Parameter(example = "how-to-train-your-dragon") String slug) {
        return wrapArticleResponse(uc.unfavoriteArticleJson(jwt.getClaim("id"), slug));
    }

    // Helper Methods
    private Response wrapArticleResponse(Article responseBody) {
        if (responseBody == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND)).build();
        }
        
        return Response.ok(Collections.singletonMap("article", responseBody)).build();
    }

    private Response wrapArticlesResponse(List<Article> articles) {
        if (articles == null) { // Empty list is fine, but if articles is null, something's wrong
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ValidationMessages.throwError(ValidationMessages.ARTICLE_NOT_FOUND)).build();
        }
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("articles", articles);
        responseBody.put("articlesCount", articles.size());
        return Response.ok(responseBody).build();
    }

    private boolean isArticleOwner(Long userId, Article article) {
        return article.getAuthor().getUserID().equals(userId);
    }

    private boolean isCommentOwner(Long userId, Comment comment) {
        return comment.getAuthor().getUserID().equals(userId);
    }

    private boolean slugExists(String slug) {
        return articleDao.findArticle(slug) != null;
    }
}