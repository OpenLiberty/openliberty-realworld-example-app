package org.example.realworldapi.application.web.model.request;

import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.domain.model.user.LoginUserInput;

@Getter
@Setter
//@JsonRootName("user")
@JsonbNillable(true)
public class LoginRequest {

    @NotBlank(message = ValidationMessages.EMAIL_MUST_BE_NOT_BLANK)
    private String email;

    @NotBlank(message = ValidationMessages.PASSWORD_MUST_BE_NOT_BLANK)
    private String password;

    public LoginUserInput toLoginUserInput() {
        return new LoginUserInput(this.email, this.password);
    }
}
