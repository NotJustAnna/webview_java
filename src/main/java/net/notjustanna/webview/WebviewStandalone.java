package net.notjustanna.webview;

import lombok.Getter;
import net.notjustanna.webview.natives.PlatformSpecific;
import net.notjustanna.webview.natives.WebviewNative;
import net.notjustanna.webview.natives.WinHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Standalone webview instance.
 *
 * @author Alex Bowles, Anna Silva
 */
@Getter
public class WebviewStandalone implements Closeable, Runnable {
    /**
     * Access to the webview core instance.
     */
    @NotNull
    private final WebviewCore webview;

    /**
     * Creates a new standalone webview instance.
     * @param enableDevTools Enable developer tools if supported by the backend.
     */
    public WebviewStandalone(Boolean enableDevTools) {
        this.webview = WebviewCore.newStandalone(enableDevTools);
    }

    /**
     * The dispatcher responsible for managing and executing native webview operations.
     * <p>
     * This field provides access to the {@link WebviewDispatcher} instance, which is used
     * to execute tasks on the native webview thread.
     */
    public WebviewDispatcher getDispatcher() {
        return webview.getDispatcher();
    }

    /**
     * Retrieves a list of all currently bound JavaScript function names.
     * <p>
     * This method returns the keys from the internal map of bindings, which
     * represent the names of JavaScript functions bound to native callbacks.
     * <p>
     * Remark: The list may be outdated if there are any pending bindings that
     * have not yet been executed. This is because the bindings are executed
     * asynchronously on the webview thread.
     *
     * @return A list of bound JavaScript function names.
     */
    public Set<String> boundFunctions() {
        return webview.boundFunctions();
    }

    /**
     * The error handler for uncaught exceptions.
     * <p>
     * This {@link Consumer} is invoked when an uncaught exception occurs
     * during task execution. By default, it logs the exception as a warning.
     *
     * @param errorHandler The error handler to set.
     */
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        webview.setErrorHandler(errorHandler);
    }

    /**
     * Terminates the webview instance asynchronously.
     * <p>
     * This method dispatches a task to the webview thread to terminate the webview,
     * clear all bindings, and release resources. It returns a `CompletableFuture`
     * that completes when the termination process is finished.
     *
     * @return A `CompletableFuture` that completes when the webview is terminated.
     */
    public CompletableFuture<Void> terminate() {
        return webview.terminate();
    }

    /**
     * Closes the webview, call this to end the event loop and free up resources.
     */
    @Override
    public void close() {
        webview.close();
    }

    /**
     * @deprecated <a href="https://github.com/NotJustAnna/webview_java/issues/1">Does not work</a>.
     * Use {@link #run()} instead.
     *
     * @see #close()
     */
    @Deprecated
    public Thread runAsync() {
        return webview.runAsync();
    }

    /**
     * Executes the webview event loop until the user presses "X" on the window.
     *
     * @see #close()
     */
    @Override
    public void run() {
        webview.run();
    }

    /**
     * Dispatches a runnable to the webview thread.
     * @deprecated This method is deprecated and will be removed in future versions.
     * @param handler callback to be executed on the webview thread.
     * @return Itself, for chaining.
     */
    @Deprecated
    public WebviewStandalone dispatch(@NotNull Runnable handler) {
        webview.dispatch(handler);
        return this;
    }

    /**
     * Unbinds a function, removing it from future pages.
     *
     * @param name The name of the function.
     */
    public WebviewStandalone unbind(@NotNull String name) {
        webview.unbind(name);
        return this;
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
    public WebviewStandalone bind(@NotNull String name, WebviewNative.@NotNull BindCallback callback) {
        webview.bind(name, callback);
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
    public WebviewStandalone evaluate(@NotNull String script) {
        webview.evaluate(script);
        return this;
    }

    /**
     * Sets the initial script to be executed when the webview is created.
     *
     * @param script The script to be executed.
     * @return Itself for chaining.
     */
    public WebviewStandalone setInitScript(@NotNull String script) {
        webview.setInitScript(script);
        return this;
    }

    /**
     * Sets the fixed size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewStandalone setFixedSize(int width, int height) {
        webview.setFixedSize(width, height);
        return this;
    }

    /**
     * Sets the size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewStandalone setSize(int width, int height) {
        webview.setSize(width, height);
        return this;
    }

    /**
     * Sets the maximum size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewStandalone setMaxSize(int width, int height) {
        webview.setMaxSize(width, height);
        return this;
    }

    /**
     * Sets the minimum size of the webview window.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     * @return Itself for chaining.
     */
    public WebviewStandalone setMinSize(int width, int height) {
        webview.setMinSize(width, height);
        return this;
    }

    /**
     * Sets the title of the webview window.
     *
     * @param title Title to set.
     * @return Itself for chaining.
     * @implNote Safe to do on standalone webviews. Undefined behavior on component-attached webviews.
     */
    public WebviewStandalone setTitle(@NotNull String title) {
        webview.setTitle(title);
        return this;
    }

    /**
     * Navigates webview to the given URL. URL may be a properly encoded data URI.
     *
     * @param url URL to navigate to. If null, navigates to about:blank.
     * @return Itself for chaining.
     */
    public WebviewStandalone navigate(@Nullable String url) {
        webview.navigate(url);
        return this;
    }

    /**
     * Load HTML content into the webview.
     *
     * @param html HTML content.
     * @return Itself for chaining.
     */
    public WebviewStandalone setHtml(@Nullable String html) {
        webview.setHtml(html);
        return this;
    }

    /**
     * Sets the dark mode appearance for the webview window.
     *
     * @param darkMode Whether to enable dark mode.
     * @return Itself for chaining.
     */
    public WebviewStandalone setDarkMode(boolean darkMode) {
        if (PlatformSpecific.current.isWindows()) {
            webview.getDispatcher().wrapExec(() -> WinHelper.setWindowAppearance(this.webview, darkMode));
        }
        return this;
    }

    /**
     * Brings the webview window to the front of all other windows.
     *
     * @return Itself for chaining.
     */
    public WebviewStandalone bringToFront() {
        if (PlatformSpecific.current.isWindows()) {
            webview.getDispatcher().wrapExec(() -> WinHelper.bringToFront(this.webview));
        }
        return this;
    }

    /**
     * Sets the webview window to always stay on top of other windows.
     *
     * @return Itself for chaining.
     */
    public WebviewStandalone alwaysOnTop() {
        if (PlatformSpecific.current.isWindows()) {
            webview.getDispatcher().wrapExec(() -> WinHelper.alwaysOnTop(this.webview));
        }
        return this;
    }

    /**
     * Sets the webview window to fullscreen mode.
     *
     * @return Itself for chaining.
     */
    public WebviewStandalone fullscreenWindow() {
        if (PlatformSpecific.current.isWindows()) {
            webview.getDispatcher().wrapExec(() -> WinHelper.fullscreenWindow(this.webview));
        }
        return this;
    }

    /**
     * Maximizes the webview window.
     *
     * @return Itself for chaining.
     */
    public WebviewStandalone maximizeWindow() {
        if (PlatformSpecific.current.isWindows()) {
            webview.getDispatcher().wrapExec(() -> WinHelper.maximizeWindow(this.webview));
        }
        return this;
    }
}
