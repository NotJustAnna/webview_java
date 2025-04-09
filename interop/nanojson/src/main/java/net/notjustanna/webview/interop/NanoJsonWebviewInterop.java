package net.notjustanna.webview.interop;

import com.grack.nanojson.*;
import net.notjustanna.webview.WebviewCore;
import net.notjustanna.webview.interop.callback.NanoJsonWebviewInteropFunction;
import net.notjustanna.webview.interop.callback.WebviewInteropFunction;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A class that provides interop functionality for handling JavaScript requests
 * in a WebView using the NanoJson library. It extends the generic WebviewInterop
 * class and specializes in handling callbacks with NanoJson.
 */
public class NanoJsonWebviewInterop extends WebviewInterop<NanoJsonWebviewInterop, NanoJsonWebviewInteropFunction> {

    /**
     * Constructs a new NanoJsonWebviewInterop instance.
     *
     * @param webview The WebviewCore instance to associate with this interop.
     */
    public NanoJsonWebviewInterop(WebviewCore webview) {
        super(webview);
    }

    /**
     * Wraps a NanoJsonWebviewInteropFunction into a WebviewInteropFunction.
     *
     * @param callback The NanoJsonWebviewInteropFunction to wrap.
     * @return A WebviewInteropFunction that processes JSON requests and responses.
     */
    @Override
    protected @NotNull WebviewInteropFunction wrap(@NotNull NanoJsonWebviewInteropFunction callback) {
        return req -> {
            JsonArray args = JsonParser.array().from(req);
            Object res = callback.apply(args);
            return JsonWriter.string(res);
        };
    }

    /**
     * Converts a Throwable into a JSON string representation of the error.
     *
     * @param e The Throwable to convert.
     * @return A JSON string containing error details such as the message, type, and stack trace.
     */
    @Override
    protected @NotNull String errorToJson(@NotNull Throwable e) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
        }
        return JsonWriter.string()
            .object()
            .value("error", sw.toString())
            .value("message", e.getMessage())
            .value("type", e.getClass().getName())
            .value("stacktrace", sw.toString())
            .end()
            .done();
    }
}