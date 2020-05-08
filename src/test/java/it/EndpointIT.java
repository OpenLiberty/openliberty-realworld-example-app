package it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.Test;

public class EndpointIT {
    private static final String baseURL = "http://localhost:9080";
    
    @Test
    public void basicHealth() {
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(baseURL + "/health");
        Response response = target.request().get();

        assertEquals("Health status code unexpected for " + baseURL + "/health.", Response.Status.OK.getStatusCode(), response.getStatus());
        String json = response.readEntity(String.class);
        assertTrue("Health message did not find outcome up in the response: " + json, json.contains("\"outcome\":\"UP\""));
        response.close();
    }

    @Test
    public void basicGetArticleEmpty() {
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(baseURL + "/api/articles");
        Response response = target.request().get();

        assertEquals("Did not retrieve 200 from " + baseURL + "/api/articles.", Response.Status.OK.getStatusCode(), response.getStatus());

        String json = response.readEntity(String.class);
        JSONObject emptyArticles = new JSONObject().put("articlesCount", 0).put("articles", new String[0]);
        assertEquals("Returned list should be empty", emptyArticles.toString(), json);
        response.close();
    }

    // @Test
    // public void createUser() {
    //     Client client = ClientBuilder.newClient();

    //     WebTarget target = client.target(baseURL + "/api/users");

    //     CreateUser testRequestBody = new CreateUser();
    //     User testUser = new User();
    //     testUser.setEmail("johnnyjoestar@ibm.com");
    //     testUser.setPassword("password");
    //     testUser.setUsername("Johnny");
    //     testRequestBody.setUser(testUser);

    //     Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(testRequestBody));

    //     assertEquals("Did not retrieve 200 from " + baseURL + "/api/users.", Response.Status.CREATED.getStatusCode(), response.getStatus());
    //     response.close();
    // }
}