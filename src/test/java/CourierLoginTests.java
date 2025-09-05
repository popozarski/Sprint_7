import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierLoginTests {

    Gson gson = new Gson();
    Courier courier;
    private String originalLogin;
    private String originalPassword = "password123";

    public class Courier {
        private String login;
        public String password;

        public Courier(String login, String password){
            this.login = login;
            this.password = password;
        }
        public Courier(){}

        public void setLogin(String login) {
            this.login = login;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Before
    @Step("Настройка тестового окружения и создание курьера")
    public void setUp(){
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";

        String timestamp = String.valueOf(System.currentTimeMillis());
        originalLogin = String.format("courier_%s", timestamp);
        courier = new Courier(originalLogin, originalPassword);

        // создаем курьера
        createCourier(courier)
                .then()
                .statusCode(201);
    }

    @Test
    @Step("Тест успешной авторизации курьера")
    public void testCourierLoginSuccess(){
        loginCourier(courier)
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    // Авторизация без логина
    @Test
    @Step("Тест авторизации без логина")
    public void testCourierLoginWithoutLogin() {
        Map<String, String> loginData = createLoginDataWithoutLogin(originalPassword);

        loginWithMapData(loginData)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    // Авторизация без пароля
    @Test
    @Step("Тест авторизации без пароля")
    public void testCourierLoginWithoutPassword() {
        Map<String, String> loginData = createLoginDataWithoutPassword(originalLogin);

        loginWithMapData(loginData)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    // Авторизация с неверным паролем
    @Test
    @Step("Тест авторизации с неверным паролем")
    public void testCourierLoginWithWrongPassword() {
        Courier courierWrongPassword = new Courier(originalLogin, "wrong_password");

        loginCourier(courierWrongPassword)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    // Авторизация с неверным логином
    @Test
    @Step("Тест авторизации с неверным логином")
    public void testCourierLoginWithWrongLogin() {
        Courier courierWithWrongLogin = new Courier("nonexistent_login", originalPassword);

        loginCourier(courierWithWrongLogin)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    // Авторизация несуществующего курьера
    @Test
    @Step("Тест авторизации несуществующего курьера")
    public void testNonExistentCourierLogin() {
        Map<String, String> loginData = createLoginData("nonexistent_courier", "password123");

        loginWithMapData(loginData)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    // Проверка, что успешный запрос возвращает id
    @Test
    @Step("Тест проверки возврата ID при успешной авторизации")
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
        String json = gson.toJson(courier);
        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Авторизация с данными в формате Map")
    private Response loginWithMapData(Map<String, String> loginData) {
        return given()
                .header("Content-type", "application/json")
                .body(loginData)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Создание данных для авторизации")
    private Map<String, String> createLoginData(String login, String password) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("login", login);
        loginData.put("password", password);
        return loginData;
    }

    @Step("Создание данных для авторизации без логина")
    private Map<String, String> createLoginDataWithoutLogin(String password) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("password", password);
        return loginData;
    }

    @Step("Создание данных для авторизации без пароля")
    private Map<String, String> createLoginDataWithoutPassword(String login) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("login", login);
        return loginData;
    }

    @Step("Удаление курьера")
    private Response deleteCourier(String id) {
        return given()
                .delete("/api/v1/courier/" + id);
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown(){

              Response response =  given()
                        .header("Content-type", "application/json")
                        .body(courier)
                        .when()
                        .post("/api/v1/courier/login");

              String id = response.path("id").toString();

              given()
                      .delete("/api/v1/courier/"  + id)
                      .then()
                      .statusCode(200);
    }
}

