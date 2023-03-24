import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import pojo.User;
import pojo.UserResponse;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class UserTests {

    private RequestSpecification baseRequestSpecification = new RequestSpecBuilder()
                                                            .setBaseUri("https://petstore.swagger.io")
                                                            .setContentType(ContentType.JSON)
                                                            .build();

    @Test
    public void userCRUDTest(){

        User user1 = new User();
        user1.setId(2011);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setUsername("JohnDoeMarch2023");
        user1.setEmail("JohnDoeMarch2023@gmail.com");
        user1.setPassword("march2023");
        user1.setPhone("0123401234");
        user1.setUserStatus(2);

        User user2 = new User();
        user2.setId(2012);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setUsername("JaneDoeMarch2023");
        user2.setEmail("JaneDoeMarch2023@gmail.com");
        user2.setPassword("april2023");
        user2.setPhone("0123501235");
        user2.setUserStatus(3);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);


        //CREATE
        UserResponse response = given().log().all()
                                .spec(baseRequestSpecification)
                                .body(users)
                                .when()
                                .post("/v2/user/createWithArray")
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract().response().as(UserResponse.class);

        Assert.assertEquals(response.getMessage(),"ok");

        //READ 1st user
        User readUser1 = given().log().all().spec(baseRequestSpecification)
                .pathParams("username", user1.getUsername())
                .when()
                .get("/v2/user/{username}")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(User.class);

        Assert.assertEquals(readUser1.getId(),user1.getId());

        //READ 2nd user
        User readUser2 = given().log().all().spec(baseRequestSpecification)
                .pathParams("username", user2.getUsername())
                .when()
                .get("/v2/user/{username}")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(User.class);

        Assert.assertEquals(readUser2.getId(),user2.getId());

        //UPDATE user2

        user2.setEmail("raluca2012.I@gmail.com");

        UserResponse userResponse = given().log().all().spec(baseRequestSpecification)
                .pathParams("username", user2.getUsername())
                .body(user2)
                .when()
                .put("/v2/user/{username}")
                .then()
                .log().all()
                .extract().response().as(UserResponse.class);

        Assert.assertEquals(userResponse.getMessage(), "1");

       //LOG IN
       userResponse = given().log().all().spec(baseRequestSpecification)
                  .param("username",user2.getUsername())
                  .param("password",user2.getPassword())
                  .when()
                  .get("/v2/user/login")
                  .then()
                  .log().all()
                  .extract().response().as(UserResponse.class);

        Assert.assertEquals(userResponse.getCode(), 200);

        //LOG OUT
        userResponse = given().log().all().spec(baseRequestSpecification)
                .when()
                .get("/v2/user/logout")
                .then()
                .log().all()
                .extract().response().as(UserResponse.class);

        Assert.assertEquals(userResponse.getMessage(), "ok");

        //DELETE
            given().log().all().spec(baseRequestSpecification)
            .pathParams("username", user2.getUsername())
            .when()
            .delete("/v2/user/{username}")
            .then()
            .log().all()
            .statusCode(200);

        given().log().all().spec(baseRequestSpecification)
                .pathParams("username", user1.getUsername())
                .when()
                .delete("/v2/user/{username}")
                .then()
                .log().all()
                .statusCode(200);

    }

    @Test
    public void getNonExistingUserByUserNameTest(){

        UserResponse userResponse =
                given().log().all().spec(baseRequestSpecification)
                        .pathParams("username", "Georgy1979")
                .when()
                .get("/v2/user/{username}")
                .then()
                .log().all()
                .statusCode(404)
                .body("message", Matchers.equalTo("User not found"))
                .header("date",containsString("GMT"))
                .extract().response().as(UserResponse.class);

        Assert.assertEquals(userResponse.getCode(),1);

    }
}
