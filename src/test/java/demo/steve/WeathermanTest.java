package demo.steve;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class WeathermanTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/weather/forecast")
          .then()
             .statusCode(200);
             //.body(is("Hello RESTEasy forecast"));
    }

}