import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    @Parameterized.Parameters(name = "Цвета самоката: {0}")
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
    @DisplayName("Создание заказа с разными цветами самоката")
    @Description("Тест проверяет создание заказа с различными вариантами выбора цвета самоката")
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

        Response response = OrderApi.createOrder(orderData);
        response.then().statusCode(201);
        trackId = response.path("track");
    }

    @Step("Проверка успешного создания заказа")
    private void verifyOrderCreatedSuccessfully() {
        OrderApi.getOrderByTrack(trackId)
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
                // Используем OrderApi для отмены заказа
                OrderApi.cancelOrder(trackId);
            } catch (Exception e) {
                System.out.println("Не удалось отменить заказ с trackId: " + trackId);
            }
        }
    }
}