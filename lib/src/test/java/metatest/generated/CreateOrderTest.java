package metatest.generated;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateOrderTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8000";
    }

    @Test
    public void testCreateOrderWithValidBuyOrder_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("account_id", equalTo(1))
            .body("symbol", equalTo("AAPL"))
            .body("order_type", equalTo("BUY"))
            .body("quantity", equalTo(10))
            .body("price", notNullValue())
            .body("status", notNullValue())
            .body("total_amount", notNullValue())
            .body("created_at", notNullValue());
    }

    @Test
    public void testCreateOrderWithValidSellOrder_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"TSLA\",\n" +
                  "  \"order_type\": \"SELL\",\n" +
                  "  \"quantity\": 5\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("account_id", equalTo(1))
            .body("symbol", equalTo("TSLA"))
            .body("order_type", equalTo("SELL"))
            .body("quantity", equalTo(5))
            .body("price", notNullValue())
            .body("status", notNullValue())
            .body("total_amount", notNullValue())
            .body("created_at", notNullValue());
    }

    @Test
    public void testCreateOrderWithMaxLengthSymbol_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 1\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("symbol", equalTo("AAPL"))
            .body("account_id", equalTo(1))
            .body("order_type", equalTo("BUY"))
            .body("quantity", equalTo(1));
    }

    @Test
    public void testCreateOrderWithMinimumQuantity_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"MSFT\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 1\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .body("quantity", equalTo(1));
    }

    @Test
    public void testCreateOrderWithLargeQuantity_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"GOOGL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10000\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .body("quantity", equalTo(10000));
    }

    @Test
    public void testCreateOrderWithMissingAccountId_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithMissingSymbol_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithMissingOrderType_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithMissingQuantity_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\"\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithNullAccountId_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": null,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithNullSymbol_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": null,\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithNullOrderType_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": null,\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithNullQuantity_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": null\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithEmptySymbol_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithInvalidOrderType_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"HOLD\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithLowercaseOrderType_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"buy\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithZeroAccountId_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 0,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithNegativeAccountId_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": -1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithZeroQuantity_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 0\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithNegativeQuantity_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": -5\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithSymbolTooLong_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"ABCDEFGHIJK\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithInvalidAccountIdType_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": \"invalid\",\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithInvalidQuantityType_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": \"ten\"\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithEmptyBody_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderWithMalformedJson_Returns400OrServerError() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(500)));
    }

    @Test
    public void testCreateOrderWithExtraFields_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10,\n" +
                  "  \"extra_field\": \"should_be_ignored\"\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201);
    }

    @Test
    public void testCreateOrderWithFloatQuantity_Returns422() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10.5\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("detail", notNullValue());
    }

    @Test
    public void testCreateOrderVerifyRequiredResponseFields_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
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
    public void testCreateOrderVerifyOptionalFieldsStructure_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .body("$", hasKey("rejection_reason"))
            .body("$", hasKey("filled_at"));
    }

    @Test
    public void testCreateOrderVerifyIdIsInteger_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .body("id", isA(Integer.class));
    }

    @Test
    public void testCreateOrderVerifyPriceIsString_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .body("price", isA(String.class));
    }

    @Test
    public void testCreateOrderVerifyTotalAmountIsString_Returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("{\n" +
                  "  \"account_id\": 1,\n" +
                  "  \"symbol\": \"AAPL\",\n" +
                  "  \"order_type\": \"BUY\",\n" +
                  "  \"quantity\": 10\n" +
                  "}")
        .when()
            .post("/api/v1/orders")
        .then()
            .statusCode(201)
            .body("total_amount", isA(String.class));
    }
}
