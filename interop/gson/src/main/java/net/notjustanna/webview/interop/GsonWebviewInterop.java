package net.notjustanna.webview.interop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.notjustanna.webview.WebviewCore;
import net.notjustanna.webview.interop.callback.GsonWebviewInteropFunction;
import net.notjustanna.webview.interop.callback.WebviewInteropFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that provides interop functionality for handling JavaScript requests
 * in a WebView using the Gson library. It extends the generic WebviewReflectionInterop
 * class and specializes in handling callbacks with Gson.
 */
public class GsonWebviewInterop extends WebviewReflectionInterop<GsonWebviewInterop, GsonWebviewInteropFunction> {
    private final Gson gson;

    /**
     * Constructs a new GsonWebviewInterop instance with a custom Gson instance.
     *
     * @param webview The WebviewCore instance to associate with this interop.
     * @param gson    The Gson instance to use for JSON processing.
     */
    public GsonWebviewInterop(WebviewCore webview, Gson gson) {
        super(webview);
        this.gson = gson;
    }

    /**
     * Constructs a new GsonWebviewInterop instance with a default Gson instance.
     *
     * @param webview The WebviewCore instance to associate with this interop.
     */
    public GsonWebviewInterop(WebviewCore webview) {
        this(webview, new Gson());
    }

    /**
     * Serializes the result object to a JSON string.
     *
     * @param res The result object to serialize.
     * @return The serialized JSON string, or {@code null} if the result is null.
     */
    @Override
    protected @Nullable String handleResult(Object res) {
        return this.gson.toJson(res);
    }

    /**
     * Parses the request JSON string and maps it to an array of arguments.
     *
     * @param req    The request JSON string.
     * @param params The parameter types to map the arguments to.
     * @return An array of arguments mapped to the specified parameter types.
     * @throws IllegalArgumentException If the number of arguments does not match the parameter types.
     */
    @Override
    protected @NotNull Object[] handleArgs(@NotNull String req, @NotNull Class<?>[] params) {
        JsonArray json = this.gson.toJsonTree(req).getAsJsonArray();
        if (json.size() != params.length) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            JsonElement node = json.get(i);
            if (node.isJsonNull()) {
                continue;
            }
            args[i] = this.gson.fromJson(node, params[i]);
        }
        return args;
    }

    /**
     * Wraps a GsonWebviewInteropFunction into a WebviewInteropFunction.
     *
     * @param callback The GsonWebviewInteropFunction to wrap.
     * @return A WebviewInteropFunction that processes JSON requests and responses.
     */
    @Override
    protected @NotNull WebviewInteropFunction wrap(@NotNull GsonWebviewInteropFunction callback) {
        return req -> {
            JsonArray json = this.gson.toJsonTree(req).getAsJsonArray();
            Object res = callback.apply(json);
            return handleResult(res);
        };
    }

    /**
     * Converts a Throwable into a JSON string representation of the error.
     *
     * @param e The Throwable to convert.
     * @return A JSON string containing error details.
     */
    @Override
    protected @NotNull String errorToJson(@NotNull Throwable e) {
        return this.gson.toJson(e);
    }
}