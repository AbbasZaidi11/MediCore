package test.java;

import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class AuthIntegrationTest {

    @BeforeAll
    // It's strictly a JUnit annotation.
    public static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOKWithValidToken() {
        // 1. Arrange
        // 2. act
        // 3. assert
        String loginPayload = """
        {
           "email":"testuser@test.com",
           "password":"password123"
        }
        """;
        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                // request setup Headers, body, auth token, query params — anything you're sending
                .post("/auth/login")
                .then()
                // assertions .statusCode(), .body("fieldName", matcher)
                // the matchers like notNullValue(), equalTo() come from Hamcrest
                // which is just standard Java testing.
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();
        System.out.println("Generated Token " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        // 1.Arrange
        // 2. act
        // 3. assert

        String loginPayload = """
        {
           "email":"invalid_user@test.com",
           "password":"wrongpassword"
        }
        """;
        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

}
