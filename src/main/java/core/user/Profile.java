package core.user;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity(name = "Profile")
@Table(name = "USER_TABLE")
public class Profile extends AbstractUser {
    
    @JsonbTransient
    String email;
    
    @Transient
    private boolean following = false;
    
    public boolean getFollowing() {
        return following;
    }
    
    public void setFollowing(User userContext) {
        following = this.checkFollowedBy(userContext);
    }

    public void followedBy(User userContext) {
        this.getFollowedBy().add(userContext);
    }

    public void unfollowedBy(User userContext) {
        this.getFollowedBy().remove(userContext);
    }
}