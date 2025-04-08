package net.notjustanna.webview;

public class StandaloneExample {
    public static void main(String[] args) {
        try(var webview = new WebviewStandalone(true)) {
            webview
                .setTitle("Webview - HTML5 Test")
                .setSize(800, 600)
                .setMinSize(400, 300)
                .navigate("https://html5test.teamdev.com")
                .maximizeWindow()
                .bringToFront()
                .run();
        }
    }
}
