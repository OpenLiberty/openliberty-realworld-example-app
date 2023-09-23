package org.example.realworldapi.application.web.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestWrapper {

    private LoginRequest user;

    @Override
    public String toString() {
        return "LoginRequestWrapper{" +
                "user=" + user +
                '}';
    }
}
