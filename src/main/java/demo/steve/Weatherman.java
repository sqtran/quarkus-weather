package demo.steve;

import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/weather")
public class Weatherman {

    private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .followRedirects(Redirect.ALWAYS)
        .connectTimeout(Duration.ofSeconds(10))
        .authenticator(Authenticator.getDefault())
        .build();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("forecast")
    public String forecast() {
        System.out.println("forecast()");
        return "Hello RESTEasy forecast";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("observation")
    public String observation() throws IOException, InterruptedException {
        System.out.println("observation()");
        
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("http://www.7timer.info/bin/api.pl?lon=113.17&lat=23.09&product=astro&output=json"))
            .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
            .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        System.out.println(response.statusCode());

        System.out.println(response.body());

        System.out.println("Returning response now");

        return response.body();
    }
}