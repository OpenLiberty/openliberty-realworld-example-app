package core.user;

import java.util.HashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbNillable;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import core.article.Article;

@MappedSuperclass
@JsonbNillable
public abstract class AbstractUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID")
    @JsonbTransient
    private Long userID;

    @ManyToMany
    @JoinTable(name = "FOLLOWED_BY",
        joinColumns = { @JoinColumn(name = "celeb", referencedColumnName = "USER_ID") },
        inverseJoinColumns = { @JoinColumn(name = "follower", referencedColumnName = "USER_ID")})
    @JsonbTransient
    private Set<User> followedBy = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "favorited")
    @JsonbTransient
    private Set<Article> favorited = new HashSet<>();

    @Column(name = "email", nullable = false, unique = true)
    protected String email;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "userPassword", nullable = false)
    private String password;
    @Column(name = "bio", nullable = true)
    private String bio;
    @Column(name = "image", nullable = true)
    private String image;

    public Long getUserID() {
        return userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonbTransient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<User> getFollowedBy() {
        return followedBy;
    }

    public boolean checkFollowedBy(User user) {
        return followedBy.contains(user);
    }

    public void follow(User user) {
        followedBy.add(user);
    }

    public void unfollow(User user) {
        followedBy.remove(user);
    }

    public boolean checkFavorited(Article article) {
        return favorited.contains(article);
    }

    public void favorite(Article article) {
        favorited.add(article);
    }

    public void unfavorite(Article article) {
        favorited.remove(article);
    }
    
    public void update(String email, String username, String password, String image, String bio) {
        if (email != null && ! "".equals(email)) {
            this.email = email;
        }
        if (username != null && ! "".equals(username)) {
            this.username = username;
        }
        if (password != null && ! "".equals(password)) {
            this.password = password;
        }
        if (image != null) {
            this.image = image;
        }
        if (bio != null) {
            this.bio = bio;
        }
    }

}