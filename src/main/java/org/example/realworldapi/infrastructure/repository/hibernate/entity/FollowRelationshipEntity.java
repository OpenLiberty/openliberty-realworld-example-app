package org.example.realworldapi.infrastructure.repository.hibernate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "FOLLOW_RELATIONSHIP")
@NamedQueries({
        @NamedQuery(name = "FREisFollowing", query = "select f from FollowRelationshipEntity f where f.primaryKey.user.id = :currentUserId and f.primaryKey.followed.id = :followedUserId")
})
public class FollowRelationshipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private FollowRelationshipEntityKey primaryKey;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private UserEntity followed;

    public FollowRelationshipEntity(UserEntity user, UserEntity followed) {
        final var usersFollowedEntityKey = new FollowRelationshipEntityKey();
        usersFollowedEntityKey.setUser(user);
        usersFollowedEntityKey.setFollowed(followed);
        this.primaryKey = usersFollowedEntityKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FollowRelationshipEntity that = (FollowRelationshipEntity) o;
        return Objects.equals(primaryKey, that.primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKey);
    }
}
