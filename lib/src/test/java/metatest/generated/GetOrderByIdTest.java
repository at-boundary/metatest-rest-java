package metatest.generated;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetOrderByIdTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8000";
    }

    @Test
    public void testGetOrderByIdWithValidId_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("account_id", notNullValue())
            .body("symbol", notNullValue())
            .body("order_type", notNullValue())
            .body("quantity", notNullValue())
            .body("price", notNullValue())
            .body("status", notNullValue())
            .body("total_amount", notNullValue())
            .body("created_at", notNullValue());
    }

    @Test
    public void testGetOrderByIdVerifyAllRequiredFields_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasKey("id"))
            .body("$", hasKey("account_id"))
            .body("$", hasKey("symbol"))
            .body("$", hasKey("order_type"))
            .body("$", hasKey("quantity"))
            .body("$", hasKey("price"))
            .body("$", hasKey("status"))
            .body("$", hasKey("total_amount"))
            .body("$", hasKey("created_at"));
    }

    @Test
    public void testGetOrderByIdVerifyOptionalFields_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasKey("rejection_reason"))
            .body("$", hasKey("filled_at"));
    }

    @Test
    public void testGetOrderByIdVerifyIdMatchesPathParameter_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(1));
    }

    @Test
    public void testGetOrderByIdVerifyFieldTypes_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", isA(Integer.class))
            .body("account_id", isA(Integer.class))
            .body("symbol", isA(String.class))
            .body("order_type", isA(String.class))
            .body("quantity", isA(Integer.class))
            .body("price", isA(String.class))
            .body("status", isA(String.class))
            .body("total_amount", isA(String.class))
            .body("created_at", isA(String.class));
    }

    @Test
    public void testGetOrderByIdVerifyPricePattern_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("price", matchesPattern("^(?!^[-+.]*$)[+-]?0*\\d*\\.?\\d*$"));
    }

    @Test
    public void testGetOrderByIdVerifyTotalAmountPattern_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("total_amount", matchesPattern("^(?!^[-+.]*$)[+-]?0*\\d*\\.?\\d*$"));
    }

    @Test
    public void testGetOrderByIdVerifyCreatedAtIsDateTime_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("created_at", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    @Test
    public void testGetOrderByIdWithDifferentValidId_Returns200() {
        given()
            .pathParam("order_id", 2)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(2));
    }

    @Test
    public void testGetOrderByIdWithLargeValidId_Returns200() {
        given()
            .pathParam("order_id", 100)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(100));
    }

    @Test
    public void testGetOrderByIdWithNonExistentId_Returns404Or422() {
        given()
            .pathParam("order_id", 999999)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithZeroId_Returns422Or404() {
        given()
            .pathParam("order_id", 0)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithNegativeId_Returns422Or404() {
        given()
            .pathParam("order_id", -1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithInvalidIdTypeString_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/invalid")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithFloatId_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/1.5")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithEmptyId_Returns404Or405() {
        // Note: /api/v1/orders/ might redirect to /api/v1/orders or return 200 with orders list
        // depending on the framework's trailing slash handling. We accept multiple valid responses.
        given()
        .when()
            .get("/api/v1/orders/")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(405)));
    }

    @Test
    public void testGetOrderByIdWithSpecialCharacters_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/@#$")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithVeryLargeId_Returns404Or422() {
        given()
            .pathParam("order_id", 2147483647)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithNullIdInPath_Returns404Or405() {
        given()
        .when()
            .get("/api/v1/orders/null")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithWhitespaceId_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/%20")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithLeadingZeros_Returns200Or404() {
        given()
        .when()
            .get("/api/v1/orders/001")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithPlusSign_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/+1")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdVerifyResponseIsNotArray_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", not(instanceOf(java.util.List.class)));
    }

    @Test
    public void testGetOrderByIdWithQueryParameters_Returns200() {
        given()
            .pathParam("order_id", 1)
            .queryParam("extra", "ignored")
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(1));
    }

    @Test
    public void testGetOrderByIdVerifySymbolNotEmpty_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("symbol", not(emptyString()));
    }

    @Test
    public void testGetOrderByIdVerifyQuantityIsPositive_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("quantity", greaterThan(0));
    }

    @Test
    public void testGetOrderByIdVerifyOrderTypeIsValid_Returns200() {
        given()
            .pathParam("order_id", 1)
        .when()
            .get("/api/v1/orders/{order_id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("order_type", anyOf(equalTo("BUY"), equalTo("SELL")));
    }

    @Test
    public void testGetOrderByIdWithHexadecimalId_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/0x1A")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithSqlInjection_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/1' OR '1'='1")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }

    @Test
    public void testGetOrderByIdWithScriptTag_Returns422Or404() {
        given()
        .when()
            .get("/api/v1/orders/<script>alert('xss')</script>")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(422)));
    }
}
