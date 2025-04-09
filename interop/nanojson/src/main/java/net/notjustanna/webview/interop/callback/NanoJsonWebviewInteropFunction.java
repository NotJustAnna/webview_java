package net.notjustanna.webview.interop.callback;

import com.grack.nanojson.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A functional interface representing a callback function for handling requests
 * from JavaScript in a WebView. The callback receives arguments as a JSON array
 * and returns a result that can be serialized to JSON.
 */
@FunctionalInterface
public interface NanoJsonWebviewInteropFunction {
    /**
     * Processes a request from JavaScript and returns a result.
     *
     * @param args The arguments passed to the JavaScript function as a JSON array.
     * @return The result of the callback, which must be handleable by
     *         {@link com.grack.nanojson.JsonStringWriter#value(Object)}.
     *         Returns {@code null} if null.
     * @throws Exception If an error occurs during processing, it will be caught
     *                   and returned as a JSON string.
     */
    @Nullable
    Object apply(@NotNull JsonArray args) throws Exception;
}