package ru.netology.page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openqa.selenium.Keys;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Getter
@NoArgsConstructor
public class CreditPage {

    private SelenideElement buttonSend = $x("//*[text()='Продолжить']//ancestor::button");
    private SelenideElement cardNumberField = $x("//span[contains(text(),'Номер карты')]//ancestor::span[contains(@class,'input')]//input");
    private SelenideElement monthField = $x("//span[contains(text(),'Месяц')]//ancestor::span[contains(@class,'input-group__input-case')]//input");
    private SelenideElement yearField = $x("//span[contains(text(),'Год')]//ancestor::span[contains(@class,'input-group__input-case')]//input");
    private SelenideElement ownerField = $x("//span[contains(text(),'Владелец')]//ancestor::span[contains(@class,'input-group__input-case')]//input");
    private SelenideElement cvcField = $x("//span[contains(text(),'CVC/CVV')]//ancestor::span[contains(@class,'input-group__input-case')]//input");
    private SelenideElement cardNumberNotification = $x("//span[contains(text(),'Номер карты')]//ancestor::span[contains(@class,'input')]//child::span[@class='input__sub']");
    private SelenideElement monthFieldNotification = $x("//span[contains(text(),'Месяц')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");
    private SelenideElement yearFieldNotification = $x("//span[contains(text(),'Год')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");
    private SelenideElement ownerFieldNotification = $x("//span[contains(text(),'Владелец')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");
    private SelenideElement cvcFieldNotification = $x("//span[contains(text(),'CVC/CVV')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");

    public void buttonSendClick() {
        buttonSend.click();
    }

    public void setValueCardNumber(String cardNumber) {
        cardNumberField.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        cardNumberField.setValue(cardNumber);
    }

    public void checkFailCardNumber(String notification) {
        cardNumberNotification.shouldHave(Condition.text(notification)).shouldBe(Condition.visible);
    }

    public void setValueMonth(String month) {
        monthField.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        monthField.setValue(month);
    }

    public void checkFailMonth(String notification) {
        monthFieldNotification.shouldHave(Condition.text(notification)).shouldBe(Condition.visible);
    }

    public void setValueYear(String year) {
        yearField.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        yearField.setValue(year);
    }

    public void checkFailYear(String notification) {
        yearFieldNotification.shouldHave(Condition.text(notification)).shouldBe(Condition.visible);
    }

    public void setValueOwner(String holder) {
        ownerField.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        ownerField.setValue(holder);
    }

    public void checkFailOwner(String notification) {
        ownerFieldNotification.shouldHave(Condition.text(notification)).shouldBe(Condition.visible);
    }

    public void setValueCvc(String cvc) {
        cvcField.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        cvcField.setValue(cvc);
    }

    public void checkFailCvc(String notification) {
        cvcFieldNotification.shouldHave(Condition.text(notification)).shouldBe(Condition.visible);
    }

    public void checkOperationStatusVisible(String operationStatusExpect){
        SelenideElement operationStatus = $x("//div[contains(text(),\"" + operationStatusExpect + "\")]");
        operationStatus.shouldBe(Condition.visible, Duration.ofSeconds(60));
    }

    public void checkOperationStatusInvisible(String operationStatusExpect) {
        SelenideElement operationStatus = $x("//div[contains(text(),\"" + operationStatusExpect + "\")]");
        operationStatus.shouldBe(Condition.hidden, Duration.ofSeconds(15));
    }

}
