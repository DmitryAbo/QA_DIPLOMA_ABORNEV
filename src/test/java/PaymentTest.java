import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.page.MainPage;
import ru.netology.dataBase.DBUtils;
import ru.netology.page.PaymentPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    String approvedStatus = "APPROVED";
    String declinedStatus = "DECLINE";
    String formatErrorNotification = "Неверный формат";
    String emptyErrorNotification = "Поле обязательно для заполнения";
    String cardExpireNotification = "Истёк срок действия карты";
    String cardInvalidExpirationDate = "Неверно указан срок действия карты";
    String operationApprovedStatus = "Операция одобрена";
    String operationDeclinedStatus = "Банк отказал";

    MainPage mainPage = new MainPage();
    PaymentPage paymentPage = new PaymentPage();

    @BeforeAll
    public static void setUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    public static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }


    @BeforeEach
    public void startBrowser() {
        open("http://localhost:8080/");
        paymentPage = mainPage.buttonBuyClick();
    }

    @AfterEach
    public void rollBack() {
        DBUtils.cleanDB();
    }

    //Покупка с помощью дебетовой карты
    //Позитивные
    @Test
    void shouldApprovedPay() {                                                                  //Не успешно стоимость на сайте не совпадает со стоимостью в БД
        var amount = mainPage.checkAmount();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusVisible(operationApprovedStatus);
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(amount, DBUtils.getPayAmount());
        assertEquals(approvedStatus, DBUtils.checkPayEntityStatus());
    }

    @Test
    void shouldApprovedPayBoundaryMonthPlus() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(1));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusVisible(operationApprovedStatus);
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(approvedStatus, DBUtils.checkPayEntityStatus());
    }

    @Test
    void shouldApprovedPayBoundaryYearPlus() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(1));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusVisible(operationApprovedStatus);
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(approvedStatus, DBUtils.checkPayEntityStatus());
    }

    //Негативные
    @Test
    void shouldDeclinedPay() {
        paymentPage.setValueCardNumber(DataHelper.getDeclinedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(1));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusVisible(operationDeclinedStatus);
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(declinedStatus, DBUtils.checkPayEntityStatus());
    }

    @Test
    void shouldDeclinedPayNonexistentCard() {
        paymentPage.setValueCardNumber("0000000000000000");
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(1));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusVisible(operationDeclinedStatus);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayBoundaryMonthMinus() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(-1));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailMonth(cardExpireNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayBoundaryYearMinus() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(-1));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailYear(cardExpireNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayInvalidExpirationDateYear() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(20));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailYear(cardInvalidExpirationDate);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyCardNumber() {
        paymentPage.setValueCardNumber("");
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailCardNumber(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayInvalidPatternNumberSymbolsCard() {
        paymentPage.setValueCardNumber("444444444444");
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailCardNumber(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayInvalidFormatCard() {
        paymentPage.setValueCardNumber("Abo!9))");
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailCardNumber(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyMonth() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("");
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailMonth(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayZeroMonth() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("0");
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailMonth(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectMonth() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("55");
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailMonth(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsMonth() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("Abo!9((");
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailMonth(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyYear() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear("");
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailYear(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayZeroYear() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear("0");
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailYear(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsYear() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear("Abo!9((");
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailYear(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyOwner() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner("");
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailOwner(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsOwner() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner("ФФ!;:990");
        paymentPage.setValueCvc(DataHelper.getValidCvcCode());
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailOwner(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyCVC() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc("");
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailCvc(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayZeroCVC() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc("0");
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailCvc(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsCVC() {
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0));
        paymentPage.setValueYear(DataHelper.getYear(0));
        paymentPage.setValueOwner(DataHelper.getValidOwner("en"));
        paymentPage.setValueCvc("!AboФТ??11");
        paymentPage.buttonSendClick();
        paymentPage.checkOperationStatusInvisible(operationApprovedStatus);
        paymentPage.checkOperationStatusInvisible(operationDeclinedStatus);
        paymentPage.checkFailCvc(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }
}