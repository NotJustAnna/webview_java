package net.notjustanna.webview;

public class StandaloneExample {
    public static void main(String[] args) {
        try(var webview = new WebviewStandalone(true)) {
            webview
                .setTitle("Webview - HTML5 Test")
                .setMinSize(400, 300)
                .setSize(800, 600)
                .navigate("https://html5test.teamdev.com")
                .run();
        }
    }
}
