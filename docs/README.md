# Инструкция по запуску проекта авто-тестов
**Перечень необходимого ПО**
  1. ОС Windows 10 21H2 19044.1889 и выше
  1. IntelliJ IDEA 2022.1.2
  1. Java
      - openjdk version "11.0.13" 2021-10-19
      - OpenJDK Runtime Environment Temurin-11.0.13+8 (build 11.0.13+8)
      - OpenJDK 64-Bit Server VM Temurin-11.0.13+8 (build 11.0.13+8, mixed mode)
  1. Docker Desktop 4.9.0
  1. Google Chrome Версия 104.0.5112.102
  1. Git version 2.34.1.windows.1
  
**Порядок действий для запуска автотестов**
  1. Запустить Docker Desktop дождаться пока приложение подключится к серверу
  1. Запустить IntelliJ IDEA 2022.1.2 
  1. Создать пустой проект с использованием сборщика проектов Gradle
  1. Клонировать проект с GitHub (ссылка на клонирование https://github.com/DmitryAbo/QA_DIPLOMA_ABORNEV.git)
  1. Подождать когда пройдет индексация файлов и скачивание зависимостей
  1. Открыть терминал IntelliJ IDEA отправить команду - "docker-compose up"
  1. Подождать окончания создания и запуска контейнеров Docker
  1. Создать новое терминальное соединение в IntelliJ IDEA, отправить команду для запуска сервиса aqa-shop.jar с конфигурацией подключения к БД MySQL - "java "-Dspring.datasource.url=jdbc:mysql://localhost:3306/app" -jar artifacts/aqa-shop.jar"
  1. Создать новое терминальное соединение, запустить тестовый прогон в IntelliJ IDEA с конфигурацией подключения к БД MySQ и формированием отчетов Allure - "./gradlew clean test --info "-Ddb.url=jdbc:mysql://localhost:3306/app" allureReport"
  1. По окончании тестового прогона отправить команду на создание отчета по тестированию (в браузере откроется страница с отчетом Allure) - " ./gradlew allureServe "
  1. Открыть терминальное соединение в котором запускался сервис aqa-shop.jar нажать сочетание клавиш - "Ctrl+C"
  1. Подождать пока приложение закроется(появится возможность написать что-нибудь в терминал)
  1. Отправить команду для запуска сервиса aqa-shop.jar с конфигурацией подключения к БД PostgreSQL - "java "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/app" -jar artifacts/aqa-shop.jar"
  1. Открыть терминальное соединение в котором запускался тестовый прогон в IntelliJ IDEA , запустить тестовый прогон в IntelliJ IDEA с конфигурацией подключения к БД MySQ и формированием отчетов Allure - "./gradlew clean test --info "-Ddb.url=jdbc:postgresql://localhost:5432/app" allureReport"
  1. По окончании тестового прогона отправить команду на создание отчета по тестированию (в браузере откроется страница с отчетом Allure) - " ./gradlew allureServe "

**Список отчетных документов проекта автоматизации:**
  1. План тестирования веб-сервиса [Plan.md](Plan.md)
  2. Отчет по итогам тестирования [Report.md](Report.md)
  3. Отчет по итогам автоматизации [Summary.md](Summary.md)
