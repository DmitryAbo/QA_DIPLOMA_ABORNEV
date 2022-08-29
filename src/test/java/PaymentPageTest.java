import com.codeborne.selenide.Condition;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.page.PaymentPage;
import ru.netology.dataBase.DBUtils;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentPageTest {

    String approvedStatus = "APPROVED";
    String declinedStatus = "DECLINE";
    String formatErrorNotification = "Неверный формат";
    String emptyErrorNotification = "Поле обязательно для заполнения";
    String cardExpireNotification = "Истёк срок действия карты";
    String cardInvalidExpirationDate = "Неверно указан срок действия карты";

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
        var paymentPage = new PaymentPage();
        var amount = paymentPage.checkAmount();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(amount, DBUtils.getPayAmount());
        assertEquals(approvedStatus, DBUtils.checkPayEntityStatus());
    }

    @Test
    void shouldApprovedPayBoundaryMonthPlus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(1, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(approvedStatus, DBUtils.checkPayEntityStatus());
    }

    @Test
    void shouldApprovedPayBoundaryYearPlus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(1, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(approvedStatus, DBUtils.checkPayEntityStatus());
    }

    //Негативные
    @Test
    void shouldDeclinedPay() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getDeclinedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(1, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible, Duration.ofSeconds(60));
        paymentPage.checkUnSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 5);
        assertEquals(declinedStatus, DBUtils.checkPayEntityStatus());
    }

    @Test
    void shouldDeclinedPayNonexistentCard() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber("0000000000000000");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(1, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible, Duration.ofSeconds(60));
        paymentPage.checkUnSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayBoundaryMountMinus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(1, false));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(cardExpireNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayBoundaryYearMinus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(1, false));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(cardExpireNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayInvalidExpirationDateYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(20, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(cardInvalidExpirationDate, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyCardNumber() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber("");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailCardNumber().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayInvalidPatternNumberSymbolsCard() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber("444444444444");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCardNumber().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayInvalidFormatCard() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber("Abo!9))");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCardNumber().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayZeroMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("0");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("55");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("Abo!9((");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear("");
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayZeroYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear("0");
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear("Abo!9((");
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyOwner() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner("");
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailOwner().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsOwner() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner("ФФ!;:990");
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailOwner().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayEmptyCVC() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc("");
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailCvc().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayZeroCVC() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc("0");
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCvc().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailPayIncorrectSymbolsCVC() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonBuyClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc("!AboФТ??11");
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCvc().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    //Покупка в кредит
    //Позитивные
    @Test
    void shouldApprovedCredit() {                                                                  //Не успешно стоимость на сайте не совпадает со стоимостью в БД
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(approvedStatus, DBUtils.checkCreditEntityStatus());
    }

    @Test
    void shouldApprovedCreditBoundaryMountPlus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(1, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(approvedStatus, DBUtils.checkCreditEntityStatus());
    }

    @Test
    void shouldApprovedCreditBoundaryYearPlus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(1, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(approvedStatus, DBUtils.checkCreditEntityStatus());
    }

    //Негативные
    @Test
    void shouldDeclinedCredit() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getDeclinedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(1, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible, Duration.ofSeconds(60));
        paymentPage.checkUnSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 3);
        assertEquals(declinedStatus, DBUtils.checkCreditEntityStatus());
    }

    @Test
    void shouldDeclinedCreditNonexistentCard() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber("0000000000000000");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible, Duration.ofSeconds(60));
        paymentPage.checkUnSuccessPay().shouldBe(Condition.visible, Duration.ofSeconds(60));
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditBoundaryMonthMinus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(1, false));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(cardExpireNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditBoundaryYearMinus() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(1, false));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(cardExpireNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditInvalidExpirationDateYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(20, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(cardInvalidExpirationDate, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyCardNumber() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber("");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailCardNumber().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditInvalidPatternNumberSymbolsCard() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber("444444444444");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCardNumber().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditInvalidFormatCard() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber("Abo!9))");
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCardNumber().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditZeroMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("0");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("55");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsMonth() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth("Abo!9((");
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailMonth().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear("");
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditZeroYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear("0");
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsYear() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear("Abo!9((");
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailYear().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyOwner() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner("");
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailOwner().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsOwner() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner("ФФ!;:990");
        paymentPage.setValueCvc(DataHelper.getCvcCode(true));
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailOwner().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditEmptyCVC() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc("");
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(emptyErrorNotification, paymentPage.checkFailCvc().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditZeroCVC() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc("0");
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCvc().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }

    @Test
    void shouldFailCreditIncorrectSymbolsCVC() {
        var paymentPage = new PaymentPage();
        paymentPage.buttonCreditClick();
        paymentPage.setValueCardNumber(DataHelper.getApprovedPan());
        paymentPage.setValueMonth(DataHelper.getMonth(0, true));
        paymentPage.setValueYear(DataHelper.getYear(0, true));
        paymentPage.setValueOwner(DataHelper.getOwner(true, "en"));
        paymentPage.setValueCvc("!AboФТ??11");
        paymentPage.buttonSendClick();
        paymentPage.checkUnSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        paymentPage.checkSuccessPay().shouldNotBe(Condition.visible,Duration.ofSeconds(15));
        assertEquals(formatErrorNotification, paymentPage.checkFailCvc().getOwnText());
        assertTrue(DBUtils.checkEntityCount() == 0);
    }
}