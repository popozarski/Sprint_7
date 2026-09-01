import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrderApi {

    @Step("Создание заказа")
    public static Response createOrder(Map<String, Object> orderData) {
        return given()
                .header("Content-type", "application/json")
                .body(orderData)
                .when()
                .post("/api/v1/orders");
    }

    @Step("Получение заказа по трек-номеру")
    public static Response getOrderByTrack(int trackId) {
        return given()
                .when()
                .get("/api/v1/orders/track?t=" + trackId);
    }

    @Step("Отмена заказа")
    public static Response cancelOrder(int trackId) {
        return given()
                .header("Content-type", "application/json")
                .body("{\"track\": " + trackId + "}")
                .when()
                .put("/api/v1/orders/cancel");
    }

    @Step("Получение списка заказов")
    public static Response getOrderList() {
        return given()
                .when()
                .get("/api/v1/orders");
    }
}