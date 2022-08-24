package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$x;

@Getter
@NoArgsConstructor
public class PaymentPage {
    private final String amountStart = "Всего ";
    private final String amountFinish = " руб.!";

    public void buttonBuyClick() {
        $x("//span[contains(text(),'Купить')]//ancestor::button").click();
    }

    public void buttonCreditClick() {
        $x("//span[contains(text(),'Купить в кредит')]//ancestor::button").click();
    }

    public void buttonSendClick() {
        $x("//*[text()='Продолжить']//ancestor::button").click();
    }

    public void setValueCardNumber(String cardNumber) {
        $x("//span[contains(text(),'Номер карты')]//ancestor::span[contains(@class,'input')]//input").click();
        $x("//span[contains(text(),'Номер карты')]//ancestor::span[contains(@class,'input')]//input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $x("//span[contains(text(),'Номер карты')]//ancestor::span[contains(@class,'input')]//input").setValue(cardNumber);
    }

    public SelenideElement checkFailCardNumber() {
        return $x("//span[contains(text(),'Номер карты')]//ancestor::span[contains(@class,'input')]//child::span[@class='input__sub']");
    }

    public void setValueMonth(String month) {
        $x("//span[contains(text(),'Месяц')]//ancestor::span[contains(@class,'input-group__input-case')]//input").click();
        $x("//span[contains(text(),'Месяц')]//ancestor::span[contains(@class,'input-group__input-case')]//input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $x("//span[contains(text(),'Месяц')]//ancestor::span[contains(@class,'input-group__input-case')]//input").setValue(month);
    }

    public SelenideElement checkFailMonth() {
        return $x("//span[contains(text(),'Месяц')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");
    }

    public void setValueYear(String year) {
        $x("//span[contains(text(),'Год')]//ancestor::span[contains(@class,'input-group__input-case')]//input").click();
        $x("//span[contains(text(),'Год')]//ancestor::span[contains(@class,'input-group__input-case')]//input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $x("//span[contains(text(),'Год')]//ancestor::span[contains(@class,'input-group__input-case')]//input").setValue(year);
    }

    public SelenideElement checkFailYear() {
        return $x("//span[contains(text(),'Год')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");
    }

    public void setValueOwner(String holder) {
        $x("//span[contains(text(),'Владелец')]//ancestor::span[contains(@class,'input-group__input-case')]//input").click();
        $x("//span[contains(text(),'Владелец')]//ancestor::span[contains(@class,'input-group__input-case')]//input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $x("//span[contains(text(),'Владелец')]//ancestor::span[contains(@class,'input-group__input-case')]//input").setValue(holder);
    }

    public SelenideElement checkFailOwner() {
        return $x("//span[contains(text(),'Владелец')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");
    }

    public void setValueCvc(String cvc) {
        $x("//span[contains(text(),'CVC/CVV')]//ancestor::span[contains(@class,'input-group__input-case')]//input").click();
        $x("//span[contains(text(),'CVC/CVV')]//ancestor::span[contains(@class,'input-group__input-case')]//input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $x("//span[contains(text(),'CVC/CVV')]//ancestor::span[contains(@class,'input-group__input-case')]//input").setValue(cvc);
    }

    public SelenideElement checkFailCvc() {
        return $x("//span[contains(text(),'CVC/CVV')]//ancestor::span[contains(@class,'input-group__input-case')]//span[@class='input__sub']");
    }

    public int checkAmount() {
        String text = $x("//*[contains(text(),'руб')]").getText();
        val start = text.indexOf(amountStart);
        val finish = text.indexOf(amountFinish);
        val value = text.substring(start + amountStart.length(), finish).replaceAll(" ", "");
        return Integer.parseInt(value);
    }

    public SelenideElement checkSuccessPay() {
        return $x("//div[contains(text(),'Операция одобрена')]");
    }

    public SelenideElement checkUnSuccessPay() {
        return $x("//div[contains(text(),'Банк отказал')]");
    }


}
