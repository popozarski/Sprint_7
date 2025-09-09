import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CourierApi {

    @Step("Создание курьера")
    public static Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Авторизация курьера")
    public static Response loginCourier(Courier loginData) {
        return given()
                .header("Content-type", "application/json")
                .body(loginData)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Удаление курьера")
    public static Response deleteCourier(String courierId) {
        return given()
                .when()
                .delete("/api/v1/courier/" + courierId);
    }
}