package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$x;

@Getter
@NoArgsConstructor
public class MainPage {
    private final String amountStart = "Всего ";
    private final String amountFinish = " руб.!";

    public PaymentPage buttonBuyClick() {
        $x("//span[contains(text(),'Купить')]//ancestor::button").click();
        return new PaymentPage();
    }

    public CreditPage buttonCreditClick() {
        $x("//span[contains(text(),'Купить в кредит')]//ancestor::button").click();
        return new CreditPage();
    }

    public int checkAmount() {
        String text = $x("//*[contains(text(),'руб')]").getText();
        val start = text.indexOf(amountStart);
        val finish = text.indexOf(amountFinish);
        val value = text.substring(start + amountStart.length(), finish).replaceAll(" ", "");
        return Integer.parseInt(value);
    }
}
