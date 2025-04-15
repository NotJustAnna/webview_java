package net.notjustanna.webview;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.Closeable;
import java.util.function.Consumer;

/**
 * Canvas component for embedding a webview in a Java AWT application.
 *
 * @author Alex Bowles, Anna Silva
 * @deprecated This component is deprecated and broken in all platforms.
 */
@Deprecated
public class WebviewComponent extends Canvas implements Closeable {
    @Getter
    private final WebviewCore webview;

    /**
     * The callback handler for when the Webview gets created.
     */
    @Setter
    @Deprecated
    private Consumer<WebviewCore> onInitialized;

    @Getter
    @Deprecated
    private final boolean initialized;

    /**
     * @deprecated This component is deprecated and broken in all platforms.
     */
    @Deprecated
    public WebviewComponent() {
        throw new UnsupportedOperationException("WebviewComponent is broken in all platforms and deprecated.");
    }

    /**
     * @deprecated This component is deprecated and broken in all platforms.
     */
    @Deprecated
    public WebviewComponent(boolean debug) {
        throw new UnsupportedOperationException("WebviewComponent is broken in all platforms and deprecated.");
    }

    @Override
    @Deprecated
    public void paint(Graphics g) {
    }

    @Override
    @Deprecated
    public void close() {
    }
}
