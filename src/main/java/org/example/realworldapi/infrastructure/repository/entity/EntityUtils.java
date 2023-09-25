package org.example.realworldapi.infrastructure.repository.entity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleModelBuilder;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentBuilder;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagBuilder;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserModelBuilder;

@ApplicationScoped
public class EntityUtils {

    @Inject
    @Named("usermodelbuilder")
    private UserModelBuilder userBuilder;
    @Inject
    private TagBuilder tagBuilder;
    @Inject
    private ArticleModelBuilder articleBuilder;
    @Inject
    private CommentBuilder commentBuilder;

    public User user(UserEntity userEntity) {
        final var id = userEntity.getId();
        final var username = userEntity.getUsername();
        final var bio = userEntity.getBio();
        final var image = userEntity.getImage();
        final var password = userEntity.getPassword();
        final var email = userEntity.getEmail();
        return userBuilder.build(id, username, bio, image, password, email);
    }

    public Tag tag(TagEntity tagEntity) {
        return tagBuilder.build(tagEntity.getId(), tagEntity.getName());
    }

    public Tag tag(TagRelationshipEntity tagRelationshipEntity) {
        return tag(tagRelationshipEntity.getTag());
    }

    public Article article(ArticleEntity articleEntity) {
        return articleBuilder.build(
                articleEntity.getId(),
                articleEntity.getSlug(),
                articleEntity.getTitle(),
                articleEntity.getDescription(),
                articleEntity.getBody(),
                articleEntity.getCreatedAt(),
                articleEntity.getUpdatedAt(),
                user(articleEntity.getAuthor()));
    }

    public Comment comment(CommentEntity commentEntity) {
        return commentBuilder.build(
                commentEntity.getId(),
                user(commentEntity.getAuthor()),
                article(commentEntity.getArticle()),
                commentEntity.getBody(),
                commentEntity.getCreatedAt(),
                commentEntity.getUpdatedAt());
    }

    public FavoriteRelationship favoriteRelationship(
            FavoriteRelationshipEntity favoriteRelationshipEntity) {
        return new FavoriteRelationship(
                user(favoriteRelationshipEntity.getUser()),
                article(favoriteRelationshipEntity.getArticle()));
    }
}
