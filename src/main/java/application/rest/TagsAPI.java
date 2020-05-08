package application.rest;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import dao.ArticleDao;

@RequestScoped
@Path("/tags")
public class TagsAPI {

    @Inject
    private ArticleDao articleDao;

    /* Get Tags */
    // Will return list of unique tags
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTags() {
        List<String> tags = articleDao.getTags();
        return Response.ok(new JSONObject().put("tags", tags).toString()).build();
    }
}