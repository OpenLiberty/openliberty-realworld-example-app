package core.comments;

import java.sql.Timestamp;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import core.user.Profile;
import core.user.User;

@Entity(name = "Comment")
@Table(name = "Comment_Table")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "COMMENT_ID")
    private Long ARTICLE_ID;

    @Column(name = "createdAt")
    private Timestamp createdAt;
    @Column(name = "updatedAt")
    private Timestamp updatedAt;
    @Column(name = "body", nullable = false)
    private String body;
    @ManyToOne
    @JoinColumn(name = "author")
    private Profile author;

    public Comment() {
        Timestamp created = Timestamp.from(Instant.now());
        this.createdAt = created;
        this.updatedAt = created;
    }

    public Long getId() {
        return ARTICLE_ID;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Profile getAuthor() {
        return author;
    }

    public void setAuthor(Profile author) {
        this.author = author;
    }

    public JSONObject toJson(User userContext) {
        return new JSONObject()
            .put("id", ARTICLE_ID)
            .put("createdAt", createdAt.toInstant())
            .put("updatedAt", updatedAt.toInstant())
            .put("body", body)
            .put("author", author.toJson(userContext));
    }
}