package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.DeleteArticleBySlug;
import org.example.realworldapi.domain.feature.FindArticleByAuthorAndSlug;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;

import java.util.UUID;

@Singleton
public class DeleteArticleBySlugImpl implements DeleteArticleBySlug {

    @Inject
    private FindArticleByAuthorAndSlug findArticleByAuthorAndSlug;
    @Inject
    private ArticleRepository articleRepository;
    @Inject
    private TagRelationshipRepository tagRelationshipRepository;

    @Override
    public void handle(UUID authorId, String slug) {
        final var article = findArticleByAuthorAndSlug.handle(authorId, slug);
        tagRelationshipRepository.delete(article);
        articleRepository.delete(article);
    }
}
