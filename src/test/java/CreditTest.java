import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.page.CreditPage;
import ru.netology.page.MainPage;
import ru.netology.dataBase.DBUtils;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreditTest {

    String approvedStatus = "APPROVED";
    String declinedStatus = "DECLINE";
    String formatErrorNotification = "Неверный формат";
    String emptyErrorNotification = "Поле обязательно для заполнения";
    String cardExpireNotification = "Истёк срок действия карты";
    String cardInvalidExpirationDate = "Неверно указан срок действия карты";
    String operationApprovedStatus = "Операция одобрена";
    String operationDeclinedStatus = "Банк отказал";
    MainPage mainPage = new MainPage();
    CreditPage creditPage = new CreditPage();

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
        creditPage = mainPage.buttonCreditClick();
    }

    @AfterEach
    public void rollBack() {
        DBUtils.cleanDB();
    }

    @Test
    void shouldApprovedCredit() {                                                                  //Не успешно стоимость на сайте не совпадает со стоимостью в БД
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
