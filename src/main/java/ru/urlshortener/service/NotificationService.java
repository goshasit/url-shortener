
package ru.urlshortener.service;

public class NotificationService {

    public void notifyExpiredLinks(int deletedCount) {
        if (deletedCount == 0) {
            return;
        }

        System.out.println(
                "\n[Уведомление] Автоматически удалено просроченных ссылок: "
                        + deletedCount
        );
    }
}
