package net.notjustanna.webview.interop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.notjustanna.webview.WebviewCore;
import net.notjustanna.webview.interop.callback.JacksonWebviewInteropFunction;
import net.notjustanna.webview.interop.callback.WebviewInteropFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that provides interop functionality for handling JavaScript requests
 * in a WebView using the Jackson library. It extends the generic WebviewReflectionInterop
 * class and specializes in handling callbacks with Jackson.
 */
public class JacksonWebviewInterop extends WebviewReflectionInterop<JacksonWebviewInterop, JacksonWebviewInteropFunction> {
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new JacksonWebviewInterop instance with a custom ObjectMapper.
     *
     * @param webview      The WebviewCore instance to associate with this interop.
     * @param objectMapper The ObjectMapper instance to use for JSON processing.
     */
    public JacksonWebviewInterop(WebviewCore webview, ObjectMapper objectMapper) {
        super(webview);
        this.objectMapper = objectMapper;
    }

    /**
     * Constructs a new JacksonWebviewInterop instance with a default ObjectMapper.
     *
     * @param webview The WebviewCore instance to associate with this interop.
     */
    public JacksonWebviewInterop(WebviewCore webview) {
        this(webview, new ObjectMapper());
    }

    /**
     * Serializes the result object to a JSON string.
     *
     * @param res The result object to serialize.
     * @return The serialized JSON string.
     * @throws Exception If an error occurs during serialization.
     */
    @Override
    protected @Nullable String handleResult(Object res) throws Exception {
        return this.objectMapper.writeValueAsString(res);
    }

    /**
     * Parses the request JSON string and maps it to an array of arguments.
     *
     * @param req    The request JSON string.
     * @param params The parameter types to map the arguments to.
     * @return An array of arguments mapped to the specified parameter types.
     * @throws Exception If an error occurs during parsing or mapping.
     */
    @Override
    protected @NotNull Object[] handleArgs(@NotNull String req, @NotNull Class<?>[] params) throws Exception {
        ArrayNode json = (ArrayNode) this.objectMapper.readTree(req);
        if (json.size() != params.length) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            JsonNode node = json.get(i);
            if (node.isNull()) {
                continue;
            }
            args[i] = this.objectMapper.treeToValue(node, params[i]);
        }
        return args;
    }

    /**
     * Wraps a JacksonWebviewInteropFunction into a WebviewInteropFunction.
     *
     * @param callback The JacksonWebviewInteropFunction to wrap.
     * @return A WebviewInteropFunction that processes JSON requests and responses.
     */
    @Override
    protected @NotNull WebviewInteropFunction wrap(@NotNull JacksonWebviewInteropFunction callback) {
        return req -> {
            ArrayNode json = (ArrayNode) this.objectMapper.readTree(req);
            Object res = callback.apply(json);
            return this.objectMapper.writeValueAsString(res);
        };
    }

    /**
     * Converts a Throwable into a JSON string representation of the error.
     *
     * @param e The Throwable to convert.
     * @return A JSON string containing error details.
     * @throws RuntimeException If an error occurs during JSON serialization.
     */
    @Override
    protected @NotNull String errorToJson(@NotNull Throwable e) {
        try {
            return this.objectMapper.writeValueAsString(e);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}