package net.notjustanna.webview.interop.callback;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A functional interface representing a callback function for handling requests
 * from JavaScript in a WebView. The callback receives arguments as a Jackson
 * ArrayNode and returns a result that can be serialized to JSON.
 */
@FunctionalInterface
public interface JacksonWebviewInteropFunction {
    /**
     * Processes a request from JavaScript and returns a result.
     *
     * @param args The arguments passed to the JavaScript function as a Jackson ArrayNode.
     * @return The result of the callback, which must be handleable by
     *         {@link com.fasterxml.jackson.databind.ObjectMapper#writeValueAsString(Object)}.
     * @throws Exception If an error occurs during processing, it will be caught
     *                   and returned as a JSON string.
     */
    @Nullable
    Object apply(@NotNull ArrayNode args) throws Exception;
}