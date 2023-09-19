package org.example.realworldapi.application.web.model.response;

import jakarta.json.bind.annotation.JsonbNillable;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
//@JsonRootName("errors")
@JsonbNillable(true)
public class ErrorResponse {

    private List<String> body;

    public ErrorResponse() {
        this.body = new LinkedList<>();
    }

    public ErrorResponse(String error) {
        this();
        this.body.add(error);
    }

    public ErrorResponse(List<String> errors) {
        this.body = errors;
    }
}
