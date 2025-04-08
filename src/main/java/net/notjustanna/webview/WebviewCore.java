package net.notjustanna.webview;

import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;
import net.notjustanna.webview.natives.WebviewNative;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Closeable;

/**
 * This is the class responsible for mapping the native webview API to Java.
 * I strongly recommend you to use {@link WebviewStandalone} or {@link WebviewAWT}
 * instead of this class.
 */
public class WebviewCore implements Closeable, Runnable {
    public static WebviewCore newStandalone(Boolean debug) {
        return new WebviewCore(debug, null);
    }

    public static WebviewCore newComponent(boolean debug, @NotNull Component component) {
        return new WebviewCore(debug, new PointerByReference(Native.getComponentPointer(component)));
    }

    /*
     * DEVELOPER NOTE:
     * This pointer might be entirely convertable to a Pointer object. (duh.)
     * What tells me that is the fact that WinHelper#setWindowAppearance used to immediately convert
     * the $pointer field to a Pointer object.
     *
     * TODO Investigate.
     */
    public final long $pointer;

    public WebviewCore(boolean debug, @Nullable PointerByReference windowPointer) {
        $pointer = WebviewNative.INSTANCE.webview_create(debug, windowPointer);
    }

    public WebviewCore setHtml(@Nullable String html) {
        WebviewNative.INSTANCE.webview_set_html($pointer, html);
        return this;
    }

    public WebviewCore navigate(@Nullable String url) {
        WebviewNative.INSTANCE.webview_navigate($pointer, url == null ? "about:blank" : url);
        return this;
    }

    public WebviewCore setTitle(@NotNull String title) {
        WebviewNative.INSTANCE.webview_set_title($pointer, title);
        return this;
    }

    public WebviewCore setMinSize(int width, int height) {
        WebviewNative.INSTANCE.webview_set_size($pointer, width, height, WebviewNative.HINT_MIN);
        return this;
    }

    public WebviewCore setMaxSize(int width, int height) {
        WebviewNative.INSTANCE.webview_set_size($pointer, width, height, WebviewNative.HINT_MAX);
        return this;
    }

    public WebviewCore setSize(int width, int height) {
        WebviewNative.INSTANCE.webview_set_size($pointer, width, height, WebviewNative.HINT_NONE);
        return this;
    }

    public WebviewCore setFixedSize(int width, int height) {
        WebviewNative.INSTANCE.webview_set_size($pointer, width, height, WebviewNative.HINT_FIXED);
        return this;
    }

    public WebviewCore setInitScript(@NotNull String script) {
        WebviewNative.INSTANCE.webview_init($pointer, script);
        return this;
    }

    public WebviewCore evaluate(@NotNull String script) {
        return this.dispatch(() -> WebviewNative.INSTANCE.webview_eval($pointer, script));
    }

    /**
     * Binds a function to the JavaScript environment on page load.
     *
     * @param name     The name to be used for the function, e.g "foo" to get
     *                 foo().
     * @param callback The callback handler, accepts a JsonArray (which are all
     *                 arguments passed to the function()) and returns a value
     *                 which is of type JsonElement (can be null). Exceptions are
     *                 automatically passed back to JavaScript.
     * @implNote This get's called AFTER window.load.
     * @implSpec After calling the function in JavaScript you will get a
     * Promise instead of the value. This is to prevent you from
     * locking up the browser while waiting on your Java code to
     * execute and generate a return value.
     */
    public WebviewCore bind(@NotNull String name, @NotNull WebviewNative.NativeBindCallback callback) {
        WebviewNative.INSTANCE.webview_bind($pointer, name, callback, 0);
        return this;
    }

    /**
     * Unbinds a function, removing it from future pages.
     *
     * @param name The name of the function.
     */
    public WebviewCore unbind(@NotNull String name) {
        WebviewNative.INSTANCE.webview_unbind($pointer, name);
        return this;
    }

    public WebviewCore dispatch(@NotNull Runnable handler) {
        WebviewNative.INSTANCE.webview_dispatch($pointer, ($pointer, arg) -> handler.run(), 0);
        return this;
    }

    /**
     * Executes the webview event loop until the user presses "X" on the window.
     *
     * @see #close()
     */
    @Override
    public void run() {
        WebviewNative.INSTANCE.webview_run($pointer);
        WebviewNative.INSTANCE.webview_destroy($pointer);
    }

    /**
     * Executes the webview event loop asynchronously until the user presses "X" on
     * the window.
     *
     * @see #close()
     */
    public Thread runAsync() {
        Thread t = new Thread(this);
        t.setDaemon(false);
        t.setName("Webview RunAsync Thread - #" + this.hashCode());
        t.start();
        return t;
    }

    /**
     * Closes the webview, call this to end the event loop and free up resources.
     */
    @Override
    public void close() {
        WebviewNative.INSTANCE.webview_terminate($pointer);
    }
}