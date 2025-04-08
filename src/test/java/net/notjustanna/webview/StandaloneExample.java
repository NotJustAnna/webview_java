package net.notjustanna.webview;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class StandaloneExample {
    public static void main(String[] args) {
        try(var webview = new WebviewStandalone(true)) {
//            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute();
            webview
                .setTitle("Webview - HTML5 Test")
                .setSize(800, 600)
                .setMinSize(400, 300)
                .navigate("https://html5test.teamdev.com")
                .setDarkMode(false)
                .bringToFront()
                .run();

//            Thread.sleep(100);

//            webview.bringToFront();
        }
    }
}
