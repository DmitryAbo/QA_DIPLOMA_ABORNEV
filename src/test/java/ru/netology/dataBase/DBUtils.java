package ru.netology.dataBase;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.codeborne.selenide.Selenide.sleep;

@UtilityClass
public class DBUtils {
    @SneakyThrows
    public static void cleanDB() {
        QueryRunner runner = new QueryRunner();
        String credit = "DELETE FROM credit_request_entity";
        String order = "DELETE FROM order_entity";
        String payment = "DELETE FROM payment_entity";
        try (
                Connection conn = DriverManager.getConnection(System.getProperty("db.url"),
                        "app",
                        "pass")
        ) {
            runner.execute(conn, credit);
            runner.execute(conn, order);
            runner.execute(conn, payment);
        }
    }

    @SneakyThrows
    public int checkEntityCount() {
        String creditEntityRq = "SELECT * FROM credit_request_entity;";
        String orderEntityRq = "SELECT * FROM order_entity;";
        String payEntityRq = "SELECT * FROM payment_entity;";
        int countEntity = 0;
        try (
                Connection conn = DriverManager.getConnection(System.getProperty("db.url"),
                        "app",
                        "pass");
                Statement statement = conn.createStatement();
        ) {
            try (ResultSet resultSet = statement.executeQuery(creditEntityRq)) {
                if (resultSet.next()) {
                    countEntity = countEntity + 1;
                }
            }
            try (ResultSet resultSet = statement.executeQuery(orderEntityRq)) {
                if (resultSet.next()) {
                    countEntity = countEntity + 2;
                }
            }
            try (ResultSet resultSet = statement.executeQuery(payEntityRq)) {
                if (resultSet.next()) {
                    countEntity = countEntity + 3;
                }
            }
        }
        return countEntity; //3-успешно для кредита/5-успешно для платежа
    }

    @SneakyThrows
    public String checkCreditEntityStatus() {
        String creditEntityStatusRq = "SELECT * FROM credit_request_entity;";
        try (
                Connection conn = DriverManager.getConnection(System.getProperty("db.url"),
                        "app",
                        "pass");
                Statement statement = conn.createStatement();
        ) {
            try (ResultSet resultSet = statement.executeQuery(creditEntityStatusRq)) {
                if (resultSet.next()) {
                    return resultSet.getString("status");
                }
            }
        }
        return "Unsuccessful request";
    }


    @SneakyThrows
    public String checkPayEntityStatus() {
        String payStatusRq = "SELECT * FROM payment_entity;";
        try (
                Connection conn = DriverManager.getConnection(System.getProperty("db.url"),
                        "app",
                        "pass");
                Statement statement = conn.createStatement();
        ) {
            try (ResultSet resultSet = statement.executeQuery(payStatusRq)) {
                if (resultSet.next()) {
                    return resultSet.getString("status");
                }
            }
        }
        return "Unsuccessful request";
    }

    @SneakyThrows
    public int getPayAmount() {
        String payStatusRq = "SELECT * FROM payment_entity;";
        try (
                Connection conn = DriverManager.getConnection(System.getProperty("db.url"),
                        "app",
                        "pass");
                Statement statement = conn.createStatement();
        ) {
            try (ResultSet resultSet = statement.executeQuery(payStatusRq)) {
                if (resultSet.next()) {
                    return resultSet.getInt("amount");
                }
            }
        }
        return 0;
    }
}
