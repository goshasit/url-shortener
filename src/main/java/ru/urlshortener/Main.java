
package ru.urlshortener;

import ru.urlshortener.cli.ConsoleApp;

public class Main {

    public static void main(String[] args) {
        AppContext context = new AppContext();
        ConsoleApp app = new ConsoleApp(context);
        app.start();
    }
}
