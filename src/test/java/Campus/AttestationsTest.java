package Campus;

import Campus.Model.Attestations;
import Campus.Model.PositionCategories;
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

public class AttestationsTest {

    String attestationsName;
    Cookies cookies;
    String attestationsID;

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
    public void createAttestation() {
        attestationsName = getRandomName();

        Attestations attestations = new Attestations();
        attestations.setName(attestationsName);

        attestationsID =

                given()
                        .cookies(cookies)
                        .contentType(ContentType.JSON)
                        .body(attestations)
                        .when()
                        .post("/school-service/api/attestation")
                        .then()
                        .log().body()
                        .statusCode(201)
                        .extract().jsonPath().getString("id")
        ;
    }

    @Test(dependsOnMethods = "createAttestation", priority = 1)
    public void createAttestationNegative() {
        Attestations attestations = new Attestations();
        attestations.setName(attestationsName);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(attestations)
                .when()
                .post("school-service/api/attestation")
                .then()
                .log().body()
                .statusCode(400)
                .body("message", equalTo("The Attestation with Name \"" + attestationsName + "\" already exists."))
                .body("message", containsString("already exists"))
        ;
    }

    @Test(dependsOnMethods = "createAttestation", priority = 2)
    public void updateAttestation() {
        attestationsName = getRandomName();

        Attestations attestations = new Attestations();
        attestations.setId(attestationsID);
        attestations.setName(attestationsName);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(attestations)

                .when()
                .put("school-service/api/attestation")
                .then()
                .log().body()
                .statusCode(200)
                .body("name", equalTo(attestationsName))
        ;
    }

    @Test(dependsOnMethods = "updateAttestation")
    public void deleteAttestationById() {
        given()
                .cookies(cookies)
                .pathParam("attestationsID", attestationsID)
                .log().uri()

                .when()
                .delete("/school-service/api/attestation/{attestationsID}")

                .then()
                .log().body()
                .statusCode(204)
        ;
    }

    @Test(dependsOnMethods = "deleteAttestationById")
    public void deleteAttestationByIdNegative() {
        given()
                .cookies(cookies)
                .pathParam("attestationsID", attestationsID)
                .log().uri()

                .when()
                .delete("/school-service/api/attestation/{attestationsID}")

                .then()
                .log().body()
                .statusCode(400)
                .body("message", equalTo("attestation not found"))
        ;
    }
}
