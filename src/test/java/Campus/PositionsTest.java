package Campus;

import Campus.Model.PositionCategories;
import Campus.Model.Positions;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class PositionsTest {

    String positionName;
    String positionID;
    String positionShortName;
    Cookies cookies;
    String tenantID;
    int g;
    String a;

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
//                        .log().body()
                        .statusCode(200)
                        .extract().response().getDetailedCookies()
        ;

        System.out.println("cookies = " + cookies);
    }

    public String getRandomName() {
        return RandomStringUtils.randomAlphabetic(8);
    }
    public String getRandomShortName() {return RandomStringUtils.randomAlphabetic(4);}
    public String getRandomTenantID(){return RandomStringUtils.randomNumeric(24);}

    @Test
    public void createPosition() {
        positionName = getRandomName();
        positionShortName = getRandomShortName();
        tenantID=getRandomTenantID();

        Positions positions = new Positions();
        positions.setName(positionName);
        positions.setShortName(positionShortName);
        positions.setTenantId(tenantID);

        positionID =
                given()
                        .cookies(cookies)
                        .contentType(ContentType.JSON)
                        .body(positions)
                        .when()
                        .post("/school-service/api/employee-position")
                        .then()
                        .log().body()
                        .statusCode(201)
                        .extract().jsonPath().getString("id")
        ;
    }

    @Test(dependsOnMethods = "createPosition", priority = 1)
    public void createPositionNegative() {
        Positions positions = new Positions();
        positions.setName(positionName);
        positions.setShortName(positionShortName);
        positions.setTenantId(tenantID);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(positions)
                .when()
                .post("/school-service/api/employee-position")
                .then()
                .log().body()
                .statusCode(400)
                .body("message", equalTo("The Position with Name \"" + positionName + "\" already exists."))
                .body("message", containsString("already exists"))
        ;
    }

    @Test(dependsOnMethods = "createPosition", priority = 2)
    public void updatePosition() {
        positionName = getRandomName();
        positionShortName = getRandomShortName();



        Positions positions = new Positions();
        positions.setName(positionName);
        positions.setShortName(positionShortName);
        positions.setTenantId(tenantID);
        positions.setId(positionID);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(positions)

                .when()
                .put("/school-service/api/employee-position")
                .then()
                .log().body()
                .statusCode(200)
                .body("name", equalTo(positionName))
        ;
    }

    @Test(dependsOnMethods = "updatePosition")
    public void deletePositionById() {
        given()
                .cookies(cookies)
                .pathParam("positionID", positionID)
                .log().uri()

                .when()
                .delete("/school-service/api/employee-position/{positionID}")

                .then()
                .log().body()
                .statusCode(204)
        ;
    }

    @Test(dependsOnMethods = "deletePositionById")
    public void deletePositionByIdNegative() {
        given()
                .cookies(cookies)
                .pathParam("positionID", positionID)
                .log().uri()

                .when()
                .delete("/school-service/api/employee-position/{positionID}")

                .then()
                .log().body()
                .statusCode(400)
                .body("message", equalTo("Positions  not found"))
        ;
    }
}
