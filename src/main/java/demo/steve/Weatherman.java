package demo.steve;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class Weatherman {

    private static final String WEATHER_API = "http://www.7timer.info/bin/api.pl?lon=%s&lat=%s&product=%s&output=%s";
    private static final String ZIPCODE_API = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&q=%s";

    private static final String DEFAULT_ZIPCODE = "20500";
    private static final Map<String, String[]> CACHE = new HashMap<String, String[]>();

    private final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .followRedirects(Redirect.ALWAYS)
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @GET
    @Path("forecast")
    public String forecast(@QueryParam("output") String output, @QueryParam("zipcode") String zipcode)
            throws IOException, InterruptedException {
        System.out.println("forecast()"); 

        String[] coordinates = lookupCoordinates(zipcode);
        HttpRequest request = buildRequest(coordinates, output, "two");

        System.out.printf("Calling %s\n", request.uri());
        return httpClient.send(request, BodyHandlers.ofString()).body();
    }

    @GET
    @Path("observation")
    public String observation(@QueryParam("output") String output, @QueryParam("zipcode") String zipcode) throws IOException, InterruptedException {
        System.out.println("observation()");

        String[] coordinates = lookupCoordinates(zipcode);
        HttpRequest request = buildRequest(coordinates, output, "civillight");

        System.out.printf("Calling %s\n", request.uri());
        return httpClient.send(request, BodyHandlers.ofString()).body();
    }

    /**
     * Builds an HTTP Request to call the Weather API
     * 
     * @param coordinates Tightly coupled [latitude][longitude] String pair
     * @param output Output type, either JSON or XML.  No other validation is performed
     * @param type One of 5 expected types as defined by the Weather API endpoint
     * @return An abstracted HTTP object to call the weather API
     */
    private final HttpRequest buildRequest(String[] coordinates, String output, String type) {
        return HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(String.format(WEATHER_API, coordinates[0], coordinates[1], type, (output != null ? output : "json"))))
            .build();
    }

    /**
     * Takes in zipcodes and returns coordinates in a String array of [latitude][longitude]
     * 
     * Results are cached in-memory so that subsequenty zipcode lookups don't need to call the public API.
     * 
     * @param zipcode The zipcode to look up
     * @return A coordinate pair of Strings
     * @throws IOException If there was HTTP connectivity issues
     * @throws InterruptedException If there was HTTP connectivity issues
     */
    private final String[] lookupCoordinates(String zipcode) throws IOException, InterruptedException {
        if (zipcode == null || zipcode.equals("")) {
            System.out.println("zipcode not passed in, defaulting to " + DEFAULT_ZIPCODE);
            zipcode = DEFAULT_ZIPCODE;
        }

        String[] coordinates = null;
        if ((coordinates = CACHE.get(zipcode)) != null) {
            System.out.printf("%s found in cache\n", zipcode);
            return coordinates; 
        }

        HttpRequest request = HttpRequest.newBuilder()
            .GET()      
            .uri(URI.create(String.format(ZIPCODE_API, zipcode)))
            .build();
        
        System.out.printf("Calling %s\n", request.uri());
        JsonElement root = JsonParser.parseString(httpClient.send(request, BodyHandlers.ofString()).body());

        String lat = root.getAsJsonObject().get("records").getAsJsonArray().get(0).getAsJsonObject().get("fields").getAsJsonObject().get("latitude").getAsString();
        String lon = root.getAsJsonObject().get("records").getAsJsonArray().get(0).getAsJsonObject().get("fields").getAsJsonObject().get("longitude").getAsString();
        coordinates = new String[]{lat, lon};

        CACHE.put(zipcode, coordinates);

        return coordinates;
    }
}