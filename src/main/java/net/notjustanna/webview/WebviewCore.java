package net.notjustanna.webview;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import net.notjustanna.webview.natives.WebviewNative;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Closeable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core webview instance, responsible for properly interfacing with native libraries.
 *
 * @author Alex Bowles, Anna Silva
 * @implNote This class is not meant to be used directly, use {@link WebviewStandalone} or {@link WebviewComponent} instead.
 */
public class WebviewCore implements Closeable, Runnable {
    /**
     * Creates a new standalone webview instance.
     *
     * @param enableDevTools Enable developer tools if supported by the backend.
     * @return A new webview core instance.
     * @implNote use {@link WebviewStandalone} instead.
     */
    public static WebviewCore newStandalone(Boolean enableDevTools) {
        return new WebviewCore(enableDevTools, null);
    }


    /**
     * Creates a new webview instance inside a component.
     *
     * @param enableDevTools Enable developer tools if supported by the backend.
     * @param component      The component to create the webview in.
     * @return A new webview core instance.
     * @implNote use {@link WebviewComponent} instead.
     */
    public static WebviewCore newComponent(boolean enableDevTools, @NotNull Component component) {
        Pointer componentPointer = Native.getComponentPointer(component);
        return new WebviewCore(enableDevTools, componentPointer);
    }

    /**
     * Raw access to the webview instance pointer.
     *
     * @param webview The webview instance.
     * @return The pointer to the webview instance.
     */
    public static Pointer nativePointer(WebviewCore webview) {
        return webview.$webview_t;
    }

    /**
     * The pointer to the webview instance.
     */
    private final Pointer $webview_t;

    private final Map<String, Object> bind_refs = new ConcurrentHashMap<>();

    /**
     * Creates a new webview instance.
     *
     * @param enableDevTools Enable developer tools if supported by the backend.
     * @param windowPointer  Optional native window handle.
     */
    public WebviewCore(boolean enableDevTools, @Nullable Pointer windowPointer) {
        $webview_t = WebviewNative.INSTANCE.webview_create(enableDevTools, windowPointer);
        if ($webview_t == null) {
            throw new RuntimeException("Failed to create webview");
        }
        if (Pointer.nativeValue($webview_t) == WebviewNative.ERROR_MISSING_DEPENDENCY) {
            throw new UnsupportedOperationException("Failed to create a webview, due to a missing dependency. " +
                "If you are on Windows, this means that Webview2 is unavailable.");
        }
    }

    /**
     * Load HTML content into the webview.
     *
     * @param html HTML content.
     * @return Itself for chaining.
     */
    public WebviewCore setHtml(@Nullable String html) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_set_html($webview_t, html));
        return this;
    }

    /**
     * Navigates webview to the given URL. URL may be a properly encoded data URI.
     *
     * @param url URL to navigate to. If null, navigates to about:blank.
     * @return Itself for chaining.
     */
    public WebviewCore navigate(@Nullable String url) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_navigate($webview_t, url == null ? "about:blank" : url));
        return this;
    }

    /**
     * Sets the title of the webview window.
     *
     * @param title Title to set.
     * @return Itself for chaining.
     * @implNote Safe to do on standalone webviews. Undefined behavior on component-attached webviews.
     */
    public WebviewCore setTitle(@NotNull String title) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_set_title($webview_t, title));
        return this;
    }

    /**
     * Sets the minimum size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewCore setMinSize(int width, int height) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_MIN));
        return this;
    }

    /**
     * Sets the maximum size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewCore setMaxSize(int width, int height) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_MAX));
        return this;
    }

    /**
     * Sets the size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewCore setSize(int width, int height) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_NONE));
        return this;
    }

    /**
     * Sets the fixed size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewCore setFixedSize(int width, int height) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_FIXED));
        return this;
    }

    /**
     * Sets the initial script to be executed when the webview is created.
     *
     * @param script The script to be executed.
     * @return Itself for chaining.
     */
    public WebviewCore setInitScript(@NotNull String script) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_init($webview_t, script));
        return this;
    }

    /**
     * Evaluates a JavaScript script in the webview.
     *
     * @param script The script to be evaluated.
     * @return Itself for chaining.
     * @implNote The script is dispatched to the webview thread, so it is safe to call this from any thread.
     * Also, this means that the script is executed asynchronously.
     */
    public WebviewCore evaluate(@NotNull String script) {
        return this.dispatch(() -> WebviewNative.INSTANCE.webview_eval($webview_t, script));
    }

    /**
     * Binds a function pointer to a new global JavaScript function.
     * <p>
     * Internally, JS glue code is injected to create the JS function by the
     * given name. The callback function is passed a request identifier,
     * a request string and a user-provided argument. The request string is
     * a JSON array of the arguments passed to the JS function.
     *
     * @param name     Name of the JS function.
     * @param callback Callback function.
     * @return WEBVIEW_ERROR_DUPLICATE, if a binding already exists with the specified name.
     */
    public WebviewCore bind(@NotNull String name, @NotNull WebviewNative.BindCallback callback) {
        int result = WebviewNative.INSTANCE.webview_bind($webview_t, name, callback, null);
        if (result == WebviewNative.ERROR_DUPLICATE) {
            throw new IllegalArgumentException("A binding already exists with the name: " + name);
        } else if (result != WebviewNative.ERROR_OK) {
            WebviewCore.handleError(result);
        }
        bind_refs.put(name, callback);
        return this;
    }

    /**
     * Unbinds a function, removing it from future pages.
     *
     * @param name The name of the function.
     */
    public WebviewCore unbind(@NotNull String name) {
        int result = WebviewNative.INSTANCE.webview_unbind($webview_t, name);
        if (result == WebviewNative.ERROR_NOT_FOUND) {
            throw new NoSuchElementException("No binding found with the name: " + name);
        } else if (result != WebviewNative.ERROR_OK) {
            WebviewCore.handleError(result);
        }
        return this;
    }

    /**
     * Dispatches a runnable to the webview thread.
     *
     * @implNote Be very mindful of the fact that this will block the webview thread.
     * @param handler callback to be executed on the webview thread.
     * @return Itself for chaining.
     */
    public WebviewCore dispatch(@NotNull Runnable handler) {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_dispatch($webview_t, ($pointer, arg) -> handler.run(), null));
        return this;
    }

    /**
     * Executes the webview event loop until the user presses "X" on the window.
     *
     * @see #close()
     */
    @Override
    public void run() {
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_run($webview_t));
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_destroy($webview_t));
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
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_terminate($webview_t));
        bind_refs.clear();
    }

    /**
     * Default error handler for the webview.
     * @param result The result code from the webview.
     */
    private static void handleError(int result) {
        String errorMessage;
        switch (result) {
            case WebviewNative.ERROR_MISSING_DEPENDENCY:
                errorMessage = "WebView2 is unavailable. Please ensure that Webview2 is installed.";
                break;
            case WebviewNative.ERROR_CANCELED:
                errorMessage = "Received WM_QUIT.";
                break;
            case WebviewNative.ERROR_INVALID_STATE:
                errorMessage = "Invalid state.";
                break;
            case WebviewNative.ERROR_INVALID_ARGUMENT:
                errorMessage = "Invalid argument.";
                break;
            case WebviewNative.ERROR_UNSPECIFIED:
                errorMessage = "Unspecified error.";
                break;
            case WebviewNative.ERROR_OK:
                return;
            case WebviewNative.ERROR_DUPLICATE:
                errorMessage = "Duplicate binding.";
                break;
            case WebviewNative.ERROR_NOT_FOUND:
                errorMessage = "Binding not found.";
                break;
            default:
                errorMessage = "Unknown error code: " + result;
                break;
        }
        throw new RuntimeException(errorMessage);
    }
}