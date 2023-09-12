package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.DeleteArticleBySlug;
import org.example.realworldapi.domain.feature.FindArticleByAuthorAndSlug;
import org.example.realworldapi.domain.model.article.ArticleRepository;

import java.util.UUID;

@Singleton
public class DeleteArticleBySlugImpl implements DeleteArticleBySlug {

    @Inject
    private FindArticleByAuthorAndSlug findArticleByAuthorAndSlug;
    @Inject
    private ArticleRepository articleRepository;

    @Override
    public void handle(UUID authorId, String slug) {
        final var article = findArticleByAuthorAndSlug.handle(authorId, slug);
        articleRepository.delete(article);
    }
}
