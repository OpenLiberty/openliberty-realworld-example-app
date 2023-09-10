package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class TagDAO extends AbstractDAO<TagEntity, UUID>
        implements TagRepository {

    private final EntityUtils entityUtils;

    @Override
    public List<Tag> findAllTags() {
        String jpql = "SELECT t FROM TagEntity t";
        TypedQuery<TagEntity> query = em.createQuery(jpql, TagEntity.class);

        List<TagEntity> resultList = query.getResultList();
        return resultList.stream().map(entityUtils::tag).collect(Collectors.toList());
    }

    @Override
    public Optional<Tag> findByName(String name) {
        String jpql = "SELECT t FROM TagEntity t WHERE UPPER(t.name) = :name";
        TypedQuery<TagEntity> query = em.createQuery(jpql, TagEntity.class);
        query.setParameter("upper(name)", name.toUpperCase().trim());

        List<TagEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Tag t = entityUtils.tag(resultList.get(0));
            return Optional.of(t);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void save(Tag tag) {
        em.persist(new TagEntity(tag));
    }

    @Override
    public List<Tag> findByNames(List<String> names) {

        String jpql = "SELECT t FROM TagEntity t WHERE UPPER(t.name) in :names";
        TypedQuery<TagEntity> query = em.createQuery(jpql, TagEntity.class);
        List<String> namesUpper = names.stream()
                .map(String::toUpperCase)
                .toList();
        query.setParameter("name", namesUpper);

        List<TagEntity> resultList = query.getResultList();
        return resultList.stream().map(entityUtils::tag).collect(Collectors.toList());
    }
}
