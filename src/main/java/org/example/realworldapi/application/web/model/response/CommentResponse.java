package org.example.realworldapi.application.web.model.response;

import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbNillable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.realworldapi.domain.model.comment.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
//@JsonRootName("comment")
@JsonbNillable(true)
public class CommentResponse {

    private UUID id;

    //    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    //    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    private String body;
    private ProfileResponse author;

    public CommentResponse(Comment comment, ProfileResponse author) {
        this.id = comment.getId();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.body = comment.getBody();
        this.author = author;
    }
}
