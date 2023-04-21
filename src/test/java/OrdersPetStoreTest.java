import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;
import pojo.DeleteOrderResponse;
import pojo.Order;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrdersPetStoreTest {

    private RequestSpecification baseRequestSpecification = new RequestSpecBuilder()
            .setBaseUri("https://petstore.swagger.io") //Base URI for the server
            .setContentType(ContentType.JSON) //HEADER (We only want Json, not XML, not something else)
            .build();

    @Test
    public void orderCRUDTest() {

        //CREATE
        int ORDER_ID = 1986;

        Order order = new Order();
        order.setId(ORDER_ID);
        order.setPetId(1234);
        order.setQuantity(1);
        order.setShipDate("2023-03-13T16:06:20");
        order.setOrderStatus("placed");
        order.setComplete(true);

        Order orderResponse = given().log().all().spec(baseRequestSpecification).body(order)
                .when()
                .post("/v2/store/order")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Order.class);

        Assert.assertEquals(orderResponse.getId(), ORDER_ID);

        //READ
        orderResponse = given().log().all().spec(baseRequestSpecification)
                .pathParams("orderId", ORDER_ID)
                .when()
                .get("v2/store/order/{orderId}")
                .then()
                .log().all()
                .extract().response().as(Order.class);

        Assert.assertEquals(orderResponse.getId(), ORDER_ID);

        // DELETE
        DeleteOrderResponse deleteOrderResponse = given().log().all().spec(baseRequestSpecification)
                .pathParams("orderId", ORDER_ID)
                .when()
                .delete("/v2/store/order/{orderId}")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(DeleteOrderResponse.class);

        Assert.assertEquals(deleteOrderResponse.getType(), "unknown");
    }

    // This currently fails as there is no validation for status enum values in Pets
    @Test
    public void inventoryTest(){

        Map response = given().log().all().spec(baseRequestSpecification)
            .when()
            .get("/v2/store/inventory")
            .then()
            .log().all()
            .statusCode(200)
            .extract().response().as(Map.class);

        Assert.assertEquals(response.size(),  3);
    }

    @Test
    public void deleteNonExistingOrderByIdTest(){

        int ORDER_ID = 6660;
        DeleteOrderResponse orderResponse3 = given().log().all().spec(baseRequestSpecification)
                .pathParams("orderId", ORDER_ID)
                .when()
                .delete("/v2/store/order/{orderId}")
                .then()
                .log().all()
                .statusCode(404)
                .extract().response().as(DeleteOrderResponse.class);

        Assert.assertEquals(orderResponse3.getType(), "unknown");
    }
}
