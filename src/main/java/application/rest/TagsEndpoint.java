package application.rest;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.json.JSONObject;

import dao.ArticleDao;

@RequestScoped
@Path("/tags")
public class TagsEndpoint {

    @Inject
    private ArticleDao articleDao;

    /* Get Tags */
    // Will return list of unique tags
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(ref = "Tags")
    @Operation(summary = "Get Tags", description = "Returns a list of all the tags")
    @APIResponse(responseCode = "200", description = "All tags", content = @Content(examples = @ExampleObject(ref = "Tags_returned")))
    public Response getTags() {
        List<String> tags = articleDao.getTags();
        return Response.ok(new JSONObject().put("tags", tags).toString()).build();
    }
}