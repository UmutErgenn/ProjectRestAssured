package Campus;

import Campus.Model.PositionCategories;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PositionCategoryTest {

    String positionCategoryName;
    Cookies cookies;
    String positionCategoryID;


    @BeforeClass
    public void loginCampus() {
        baseURI = "https://test.mersys.io/";

        Map<String, String> credential = new HashMap<>();
        credential.put("username", "turkeyts");
        credential.put("password", "TechnoStudy123");
        credential.put("rememberMe", "true");

        cookies =
                given()
                        .contentType(ContentType.JSON)
                        .body(credential)

                        .when()
                        .post("auth/login")

                        .then()
                        //.log().body()
                        .statusCode(200)
                        .extract().response().getDetailedCookies()
        ;

        System.out.println("cookies = " + cookies);
    }

    public String getRandomName() {
        return RandomStringUtils.randomAlphabetic(8);
    }

    @Test
    public void createPositionCategory() {
        positionCategoryName = getRandomName();

        PositionCategories positionCategories = new PositionCategories();
        positionCategories.setName(positionCategoryName);

        positionCategoryID =

                given()
                        .cookies(cookies)
                        .contentType(ContentType.JSON)
                        .body(positionCategories)
                        .when()
                        .post("school-service/api/position-category")
                        .then()
                        .log().body()
                        .statusCode(201)
                        .extract().jsonPath().getString("id")
        ;
    }

    @Test(dependsOnMethods = "createPositionCategory", priority = 1)
    public void createPositionCategoryNegative() {
        PositionCategories positionCategories = new PositionCategories();
        positionCategories.setName(positionCategoryName);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(positionCategories)
                .when()
                .post("school-service/api/position-category")
                .then()
                .log().body()
                .statusCode(400)
                .body("message", equalTo("The Position Category with Name \"" + positionCategoryName + "\" already exists."))
                .body("message", containsString("already exists"))
        ;
    }


    @Test(dependsOnMethods = "createPositionCategory", priority = 2)
    public void updatePositionCategory() {
        positionCategoryName = getRandomName();

        PositionCategories positionCategories = new PositionCategories();
        positionCategories.setId(positionCategoryID);
        positionCategories.setName(positionCategoryName);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(positionCategories)

                .when()
                .put("school-service/api/position-category")
                .then()
                .log().body()
                .statusCode(200)
                .body("name", equalTo(positionCategoryName))
        ;
    }

    @Test(dependsOnMethods = "updatePositionCategory")
    public void deletePositionCategoryById() {
        given()
                .cookies(cookies)
                .pathParam("positionCategoryID", positionCategoryID)
                .log().uri()

                .when()
                .delete("/school-service/api/position-category/{positionCategoryID}")

                .then()
                .log().body()
                .statusCode(204)
        ;
    }

    @Test(dependsOnMethods = "deletePositionCategoryById")
    public void deletePositionCategoryByIdNegative() {
        given()
                .cookies(cookies)
                .pathParam("positionCategoryID", positionCategoryID)
                .log().uri()

                .when()
                .delete("/school-service/api/position-category/{positionCategoryID}")

                .then()
                .log().body()
                .statusCode(400)
                .body("message", equalTo("PositionCategory not  found"))
        ;
    }
}

