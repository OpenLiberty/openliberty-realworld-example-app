package application.rest;

import java.util.Collections;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import application.errors.ValidationMessages;
import core.user.Profile;
import dao.UserContext;

@RequestScoped
@Path("/profiles")
@RolesAllowed("users")
public class ProfilesEndpoint {

    @Inject
    private UserContext uc;

    @Inject
    private JsonWebToken jwt;

    /* Get Profile */
    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @PermitAll
    @Tag(ref = "Profile")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Get Profile", description = "Returns a Profile")
    @APIResponse(responseCode = "200", description = "Profile retrieved", content = @Content(examples = @ExampleObject(ref = "Profile_returned")))
    public Response getProfile(@PathParam("username") @Parameter(example = "jake") String username) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        return wrapResponse(userId, username);
    }

    /* Follow Profile */
    @POST
    @Path("/{username}/follow")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "Profile")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Follow User", description = "Returns a Profile")
    @APIResponse(responseCode = "200", description = "Profile followed", content = @Content(examples = @ExampleObject(ref = "Profile_following_returned")))
    public Response followUser(@PathParam("username") @Parameter(example = "jake") String username) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        uc.followProfile(userId, username);
        return wrapResponse(userId, username);
    }

    /* Unfollow Profile */
    @DELETE
    @Path("/{username}/follow")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Tag(ref = "Profile")
    @SecurityRequirement(name = "Authentication")
    @Operation(summary = "Unfollow User", description = "Returns a Profile")
    @APIResponse(responseCode = "200", description = "Profile unfollowed", content = @Content(examples = @ExampleObject(ref = "Profile_returned")))
    public Response unfollowUser(@PathParam("username") @Parameter(example = "jake") String username) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        uc.unfollowProfile(userId, username);
        return wrapResponse(userId, username);
    }

    private Response wrapResponse(Long userId, String username) {
        Profile responseBody = uc.findProfileByUsername(userId, username);
        if (responseBody == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ValidationMessages.throwError(ValidationMessages.PROFILE_NOT_FOUND)).build();
        } else {
            return Response.ok(Collections.singletonMap("profile", responseBody)).build();
        }
    }
}