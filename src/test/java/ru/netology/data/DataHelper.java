package ru.netology.data;

import com.github.javafaker.Faker;
import lombok.Value;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@UtilityClass
public class DataHelper {

    public static String getApprovedPan() {
        return "4444 4444 4444 4441";
    }

    public static String getDeclinedPan() {
        return "4444 4444 4444 4442";
    }

    public static String getInvalidPan() {
        return String.valueOf((long) (Math.random() * 1000000000000000.0));
    }

    public static String getValidOwner(String locale) {
        Faker faker = new Faker(new Locale(locale));
        return faker.name().lastName();
    }

    public static String getInvalidOwner(String locale) {
        Faker faker = new Faker(new Locale(locale));
        return faker.name().lastName() + "-?";
    }

    public static String getValidCvcCode() {
        return String.valueOf((long) ((Math.random() + 0.1) * 1000));
    }

    public static String getInvalidCvcCode() {
        return String.valueOf((long) ((Math.random() + 0.1) * 100));
    }

    public static String getYear(int offset) {
        return LocalDate.now().plusYears(offset).format(DateTimeFormatter.ofPattern("yy"));
    }

    public static String getMonth(int offset) {
        return LocalDate.now().plusMonths(offset).format(DateTimeFormatter.ofPattern("MM"));
    }

    public static String getInvalidDate() {
        return String.valueOf((long) (Math.random() * 10));
    }


}