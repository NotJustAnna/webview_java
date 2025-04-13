package net.notjustanna.webview;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.Getter;
import lombok.extern.java.Log;
import net.notjustanna.webview.natives.JvmHelper;
import net.notjustanna.webview.natives.MacHelper;
import net.notjustanna.webview.natives.PlatformSpecific;
import net.notjustanna.webview.natives.WebviewNative;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Core webview instance, responsible for properly interfacing with native libraries.
 *
 * @author Alex Bowles, Anna Silva
 * @implNote This class is not meant to be used directly, use {@link WebviewStandalone} or {@link WebviewComponent} instead.
 */
@Log
public class WebviewCore implements Closeable, Runnable {

    /**
     * Creates a new standalone webview instance.
     *
     * @param enableDevTools Enable developer tools if supported by the backend.
     * @return A new webview core instance.
     * @implNote use {@link WebviewStandalone#WebviewStandalone(Boolean)} instead.
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

    /**
     * Concurrent Map of all bindings to their respective callbacks.
     * This is used to keep references to the callbacks, so they are not garbage collected
     * and can be accessed by the native code.
     */
    private final Map<String, Object> bindRefs;

    /**
     * The dispatcher responsible for managing and executing native webview operations.
     * <p>
     * This field provides access to the {@link WebviewDispatcher} instance, which is used
     * to execute tasks on the native webview thread.
     */
    @Getter
    private final WebviewDispatcher dispatcher;

    /**
     * Weak reference to the thread that created the webview instance.
     * Used to reference if {@link #run()} is called from the thread that created the webview.
     */
    private final WeakReference<Thread> threadRef;

    /**
     * Creates a new webview instance.
     *
     * @param enableDevTools Enable developer tools if supported by the backend.
     * @param windowPointer  Optional native window handle.
     */
    public WebviewCore(boolean enableDevTools, @Nullable Pointer windowPointer) {
        WebviewCore.checkEnvironment();
        $webview_t = WebviewNative.INSTANCE.webview_create(enableDevTools, windowPointer);
        if ($webview_t == null) {
            throw new RuntimeException(ERROR_FAILED_TO_CREATE_WEBVIEW);
        }
        threadRef = new WeakReference<>(Thread.currentThread());
        bindRefs = new ConcurrentHashMap<>();
        dispatcher = new WebviewDispatcher($webview_t, threadRef);
        WebviewCore.warnIfNotMainThread();
    }

    /**
     * Load HTML content into the webview.
     *
     * @param html HTML content.
     * @return Itself for chaining.
     */
    public WebviewCore setHtml(@Nullable String html) {
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_set_html($webview_t, html));
        return this;
    }

    /**
     * Navigates webview to the given URL. URL may be a properly encoded data URI.
     *
     * @param url URL to navigate to. If null, navigates to about:blank.
     * @return Itself for chaining.
     */
    public WebviewCore navigate(@Nullable String url) {
        String param = url == null ? "about:blank" : url;
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_navigate($webview_t, param));
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
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_set_title($webview_t, title));
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
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_MIN));
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
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_MAX));
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
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_NONE));
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
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_set_size($webview_t, width, height, WebviewNative.HINT_FIXED));
        return this;
    }

    /**
     * Sets the initial script to be executed when the webview is created.
     *
     * @param script The script to be executed.
     * @return Itself for chaining.
     */
    public WebviewCore setInitScript(@NotNull String script) {
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_init($webview_t, script));
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
        dispatcher.execNative(() -> WebviewNative.INSTANCE.webview_eval($webview_t, script));
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
    * @return Itself for chaining.
     */
    public WebviewCore bind(@NotNull String name, @NotNull WebviewNative.BindCallback callback) {
        dispatcher.wrapExec(() -> {
            int result = WebviewNative.INSTANCE.webview_bind($webview_t, name, callback, null);
            if (result == WebviewNative.ERROR_DUPLICATE) {
                throw new IllegalArgumentException("A binding already exists with the name: " + name);
            } else if (result != WebviewNative.ERROR_OK) {
                WebviewCore.handleError(result);
            }
            bindRefs.put(name, callback);
        });
        return this;
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
        return Set.copyOf(bindRefs.keySet());
    }

    /**
     * Unbinds a function, removing it from future pages.
     *
     * @param name The name of the function.
     * @return Itself for chaining.
     */
    public WebviewCore unbind(@NotNull String name) {
        dispatcher.wrapExec(() -> {
            int result = WebviewNative.INSTANCE.webview_unbind($webview_t, name);
            if (result == WebviewNative.ERROR_NOT_FOUND) {
                throw new NoSuchElementException("No binding found with the name: " + name);
            } else if (result != WebviewNative.ERROR_OK) {
                WebviewCore.handleError(result);
            }
            bindRefs.remove(name);
        });
        return this;
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
        getDispatcher().setErrorHandler(errorHandler);
    }

    /**
     * Executes the webview event loop until the webview is closed.
     *
     * @see #close()
     */
    @Override
    public void run() {
        if (!threadRef.refersTo(Thread.currentThread())) {
            throw new UnsupportedOperationException(ERROR_DIFFERENT_THREAD_RUN);
        }
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_run($webview_t));
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_destroy($webview_t));
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
        return dispatcher.runAsync(() -> {
            WebviewCore.handleError(WebviewNative.INSTANCE.webview_terminate($webview_t));
            bindRefs.clear();
        });
    }

    /**
     * Closes the webview, call this to end the event loop and free up resources.
     * <p>
     * Same as {@link #terminate() this.terminate().join()}
     */
    @Override
    public void close() {
        terminate().join();
    }

    /**
     * @deprecated <a href="https://github.com/NotJustAnna/webview_java/issues/1">Does not work</a>.
     * Use {@link #run()} instead.
     *
     * @see #close()
     */
    @Deprecated
    public Thread runAsync() {
        throw new UnsupportedOperationException(ERROR_RUN_ASYNC);
    }

    /**
     * Dispatches a runnable to the webview thread.
     * @deprecated This method is deprecated and will be removed in future versions. Use {@link #getDispatcher()}.{@link WebviewDispatcher#execute(Runnable)} instead.
     * @param handler callback to be executed on the webview thread.
     * @return Itself, for chaining.
     * @see #getDispatcher()
     * @see WebviewDispatcher#execute(Runnable)
     * @see WebviewDispatcher#runAsync(Runnable)
     * @see WebviewDispatcher#supplyAsync(Supplier)
     */
    @Deprecated
    public WebviewCore dispatch(@NotNull Runnable handler) {
        dispatcher.execute(handler);
        return this;
    }

    /**
     * Checks the environment to ensure compatibility with the current platform.
     * <p>
     * This method performs platform-specific checks to verify that the application
     * is running in a supported environment.
     *
     * @throws UnsupportedOperationException if the environment does not meet the required conditions.
     */
    private static void checkEnvironment() {
        if (PlatformSpecific.current == PlatformSpecific.DARWIN) {
            if (!MacHelper.startedOnFirstThread()) {
                String extra = JvmHelper.isMainThread() ? MACOS_RELOAD : MACOS_DEVELOPER_ERROR;
                throw new UnsupportedOperationException(ERROR_NO_XSTART_ON_FIRST_THREAD + extra);
            }

            if (!JvmHelper.isMainThread()) {
                throw new UnsupportedOperationException(ERROR_MAC_OS_NOT_MAIN_THREAD);
            }
        }

        if (JvmHelper.isCurrentThreadVirtual()) {
            throw new UnsupportedOperationException(ERROR_VIRTUAL_THREAD);
        }
    }

    /**
     * Logs a warning if the current thread is not the main thread.
     */
    private static void warnIfNotMainThread() {
        if (!JvmHelper.isMainThread() && !Boolean.getBoolean("webview.ignore-not-main-thread")) {
            log.warning(WARN_NOT_MAIN_THREAD);
        }
    }

    /**
     * Default error handler for the webview.
     * @param result The result code from the webview.
     */
    static void handleError(int result) {
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

    private static final String ERROR_DIFFERENT_THREAD_RUN = "Webview has to be executed on the same thread it was created on. " +
        "This is a limitation of the underlying webview library.";

    private static final String MACOS_RELOAD = "Reload the application with -XstartOnFirstThread to fix this.";

    private static final String MACOS_DEVELOPER_ERROR = "Also, webview has to be run on JVM's main thread. " +
        "This is a limitation of MacOS.";

    private static final String ERROR_NO_XSTART_ON_FIRST_THREAD = "Process was not started with -XstartOnFirstThread. ";

    private static final String ERROR_MAC_OS_NOT_MAIN_THREAD = "Cannot create webview on a non-main thread on macOS.";

    private static final String ERROR_FAILED_TO_CREATE_WEBVIEW = "Failed to create webview";

    private static final String ERROR_RUN_ASYNC = "runAsync() is deprecated, as it is not possible to run the webview " +
        "on a different thread than the one it was created on. Please use run() or create a webview instance in a separate thread.";

    private static final String ERROR_VIRTUAL_THREAD = "Webview cannot be created on a virtual thread. " +
        "This is a limitation of the underlying webview library. Since it blocks the thread indefinitely, it's also a misuse of virtual threads.";

    private static final String WARN_NOT_MAIN_THREAD = "Webview was not created on the main thread. " +
        "Running on a non-main thread is not supported on some platfoms, such as macOS.";
}