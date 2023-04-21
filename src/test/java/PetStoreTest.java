import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;
import pojo.Category;
import pojo.Pet;
import pojo.Tag;
import pojo.PetResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

public class PetStoreTest {

    private RequestSpecification jsonRequest = new RequestSpecBuilder()
            .setBaseUri("https://petstore.swagger.io") //Base URI for the server
            .setContentType(ContentType.JSON) //HEADER (We only want Json, not XML, not something else)
            .build();

    private RequestSpecification formDataRequest = new RequestSpecBuilder()
            .setBaseUri("https://petstore.swagger.io") //Base URI for the server
            .setContentType(ContentType.URLENC) //HEADER (FormData)
            .build();

    @Test
    public void petCRUDTest(){

        //CREATE
        String PET_NAME = "Johnny";
        String PET_ID = "1234";

        Tag tag = new Tag();
        tag.setId("9999");
        tag.setName("newTag");

        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(tag);

        Category category = new Category();
        category.setId("11");
        category.setName("newCategory");

        List<String> urlList = new ArrayList<>(Arrays.asList("https://images.app.goo.gl/ZHZJF3zehDZnN6hm7",
                                                               "https://images.app.goo.gl/hp16oDjyCnemmURm8"));

        Pet pet = new Pet();
        pet.setId(PET_ID);
        pet.setName(PET_NAME);
        pet.setStatus("pending");
        pet.setTags(tagList);
        pet.setCategory(category);
        pet.setPhotoUrls(urlList);

        Pet petResponse = given().log().all().spec(jsonRequest)
                .body(pet)
                .when()
                .post("v2/pet")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Pet.class);

        Assert.assertEquals(petResponse.getName(), PET_NAME );
        Assert.assertEquals(petResponse.getTags().get(0).getName(), "newTag");

        //UPDATE
        pet.setStatus("available");

        petResponse = given().log().all().spec(jsonRequest)
                .body(pet)
                .when()
                .put("/v2/pet")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Pet.class);

        Assert.assertEquals(petResponse.getStatus(), "available");

        //READ
        Pet[] petResponses = given().log().all().spec(jsonRequest)
                .param("status", "available")
                .when()
                .get("/v2/pet/findByStatus")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Pet[].class);

        for (Pet p : petResponses){
            Assert.assertEquals(p.getStatus(),"available");
        }

        //READ
        pet = given().log().all()
                .spec(jsonRequest)
                .pathParams("petId", PET_ID)
                .when()
                .get("/v2/pet/{petId}")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Pet.class);

        Assert.assertTrue(pet.getId().equals("1234"));


        //UPDATE
        PET_NAME = "Alexandra";
        String NEW_PET_STATUS = "sold";

        PetResponse response = given().log().all().spec(formDataRequest)
                .pathParams("petId", PET_ID )
                .param("name", PET_NAME)
                .param("status", NEW_PET_STATUS)
                .when()
                .post("v2/pet/{petId}")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(PetResponse.class);

        Assert.assertEquals(response.getCode(), 200);
        Assert.assertEquals(response.getType(),"unknown");
        Assert.assertEquals(response.getMessage(), PET_ID);

        // READ
        pet = given().log().all()
                .spec(jsonRequest)
                .pathParams("petId", PET_ID)
                .when()
                .get("/v2/pet/{petId}")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Pet.class);

        Assert.assertEquals(pet.getStatus(), NEW_PET_STATUS);
        Assert.assertEquals(pet.getName(), PET_NAME);

    }

    @Test
    public void deletePetTest(){

        given().log().all().spec(jsonRequest)
                .header("api_key", "api_key") //NO  API KEY  provided
                .pathParams("petId", "1234")
                .when()
                .delete("/v2/pet/{petId}")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().asString();
    }

    @Test
    public void findNonExistingPetIdTest(){

        PetResponse petResponse = given().log().all()
                .spec(jsonRequest)
                .pathParams("petId", "1999")
                .when()
                .get("/v2/pet/{petId}")
                .then()
                .log().all()
                .statusCode(404)
                .extract().response().as(PetResponse.class);

        Assert.assertTrue(petResponse.getMessage().equals("Pet not found"));
    }

    @Test
    public void updateNonExistingPetIdTest() {

        String PET_ID = "575890";
        String NEW_PET_NAME = "Alexandra";
        String NEW_PET_STATUS = "sold";

        PetResponse response = given().log().all().spec(formDataRequest)
                .pathParams("petId", PET_ID)
                .param("name", NEW_PET_NAME)
                .param("status", NEW_PET_STATUS)
                .when()
                .post("v2/pet/{petId}")
                .then()
                .log().all()
                .statusCode(404)
                .extract().response().as(PetResponse.class);

        Assert.assertEquals(response.getCode(), 404);
    }
}
