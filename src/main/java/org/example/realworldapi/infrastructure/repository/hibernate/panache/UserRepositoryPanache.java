package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.quarkus.panache.common.Parameters.with;

@ApplicationScoped
@AllArgsConstructor
public class UserRepositoryPanache extends AbstractDAO<UserEntity, UUID>
        implements UserRepository {

    private final EntityUtils entityUtils;

    @Override
    public void save(User user) {
        em.persist(new UserEntity(user));
    }

    @Override
    public boolean existsBy(String field, String value) {
        return em.count("upper(" + field + ")", value.toUpperCase().trim()) > 0;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.email) = :email";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("email", email.toUpperCase().trim());

        List<UserEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            User u = entityUtils.user(resultList.get(0));
            return Optional.of(u);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findUserById(UUID id) {

        UserEntity u = findUserEntityById(id);
        if (u != null) {
            User usr = entityUtils.user(u);
            return Optional.of(usr);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsUsername(UUID excludeId, String username) {
        return count(
                "id != :excludeId and upper(username) = :username",
                with("excludeId", excludeId).and("username", username.toUpperCase().trim()))
                > 0;
    }

    @Override
    public boolean existsEmail(UUID excludeId, String email) {
        return count(
                "id != :excludeId and upper(email) = :email",
                with("excludeId", excludeId).and("email", email.toUpperCase().trim()))
                > 0;
    }

    @Override
    public void update(User user) {
        final var userEntity = findById(user.getId());
        userEntity.update(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.username) = :username";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("username", username.toUpperCase().trim());

        List<UserEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            User u = entityUtils.user(resultList.get(0));
            return Optional.of(u);
        } else {
            return Optional.empty();
        }
    }
}
