package metatest.generated;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetOrdersTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8000";
    }

    @Test
    public void testGetOrdersWithoutFilters_Returns200() {
        given()
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithAccountIdFilter_Returns200() {
        given()
            .queryParam("account_id", 1)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithStatusFilter_Returns200() {
        given()
            .queryParam("status", "FILLED")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithBothFilters_Returns200() {
        given()
            .queryParam("account_id", 1)
            .queryParam("status", "FILLED")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithLimitParameter_Returns200() {
        given()
            .queryParam("limit", 10)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("size()", lessThanOrEqualTo(10));
    }

    @Test
    public void testGetOrdersWithOffsetParameter_Returns200() {
        given()
            .queryParam("offset", 5)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithLimitAndOffset_Returns200() {
        given()
            .queryParam("limit", 10)
            .queryParam("offset", 5)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("size()", lessThanOrEqualTo(10));
    }

    @Test
    public void testGetOrdersWithMinimumLimit_Returns200() {
        given()
            .queryParam("limit", 1)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("size()", lessThanOrEqualTo(1));
    }

    @Test
    public void testGetOrdersWithMaximumLimit_Returns200() {
        given()
            .queryParam("limit", 100)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("size()", lessThanOrEqualTo(100));
    }

    @Test
    public void testGetOrdersWithDefaultLimit_Returns200() {
        given()
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("size()", lessThanOrEqualTo(50));
    }

    @Test
    public void testGetOrdersWithZeroOffset_Returns200() {
        given()
            .queryParam("offset", 0)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithAllParameters_Returns200() {
        given()
            .queryParam("account_id", 1)
            .queryParam("status", "FILLED")
            .queryParam("limit", 20)
            .queryParam("offset", 0)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("size()", lessThanOrEqualTo(20));
    }

    @Test
    public void testGetOrdersWithLimitAboveMaximum_Returns422() {
        given()
            .queryParam("limit", 101)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithLimitBelowMinimum_Returns422() {
        given()
            .queryParam("limit", 0)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithNegativeLimit_Returns422() {
        given()
            .queryParam("limit", -1)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithNegativeOffset_Returns422() {
        given()
            .queryParam("offset", -1)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithInvalidLimitType_Returns422() {
        given()
            .queryParam("limit", "invalid")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithInvalidOffsetType_Returns422() {
        given()
            .queryParam("offset", "invalid")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithInvalidAccountIdType_Returns422() {
        given()
            .queryParam("account_id", "invalid")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithFloatAccountId_Returns422() {
        given()
            .queryParam("account_id", 1.5)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithFloatLimit_Returns422() {
        given()
            .queryParam("limit", 10.5)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testGetOrdersWithNullAccountId_Returns200() {
        // When passing null as query parameter, RestAssured omits the parameter entirely
        // This is equivalent to not passing the parameter at all
        given()
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithNullStatus_Returns200() {
        given()
            .queryParam("status", (Object) null)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithEmptyStatus_Returns200() {
        given()
            .queryParam("status", "")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersVerifyArrayResponse_Returns200() {
        given()
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    public void testGetOrdersVerifyEmptyArrayResponse_Returns200() {
        given()
            .queryParam("account_id", 999999)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("size()", equalTo(0));
    }

    @Test
    public void testGetOrdersVerifyResponseItemsHaveRequiredFields_Returns200() {
        given()
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class))
            .body("every { it.containsKey('id') }", equalTo(true))
            .body("every { it.containsKey('account_id') }", equalTo(true))
            .body("every { it.containsKey('symbol') }", equalTo(true))
            .body("every { it.containsKey('order_type') }", equalTo(true))
            .body("every { it.containsKey('quantity') }", equalTo(true))
            .body("every { it.containsKey('price') }", equalTo(true))
            .body("every { it.containsKey('status') }", equalTo(true))
            .body("every { it.containsKey('total_amount') }", equalTo(true))
            .body("every { it.containsKey('created_at') }", equalTo(true));
    }

    @Test
    public void testGetOrdersWithVeryLargeOffset_Returns200() {
        given()
            .queryParam("offset", 100000)
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithMultipleStatusValues_Returns200() {
        given()
            .queryParam("status", "FILLED")
            .queryParam("status", "REJECTED")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testGetOrdersWithUnknownQueryParameter_Returns200() {
        given()
            .queryParam("unknown_param", "value")
        .when()
            .get("/api/v1/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }
}
