package application.rest;

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
import org.json.JSONObject;

import application.errors.ValidationMessages;
import dao.UserContext;

@RequestScoped
@Path("/profiles")
@RolesAllowed("users")
public class ProfilesAPI {

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
    public Response getProfile(@PathParam("username") String username) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        return wrapResponse(userId, username);
    }

    /* Follow Profile */
    @POST
    @Path("/{username}/follow")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response followUser(@PathParam("username") String username) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        uc.followProfile(userId, username);
        return wrapResponse(userId, username);
    }

    /* Unfollow Profile */
    @DELETE
    @Path("/{username}/follow")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response unfollowUser(@PathParam("username") String username) {
        Long userId = (jwt == null) ? null : jwt.getClaim("id");
        uc.unfollowProfile(userId, username);
        return wrapResponse(userId, username);
    }

    private Response wrapResponse(Long userId, String username) {
        JSONObject responseBody = uc.findProfileByUsernameJson(userId, username);
        if (responseBody == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ValidationMessages.throwError(ValidationMessages.PROFILE_NOT_FOUND)).build();
        } else {
            return Response.ok(responseBody.toString()).build();
        }
    }
}