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
 */
public class WebviewComponent extends Canvas implements Closeable {
    @Getter
    private WebviewCore webview;
    private final boolean debug;

    private Dimension lastSize;

    /**
     * The callback handler for when the Webview gets created.
     */
    @Setter
    private Consumer<WebviewCore> onInitialized;

    private @Getter boolean initialized = false;

    public WebviewComponent() {
        this(false);
    }

    /**
     * @param debug Whether to allow the opening of inspect element/devtools.
     */
    public WebviewComponent(boolean debug) {
        this.debug = debug;
        this.setBackground(Color.BLACK);
    }

    @Override
    public void paint(Graphics g) {
        Dimension size = this.getSize();

        if (!size.equals(this.lastSize)) {
            this.lastSize = size;

            if (this.webview != null) {
                this.updateSize();
            }
        }

        if (!this.initialized) {
            this.initialized = true;

            // We need to create the webview off of the swing thread.
            Thread t = new Thread(() -> {
                this.webview = WebviewCore.newComponent(this.debug, this);
                this.updateSize();

                if (this.onInitialized != null) {
                    this.onInitialized.accept(this.webview);
                }

                this.webview.run();
            });
            t.setDaemon(false);
            t.setName("AWTWebview RunAsync Thread - #" + this.hashCode());
            t.start();
        }
    }

    private void updateSize() {
        int width = this.lastSize.width;
        int height = this.lastSize.height;

        this.webview.setFixedSize(width, height);
    }

    @Override
    public void close() {
        this.webview.close();
        this.initialized = false;
        this.webview = null;
    }

}
