package net.notjustanna.webview;

import java.awt.*;

public class AWTExample extends Frame {
    public static void main(String[] args) {
        new AWTExample().setVisible(true);
    }

    AWTExample() {
        WebviewComponent webview = new WebviewComponent(true);

        webview.setOnInitialized((wv) -> {
            wv.navigate("https://html5test.teamdev.com");
        });
        webview.setSize(800,600);

        add(webview);
        pack();
    }
}
