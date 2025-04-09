package net.notjustanna.webview.interop;

import com.sun.jna.Pointer;
import net.notjustanna.webview.WebviewCore;
import net.notjustanna.webview.interop.callback.WebviewInteropFunction;
import net.notjustanna.webview.natives.WebviewNative;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing the interoperation layer between Java and a WebView.
 * This class provides methods to bind and unbind JavaScript functions to Java callbacks,
 * as well as utilities for safely handling strings and errors.
 *
 * @param <Self> The type of the subclass extending this class.
 * @param <Fn>   The type of the callback function.
 */
public abstract class WebviewInterop<Self extends WebviewInterop<Self, Fn>, Fn> {
    /**
     * The core WebView instance used for binding and unbinding functions.
     */
    protected final WebviewCore webview;

    /**
     * Constructs a new WebviewInterop instance.
     *
     * @param webview The WebView core instance.
     */
    public WebviewInterop(WebviewCore webview) {
        this.webview = webview;
    }

    /**
     * Binds a Java callback function to a new global JavaScript function.
     * Internally, JavaScript glue code is injected to create the function.
     *
     * @param name     The name of the JavaScript function.
     * @param callback The Java callback function to bind.
     * @return The current instance of the class (for chaining).
     */
    public Self bind(@NotNull String name, @NotNull Fn callback) {
        WebviewNative.BindCallback nativeCallback = this.wrapNative(this.wrap(callback));
        webview.bind(name, nativeCallback);
        return castThis();
    }

    /**
     * Unbinds a previously bound JavaScript function, removing it from future pages.
     *
     * @param name The name of the JavaScript function to unbind.
     * @return The current instance of the class (for chaining).
     */
    public Self unbind(@NotNull String name) {
        webview.unbind(name);
        return castThis();
    }

    /**
     * Wraps a callback function into a WebviewInteropCallback.
     *
     * @param callback The callback function to wrap.
     * @return A wrapped WebviewInteropCallback.
     */
    @NotNull
    protected abstract WebviewInteropFunction wrap(@NotNull Fn callback);

    /**
     * Converts a Throwable into a JSON string representation of the error.
     *
     * @param e The Throwable to convert.
     * @return A JSON string representing the error.
     */
    @NotNull
    protected abstract String errorToJson(@NotNull Throwable e);

    /**
     * Wraps a WebviewInteropCallback into a native WebView callback.
     *
     * @param callback The WebviewInteropCallback to wrap.
     * @return A native WebView BindCallback.
     */
    @NotNull
    protected WebviewNative.BindCallback wrapNative(@NotNull WebviewInteropFunction callback) {
        Pointer wv = WebviewCore.nativePointer(this.webview);
        return (id, req, arg) -> {
            try {
                String str = WebviewInterop.safeString(req);
                String res = callback.apply(str);
                if (res == null) {
                    res = "null";
                }
                WebviewNative.INSTANCE.webview_return(wv, id, 0, res);
            } catch (Exception e) {
                String res = this.errorToJson(e);
                WebviewNative.INSTANCE.webview_return(wv, id, 0, res);
            }
        };
    }

    /**
     * Casts the current instance to the type of the subclass.
     * This is safe because the type of "this" will always match "Self".
     *
     * @return The current instance cast to the type of the subclass.
     */
    @SuppressWarnings("unchecked")
    protected Self castThis() {
        return (Self) this;
    }

    /**
     * Safely processes a string by escaping null characters and non-ASCII characters.
     *
     * @param str The input string to process.
     * @return A safe string with escaped characters.
     */
    @NotNull
    private static String safeString(@NotNull String str) {
        int length = str.length();
        StringBuilder sb = new StringBuilder(length);

        int lastIndex = 0;

        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);

            if ((ch == 0 || ch > 127)) {
                // Append previous safe chunk in bulk
                if (lastIndex < i) {
                    sb.append(str, lastIndex, i);
                }

                if (ch == 0) {
                    sb.append("\\u0000");
                } else {
                    sb.append("\\u").append(String.format("%04x", (int) ch));
                }

                lastIndex = i + 1;
            }
        }

        // Append remaining safe chunk if any
        if (lastIndex < length) {
            sb.append(str, lastIndex, length);
        }

        return sb.toString();
    }

}