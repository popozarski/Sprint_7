import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;

import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderListTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Тест проверяет получение списка всех заказов из системы")
    public void testGetOrderList() {
        Response response = OrderApi.getOrderList();

        response.then()
                .statusCode(200)
                .body("orders", notNullValue());
    }
}