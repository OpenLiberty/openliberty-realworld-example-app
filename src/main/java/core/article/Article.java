package core.article;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.github.slugify.Slugify;

import core.comments.Comment;
import core.user.Profile;
import core.user.User;

@Entity(name = "Article")
@Table(name = "Article_Table")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ARTICLE_ID")
    @JsonbTransient
    private Long ARTICLE_ID;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "comments")
    @JsonbTransient
    private List<Comment> comments;

    @Column(name = "slug")
	private String slug;
    @Column(name = "title", nullable =  false)
	private String title;
    @Column(name = "description", nullable = false)
	private String description;
    @Column(name = "body", nullable = false)
	private String body;
    @Column(name = "tagList", nullable = true)
    public List<String> tagList;
    @Column(name = "createdAt")
	private Timestamp createdAt;
    @Column(name = "updatedAt")
	private Timestamp updatedAt;
    @Column(name = "favoritesCount")
	private int favoritesCount;
    @ManyToOne(targetEntity = Profile.class)
	private Profile author;

    public Article() {
        Timestamp created = Timestamp.from(Instant.now());
        this.createdAt = created;
        this.updatedAt = created;
        this.favoritesCount = 0;
    }

    public String getSlug() {
        return slug;
    }

    public void initSlug() {
        this.slug = new Slugify().slugify(this.title);
    }

    public void setSlug(String title) {
        this.slug = new Slugify().slugify(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public Instant getCreatedAt() {
        return createdAt.toInstant();
    }

    public Instant getUpdatedAt() {
        return updatedAt.toInstant();
    }

    public void setUpdatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public int getFavoritesCount() {
        return favoritesCount;
    }

    public void upFavoritesCount() {
        ++this.favoritesCount;
    }

    public void downFavoritesCount() {
        --this.favoritesCount;
    }

    public Profile getAuthor() {
        return this.author;
    }

    public void setAuthor(Profile author) {
        this.author = author;
    }

    public List<Comment> getComments() {
        return this.comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }
    
    @Transient
    private boolean favorited = false;
    
    public boolean getFavorited() {
        return favorited;
    }
    
    public void setFavorited(User userContext) {
        favorited = userContext == null ? false : userContext.checkFavorited(this);
    }

    public void update(String title, String description, String body) {
        if (title != null && ! "".equals(title)) {
            this.title = title;
            this.slug = new Slugify().slugify(title);
        }
        if (description != null && ! "".equals(description)) {
            this.description = description;
        }
        if (body != null && ! "".equals(body)) {
            this.body = body;
        }
    }

}