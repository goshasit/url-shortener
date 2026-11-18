
package ru.urlshortener.cli;

import ru.urlshortener.AppContext;
import ru.urlshortener.model.ShortLink;
import ru.urlshortener.model.User;

import java.util.Collection;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApp {

    private final AppContext context;
    private final Scanner scanner;

    public ConsoleApp(AppContext context) {
        this.context = context;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== URL SHORTENER ===");

        context.getLinkCleanupService().start();

        try {
            runMenu();
        } finally {
            context.getLinkCleanupService().stop();
            context.getStorageService().save();
            System.out.println("Данные сохранены.");
        }
    }

    private void runMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n========== МЕНЮ ==========");
            System.out.println("1. Создать пользователя");
            System.out.println("2. Выбрать пользователя");
            System.out.println("3. Создать короткую ссылку");
            System.out.println("4. Мои ссылки");
            System.out.println("5. Открыть ссылку");
            System.out.println("6. Удалить ссылку");
            System.out.println("7. Изменить лимит переходов");
            System.out.println("8. Изменить срок действия");
            System.out.println("0. Выход");
            System.out.println("==========================");

            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> login();
                    case "3" -> createLink();
                    case "4" -> showLinks();
                    case "5" -> openLink();
                    case "6" -> deleteLink();
                    case "7" -> updateMaxClicks();
                    case "8" -> updateTtl();
                    case "0" -> running = false;
                    default -> System.out.println("Неизвестная команда.");
                }
            } catch (RuntimeException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void createUser() {
        User user = context.getSessionService().createAndLogin();

        context.getStorageService().save();

        System.out.println("\nПользователь успешно создан.");
        System.out.println("Ваш UUID: " + user.getId());
        System.out.println("Вы автоматически вошли в систему.");
    }

    private void login() {
        System.out.print("Введите UUID пользователя: ");

        UUID userId = UUID.fromString(scanner.nextLine().trim());

        User user = context.getSessionService().login(userId);

        System.out.println("Вы вошли как: " + user.getId());
    }

    private void createLink() {
        UUID userId = requireUser();

        System.out.print("Введите исходный URL: ");
        String url = scanner.nextLine().trim();

        ShortLink link = context.getLinkService().createLink(
                userId,
                url
        );

        context.getStorageService().save();

        System.out.println("\nКороткая ссылка создана.");
        System.out.println("Код: " + link.getShortCode());
        System.out.println("Исходный URL: " + link.getOriginalUrl());
        System.out.println("Лимит переходов: " + link.getMaxClicks());
        System.out.println("Истекает: " + link.getExpiresAt());
    }

    private void showLinks() {
        UUID userId = requireUser();

        Collection<ShortLink> links =
                context.getLinkService().getUserLinks(userId);

        if (links.isEmpty()) {
            System.out.println("У вас пока нет ссылок.");
            return;
        }

        System.out.println("\n========== МОИ ССЫЛКИ ==========");

        for (ShortLink link : links) {
            System.out.println("-------------------------------");
            System.out.println("Код: " + link.getShortCode());
            System.out.println("URL: " + link.getOriginalUrl());

            System.out.println(
                    "Переходы: "
                            + link.getCurrentClicks()
                            + "/" + link.getMaxClicks()
            );

            System.out.println("Создана: " + link.getCreatedAt());
            System.out.println("Истекает: " + link.getExpiresAt());
        }

        System.out.println("-------------------------------");
    }

    private void openLink() {
        System.out.print("Введите короткий код: ");

        String code = scanner.nextLine().trim();

        ShortLink link = context.getLinkService().accessLink(code);

        // Сохраняем увеличенное количество переходов до открытия браузера.
        context.getStorageService().save();

        context.getBrowserUtil().open(link.getOriginalUrl());

        System.out.println("Ссылка открыта в браузере.");
        System.out.println(
                "Переходы: "
                        + link.getCurrentClicks()
                        + "/" + link.getMaxClicks()
        );
    }

    private void deleteLink() {
        UUID userId = requireUser();

        System.out.print("Введите короткий код ссылки: ");

        String code = scanner.nextLine().trim();

        context.getLinkService().deleteLink(userId, code);

        context.getStorageService().save();

        System.out.println("Ссылка успешно удалена.");
    }

    private UUID requireUser() {
        return context.getSessionService().getCurrentUserId();
    }


    private void updateMaxClicks() {
        UUID userId = requireUser();

        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine().trim();

        System.out.print("Введите новый лимит переходов: ");
        int maxClicks = Integer.parseInt(scanner.nextLine().trim());

        ShortLink link = context.getLinkService().updateMaxClicks(
                userId,
                code,
                maxClicks
        );

        context.getStorageService().save();

        System.out.println("Лимит переходов изменён.");
        System.out.println(
                "Новый лимит: "
                        + link.getMaxClicks()
        );
    }

    private void updateTtl() {
        UUID userId = requireUser();

        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine().trim();

        System.out.print("Введите новый срок действия в часах: ");
        long ttlHours = Long.parseLong(scanner.nextLine().trim());

        ShortLink link = context.getLinkService().updateTtl(
                userId,
                code,
                ttlHours
        );

        context.getStorageService().save();

        System.out.println("Срок действия изменён.");
        System.out.println(
                "Новая дата окончания: "
                        + link.getExpiresAt()
        );
    }

}
