

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class OrderListTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    public void testGetOrderList() {
        getOrderList();
        verifyOrderListResponse();
    }

    @Step("Получение списка заказов")
    private Response getOrderList() {
        return given()
                .when()
                .get("/api/v1/orders");
    }

    @Step("Проверка ответа со списком заказов")
    private void verifyOrderListResponse() {
        Response response = getOrderList();

        response.then()
                .statusCode(200)
                .body("orders", notNullValue());

    }
}
