package net.notjustanna.webview.interop.callback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A functional interface representing a callback function for handling requests
 * from JavaScript in a WebView. The callback receives a request JSON Array string
 * and returns a response JSON string or {@code null} if null.
 */
@FunctionalInterface
public interface WebviewInteropFunction {
    /**
     * Processes a request from JavaScript and returns a response.
     *
     * @param req The request string in JSON Array format passed from the JavaScript function.
     * @return The response string in JSON format to be sent back to JavaScript, or {@code null} if null.
     * @throws Exception If an error occurs during execution.
     */
    @Nullable
    String apply(@NotNull String req) throws Exception;
}