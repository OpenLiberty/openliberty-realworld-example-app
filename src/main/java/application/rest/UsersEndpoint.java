package application.rest;

import java.util.Collections;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.json.JSONException;

import com.ibm.websphere.security.jwt.InvalidBuilderException;
import com.ibm.websphere.security.jwt.InvalidClaimException;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtException;
import com.ibm.websphere.security.jwt.KeyException;

import application.errors.ValidationMessages;
//import application.rest.api.UsersAPI;
import core.user.CreateUser;
import core.user.User;
import dao.UserDao;
import security.JwtGenerator;

@RequestScoped
@Path("/")
@RolesAllowed("users")
public class UsersEndpoint {
    private JwtGenerator tknGenerator = new JwtGenerator();

    @Inject
    private UserDao userDao;

    @Inject
    private JsonWebToken jwt;

    /* Register */
    @POST
    @Path("/users")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "Login")
    @Operation(summary = "Create a new User", description = "Enter a unique username and email")
    @RequestBody(content = @Content(examples = @ExampleObject(ref = "User_registration")))
    @APIResponse(responseCode = "200", description = "New User created", content = @Content(examples = @ExampleObject(ref = "User_returned")))
    public Response createUser(CreateUser requestBody)
            throws JSONException, JwtException, InvalidBuilderException, InvalidClaimException, KeyException {
        User user = requestBody.getUser();
        String username = user.getUsername();
        String email = user.getEmail();
        String password = user.getPassword();

        // Required fields
        if (username == null || email == null || password == null || 
            username.equals("") || email.equals("") || password.equals("")) {
            return Response.status(422)
                .entity(ValidationMessages.throwError(ValidationMessages.REGISTRATION_REQUIREMENTS_BLANK))
                .build();
        }
        if (userDao.userExists(username)) {
            return Response.status(422)
                .entity(ValidationMessages.throwError(ValidationMessages.DUPLICATE_USERNAME))
                .build();
        }
        if (userDao.emailExists(email)) {
            return Response.status(422)
                .entity(ValidationMessages.throwError(ValidationMessages.DUPLICATE_EMAIL))
                .build();
        }
        userDao.createUser(user);
        return wrapUserResponse(user);
    }

    /* Login */
    @POST
    @Path("/users/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(ref = "Login")
    @Operation(summary = "Login as an existing User", description = "Enter a valid username and email")
    @RequestBody(content = @Content(examples = @ExampleObject(ref = "User_authentication")))
    @APIResponse(responseCode = "200", description = "User logged in", content = @Content(examples = @ExampleObject(ref = "User_returned")))
    public Response loginUser(CreateUser requestBody)
            throws JSONException, JwtException, InvalidBuilderException, InvalidClaimException, KeyException {

        User loginInfo = requestBody.getUser();
        String email = loginInfo.getEmail();
        String password = loginInfo.getPassword();

        // Required fields
        if (email == null || password == null ||
            email.equals("") || password.equals("")) {
            return Response.status(422)
                .entity(ValidationMessages.throwError(ValidationMessages.LOGIN_REQUIREMENTS_BLANK))
                .build();
        }
        if (!userDao.emailExists(email)) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.EMAIL_NOT_FOUND))
                .build();
        }

        User user = userDao.login(loginInfo.getEmail(), loginInfo.getPassword());
        if (user == null) {
            return Response.status(422)
                .entity(ValidationMessages.throwError(ValidationMessages.LOGIN_FAIL))
                .build();
        }
        return wrapUserResponse(user);
    }

    /* Current User */
    @GET
    @Path("/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(ref = "User")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Get Current User", description = "Returns a User that's the current user")
    @APIResponse(responseCode = "200", description = "Current User retrieved", content = @Content(examples = @ExampleObject(ref = "User_returned")))
    public Response getCurrent() throws InvalidTokenException, InvalidConsumerException, JSONException, JwtException,
            InvalidBuilderException, InvalidClaimException, KeyException {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        User currentUser = userDao.findUser(userId);
        if (currentUser == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.USER_NOT_FOUND))
                .build();
        }
        return wrapUserResponse(currentUser);
    }

    /* Update User */
    @PUT
    @Path("/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "User")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Update User", description = "Returns the updated User. Accepted fields: email, username, password, image, bio")
    @RequestBody(content = @Content(examples = @ExampleObject(ref = "User_update")))
    @APIResponse(responseCode = "200", description = "Current User updated", content = @Content(examples = @ExampleObject(ref = "User_updated_returned")))
    public Response update(CreateUser requestBody)
            throws JSONException, JwtException, InvalidBuilderException, InvalidClaimException, KeyException {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        User currentUser = userDao.findUser(userId);
        if (currentUser == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.USER_NOT_FOUND))
                .build();
        }
        if (!userId.equals(currentUser.getUserID())) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.UPDATING_DIFFERENT_USER))
                .build();
        }
        User newUser = userDao.updateUser(currentUser, requestBody.getUser());
        return wrapUserResponse(newUser);
    }

    // Helper methods
    private Response wrapUserResponse(User user)
            throws JSONException, JwtException, InvalidBuilderException, InvalidClaimException, KeyException {
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ValidationMessages.throwError(ValidationMessages.USER_NOT_FOUND))
                .build();
        }

        return Response.ok(Collections.singletonMap("user", user)).build();
    }

}
