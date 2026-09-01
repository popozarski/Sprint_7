import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class CourierCreationTest {
    private Courier testCourier;
    private String courierId;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Тест проверяет успешное создание нового курьера с валидными данными")
    public void testCreateCourierSuccess() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testCourier = new Courier("courier_" + timestamp, "password123", "Test Courier");

        CourierApi.createCourier(testCourier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Создание двух одинаковых курьеров")
    @Description("Тест проверяет обработку попытки создания двух курьеров с одинаковыми данными")
    public void testCreateDuplicateCouriers() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testCourier = new Courier("duplicate_" + timestamp, "password123", "Duplicate Courier");

        CourierApi.createCourier(testCourier)
                .then()
                .statusCode(201);

        CourierApi.createCourier(testCourier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Создание курьера без логина")
    @Description("Тест проверяет обработку попытки создания курьера без указания логина")
    public void testCreateCourierWithoutLogin() {
        Courier courierWithoutLogin = new Courier();
        courierWithoutLogin.setPassword("password123");
        courierWithoutLogin.setFirstName("No Login Courier");

        CourierApi.createCourier(courierWithoutLogin)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    @Description("Тест проверяет обработку попытки создания курьера без указания пароля")
    public void testCreateCourierWithoutPassword() {
        Courier courierWithoutPassword = new Courier();
        courierWithoutPassword.setLogin("no_password_" + System.currentTimeMillis());
        courierWithoutPassword.setFirstName("No Password Courier");

        CourierApi.createCourier(courierWithoutPassword)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без имени")
    @Description("Тест проверяет создание курьера без указания имени (должно быть успешным)")
    public void testCreateCourierWithoutFirstName() {
        Courier courierWithoutFirstName = new Courier();
        courierWithoutFirstName.setLogin("no_name_" + System.currentTimeMillis());
        courierWithoutFirstName.setPassword("password123");

        CourierApi.createCourier(courierWithoutFirstName)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Создание курьера с существующим логином")
    @Description("Тест проверяет обработку попытки создания курьера с уже существующим логином")
    public void testCreateCourierWithExistingLogin() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testCourier = new Courier("existing_" + timestamp, "password123", "Existing Courier");

        CourierApi.createCourier(testCourier)
                .then()
                .statusCode(201);

        Courier duplicateCourier = new Courier(
                testCourier.getLogin(),
                "different_password",
                "Different Name"
        );

        CourierApi.createCourier(duplicateCourier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @After
    public void tearDown() {
        if (testCourier != null && testCourier.getLogin() != null) {
            // Создаем объект для логина
            Courier loginData = new Courier();
            loginData.setLogin(testCourier.getLogin());
            loginData.setPassword(testCourier.getPassword());

            Response loginResponse = CourierApi.loginCourier(loginData);

            if (loginResponse.statusCode() == 200) {
                courierId = loginResponse.path("id").toString();
                CourierApi.deleteCourier(courierId).then().statusCode(200);
            }
        }
    }
}