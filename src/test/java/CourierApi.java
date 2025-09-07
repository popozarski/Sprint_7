import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CourierApi {

    public static Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");
    }

    public static Response loginCourier(Courier loginData) {
        return given()
                .header("Content-type", "application/json")
                .body(loginData)
                .when()
                .post("/api/v1/courier/login");
    }

    public static Response deleteCourier(String courierId) {
        return given()
                .when()
                .delete("/api/v1/courier/" + courierId);
    }
}