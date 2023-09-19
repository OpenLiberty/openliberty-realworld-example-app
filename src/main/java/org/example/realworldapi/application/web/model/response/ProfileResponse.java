package org.example.realworldapi.application.web.model.response;

import jakarta.json.bind.annotation.JsonbNillable;
import lombok.Getter;
import lombok.Setter;
import org.example.realworldapi.domain.model.user.User;

@Getter
@Setter
//@JsonRootName("profile")
@JsonbNillable(true)
public class ProfileResponse {

    private String username;
    private String bio;
    private String image;
    private boolean following;

    public ProfileResponse(User user) {
        this.username = user.getUsername();
        this.bio = user.getBio();
        this.image = user.getImage();
    }
}
