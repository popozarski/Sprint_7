

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderCreationTest {
    private final String[] colors;
    private int trackId;
    private final Map<String, Object> orderData;

    public OrderCreationTest(String[] colors) {
        this.colors = colors;
        this.orderData = createOrderData();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new String[]{"BLACK"}},
                {new String[]{"GREY"}},
                {new String[]{"BLACK", "GREY"}},
                {null} // случай, когда цвет не указан
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    public void testCreateOrderWithDifferentColors() {
        createOrderWithColors(colors);
        verifyOrderCreatedSuccessfully();
    }

    @Step("Создание заказа с цветами: {colors}")
    private void createOrderWithColors(String[] colors) {
        // Добавляем цвета в данные заказа, если они указаны
        if (colors != null) {
            orderData.put("color", colors);
        }

        Response response = given()
                .header("Content-type", "application/json")
                .body(orderData)
                .when()
                .post("/api/v1/orders");

        response.then().statusCode(201);
        trackId = response.path("track");
    }

    @Step("Проверка успешного создания заказа")
    private void verifyOrderCreatedSuccessfully() {
        given()
                .when()
                .get("/api/v1/orders/track?t=" + trackId)
                .then()
                .statusCode(200)
                .body("order", notNullValue());
    }

    @Step("Подготовка данных заказа")
    private Map<String, Object> createOrderData() {
        Map<String, Object> order = new HashMap<>();
        order.put("firstName", "Иван");
        order.put("lastName", "Иванов");
        order.put("address", "ул. Ленина, 123");
        order.put("metroStation", 4);
        order.put("phone", "+79991234567");
        order.put("rentTime", 3);
        order.put("deliveryDate", "2024-06-30");
        order.put("comment", "Тестовый заказ");
        return order;
    }

    @After
    @Step("Попытка отмены тестового заказа")
    public void tearDown() {
        if (trackId != 0) {
            try {
                // Просто пытаемся отменить заказ без проверки результата
                given()
                        .header("Content-type", "application/json")
                        .body("{\"track\": " + trackId + "}")
                        .when()
                        .put("/api/v1/orders/cancel");
            } catch (Exception e) {
                System.out.println("Не удалось отменить заказ с trackId: " + trackId);
            }
        }
    }
}