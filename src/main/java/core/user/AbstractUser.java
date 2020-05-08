package core.user;

import java.util.HashSet;
import java.util.Set;

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
public abstract class AbstractUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID")
    private Long USER_ID;

    @ManyToMany
    @JoinTable(name = "FOLLOWED_BY",
        joinColumns = { @JoinColumn(name = "celeb", referencedColumnName = "USER_ID") },
        inverseJoinColumns = { @JoinColumn(name = "follower", referencedColumnName = "USER_ID")})
    private Set<User> followedBy = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "favorited")
    private Set<Article> favorited = new HashSet<>();

    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "userPassword", nullable = false)
    private String password;
    @Column(name = "bio", nullable = true)
    private String bio;
    @Column(name = "image", nullable = true)
    private String image;

    public Long getId() {
        return USER_ID;
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

    public String getImg() {
        return image;
    }

    public void setImg(String image) {
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
        System.out.println("Updating properties...");
        
        if (email != null && ! "".equals(email)) {
            System.out.println("Updating email");
            this.email = email;
        }
        if (username != null && ! "".equals(username)) {
            System.out.println("Updating username");
            this.username = username;
        }
        if (password != null && ! "".equals(password)) {
            System.out.println("Updating password");
            this.password = password;
        }
        if (image != null) {
            System.out.println("Update image");
            this.image = image;
        }
        if (bio != null) {
            System.out.println("Updating bio");
            this.bio = bio;
        }
    }

}