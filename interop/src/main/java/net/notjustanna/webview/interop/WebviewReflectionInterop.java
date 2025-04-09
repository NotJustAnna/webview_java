package net.notjustanna.webview.interop;

import net.notjustanna.webview.WebviewCore;
import net.notjustanna.webview.interop.callback.WebviewInteropFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * An abstract class extending {@link WebviewInterop} to provide reflection-based
 * binding of Java methods to JavaScript functions in a WebView.
 *
 * @param <Self> The type of the subclass extending this class.
 * @param <Fn>   The type of the callback function.
 */
public abstract class WebviewReflectionInterop<Self extends WebviewReflectionInterop<Self, Fn>, Fn> extends WebviewInterop<Self, Fn> {
    /**
     * A constant representing an empty array of arguments.
     */
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    /**
     * Constructs a new {@code WebviewReflectionInterop} instance.
     *
     * @param webview The WebView core instance.
     */
    public WebviewReflectionInterop(WebviewCore webview) {
        super(webview);
    }

    /**
     * Binds a Java method to a new global JavaScript function.
     *
     * @param name   The name of the JavaScript function.
     * @param obj    The object or class containing the method.
     * @param method The name of the method to bind.
     * @return The current instance of the class (for chaining).
     * @throws IllegalArgumentException If no method or multiple methods with the given name are found.
     */
    public Self bindMethod(@NotNull String name, @NotNull Object obj, @NotNull String method) {
        webview.bind(name, this.wrapNative(this.wrapMethod(obj, method)));
        return castThis();
    }

    /**
     * Handles the result of a method invocation and converts it to a string.
     *
     * @param res The result of the method invocation.
     * @return A string representation of the result, or {@code null} if the result is null.
     * @throws Exception If an error occurs during result handling.
     */
    protected abstract @Nullable String handleResult(Object res) throws Exception;

    /**
     * Processes the request string and converts it into an array of arguments
     * matching the parameter types of the method.
     *
     * @param req    The request string containing the arguments.
     * @param params The parameter types of the method.
     * @return An array of arguments matching the parameter types.
     * @throws Exception If an error occurs during argument processing.
     */
    @NotNull
    protected abstract Object[] handleArgs(@NotNull String req, @NotNull Class<?>[] params) throws Exception;

    /**
     * Wraps a Java method into a {@link WebviewInteropFunction}.
     *
     * @param obj    The object or class containing the method.
     * @param method The name of the method to wrap.
     * @return A wrapped {@link WebviewInteropFunction}.
     * @throws IllegalArgumentException If no method or multiple methods with the given name are found.
     */
    private @NotNull WebviewInteropFunction wrapMethod(@NotNull Object obj, @NotNull String method) {
        boolean isClass = obj instanceof Class;
        Class<?> cls = isClass ? (Class<?>) obj : obj.getClass();
        List<Method> methods = Arrays.stream(cls.getMethods()).filter(m -> m.getName().equals(method)).toList();

        if (methods.isEmpty()) {
            throw new IllegalArgumentException("No method found with name " + method);
        }

        if (methods.size() > 1) {
            throw new IllegalArgumentException("Multiple methods found with name " + method);
        }

        Method m = methods.get(0);
        Class<?>[] params = m.getParameterTypes();
        return req -> {
            Object[] args = EMPTY_ARGUMENTS;
            if (params.length != 0) {
                args = handleArgs(req, params);
            }

            Object res = m.invoke(obj, args);

            return handleResult(res);
        };
    }
}