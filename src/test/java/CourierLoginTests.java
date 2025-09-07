import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierLoginTests {

    Courier courier;
    private String originalLogin;
    private String originalPassword = "password123";

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";

        String timestamp = String.valueOf(System.currentTimeMillis());
        originalLogin = String.format("courier_%s", timestamp);
        courier = new Courier(originalLogin, originalPassword, "Test Courier");

        // создаем курьера
        createCourier(courier)
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    @Description("Тест проверяет успешную авторизацию курьера с валидными учетными данными")
    public void testCourierLoginSuccess() {
        loginCourier(courier)
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("Авторизация без логина")
    @Description("Тест проверяет обработку попытки авторизации без указания логина")
    public void testCourierLoginWithoutLogin() {
        Courier courierWithoutLogin = new Courier();
        courierWithoutLogin.setPassword(originalPassword);

        loginCourier(courierWithoutLogin)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация без пароля")
    @Description("Тест проверяет обработку попытки авторизации без указания пароля")
    public void testCourierLoginWithoutPassword() {
        Courier courierWithoutPassword = new Courier();
        courierWithoutPassword.setLogin(originalLogin);

        loginCourier(courierWithoutPassword)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация с неверным паролем")
    @Description("Тест проверяет обработку попытки авторизации с неверным паролем")
    public void testCourierLoginWithWrongPassword() {
        Courier courierWrongPassword = new Courier(originalLogin, "wrong_password", "Test Courier");

        loginCourier(courierWrongPassword)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация с неверным логином")
    @Description("Тест проверяет обработку попытки авторизации с неверным логином")
    public void testCourierLoginWithWrongLogin() {
        Courier courierWithWrongLogin = new Courier("nonexistent_login", originalPassword, "Test Courier");

        loginCourier(courierWithWrongLogin)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    @Description("Тест проверяет обработку попытки авторизации несуществующего курьера")
    public void testNonExistentCourierLogin() {
        Courier nonExistentCourier = new Courier("nonexistent_courier", "password123", "Test Courier");

        loginCourier(nonExistentCourier)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Проверка возврата ID при успешной авторизации")
    @Description("Тест проверяет, что успешный запрос авторизации возвращает идентификатор курьера")
    public void testSuccessfulLoginReturnsId() {
        Response response = loginCourier(courier);

        response.then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Step("Создание курьера")
    private Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Авторизация курьера")
    private Response loginCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Удаление курьера")
    private Response deleteCourier(String id) {
        return given()
                .delete("/api/v1/courier/" + id);
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        Response response = loginCourier(courier);

        if (response.statusCode() == 200) {
            String id = response.path("id").toString();
            deleteCourier(id).then().statusCode(200);
        }
    }
}

