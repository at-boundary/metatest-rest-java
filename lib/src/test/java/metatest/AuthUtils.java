package metatest;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class AuthUtils {

    public static final String USERNAME = "testuser";
    public static final String PASSWORD = "test1234";

    public static String generateToken(){
        String requestBody = """
                {
                    "username": "testuser",
                    "password": "test1234"
                }
            """;

        return "Bearer " + given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/login").getBody().jsonPath().get("token");
    }

}
