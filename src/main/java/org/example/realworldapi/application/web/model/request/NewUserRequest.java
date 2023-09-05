package org.example.realworldapi.application.web.model.request;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;
import org.example.realworldapi.domain.model.user.CreateUserInput;

@Getter
@Setter
@JsonRootName("user")
public class NewUserRequest {
    private String username;
    private String email;
    private String password;

    public CreateUserInput toCreateUserInput() {
        return new CreateUserInput(this.username, this.email, this.password);
    }
}
