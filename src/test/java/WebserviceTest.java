import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.page.MainPage;
import ru.netology.dataBase.DBUtils;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebserviceTest {

    String approvedStatus = "APPROVED";
    String declinedStatus = "DECLINE";
    String formatErrorNotification = "Неверный формат";
    String emptyErrorNotification = "Поле обязательно для заполнения";
    String cardExpireNotification = "Истёк срок действия карты";
    String cardInvalidExpirationDate = "Неверно указан срок действия карты";
    String operationApprovedStatus = "Операция одобрена";
    String operationDeclinedStatus = "Банк отказал";

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
    }

    @AfterEach
    public void rollBack() {
        DBUtils.cleanDB();
    }

    //Покупка с помощью дебетовой карты
    //Позитивные
    @Test
    void shouldApprovedPay() {                                                                  //Не успешно стоимость на сайте не совпадает со стоимостью в БД
        DBUtils.cleanDB();
        var mainPage = new MainPage();
        var amount = mainPage.checkAmount();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
        var mainPage = new MainPage();
        var paymentPage = mainPage.buttonBuyClick();
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
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    //Покупка в кредит
    //Позитивные
    @Test
    void shouldApprovedCredit() {                                                                  //Не успешно стоимость на сайте не совпадает со стоимостью в БД
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusVisible(operationApprovedStatus);
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(approvedStatus, DBUtils.checkCreditEntityStatus());
    }

    @Test
    void shouldApprovedCreditBoundaryMonthPlus() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(1));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusVisible(operationApprovedStatus);
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(approvedStatus, DBUtils.checkCreditEntityStatus());
    }

    @Test
    void shouldApprovedCreditBoundaryYearPlus() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(1));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusVisible(operationApprovedStatus);
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(approvedStatus, DBUtils.checkCreditEntityStatus());
    }

    //Негативные
    @Test
    void shouldDeclinedCredit() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getDeclinedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(1));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusVisible(operationDeclinedStatus);
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(declinedStatus, DBUtils.checkCreditEntityStatus());
    }

    @Test
    void shouldDeclinedCreditNonexistentCard() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber("0000000000000000");
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(1));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusVisible(operationDeclinedStatus);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditBoundaryMonthMinus() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(-1));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailMonth(cardExpireNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditBoundaryYearMinus() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(-1));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailYear(cardExpireNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditInvalidExpirationDateYear() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(20));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailYear(cardInvalidExpirationDate);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyCardNumber() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber("");
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailCardNumber(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditInvalidPatternNumberSymbolsCard() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber("444444444444");
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailCardNumber(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditInvalidFormatCard() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber("Abo!9))");
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailCardNumber(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyMonth() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth("");
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailMonth(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditZeroMonth() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth("0");
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailMonth(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectMonth() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth("55");
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailMonth(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsMonth() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth("Abo!9((");
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailMonth(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyYear() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear("");
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailYear(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditZeroYear() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear("0");
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailYear(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsYear() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear("Abo!9((");
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailYear(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyOwner() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner("");
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailOwner(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsOwner() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner("ФФ!;:990");
        creditPage.setValueCvc(DataHelper.getValidCvcCode());
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailOwner(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyCVC() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc("");
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailCvc(emptyErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditZeroCVC() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc("0");
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailCvc(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsCVC() {
        var mainPage = new MainPage();
        var creditPage = mainPage.buttonCreditClick();
        creditPage.setValueCardNumber(DataHelper.getApprovedPan());
        creditPage.setValueMonth(DataHelper.getMonth(0));
        creditPage.setValueYear(DataHelper.getYear(0));
        creditPage.setValueOwner(DataHelper.getValidOwner("en"));
        creditPage.setValueCvc("!AboФТ??11");
        creditPage.buttonSendClick();
        creditPage.checkOperationStatusInvisible(operationApprovedStatus);
        creditPage.checkOperationStatusInvisible(operationDeclinedStatus);
        creditPage.checkFailCvc(formatErrorNotification);
        assertTrue(DBUtils.checkEntityCount() == 0);
    }
}