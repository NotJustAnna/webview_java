package net.notjustanna.webview;

import com.sun.jna.Pointer;
import lombok.Setter;
import lombok.extern.java.Log;
import net.notjustanna.webview.natives.WebviewNative;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A dispatcher for managing tasks on the webview thread.
 * <p>
 * This class implements the {@link Executor} interface and provides functionality
 * for dispatching tasks to the webview thread. It maintains a set of active
 * dispatch references and supports error handling through a customizable
 * {@link Consumer}.
 *
 * @author Anna Silva
 */
@Log
public class WebviewDispatcher implements Executor {
    /**
     * A thread-safe set of active dispatch references.
     * <p>
     * This set is used to track tasks that are currently being dispatched
     * to the webview thread.
     */
    private final Set<Object> dispatchRefs = ConcurrentHashMap.newKeySet();

    /**
     * The error handler for uncaught exceptions.
     * <p>
     * This {@link Consumer} is invoked when an uncaught exception occurs
     * during task execution. By default, it logs the exception as a warning.
     */
    @Setter
    private Consumer<Throwable> errorHandler = DEFAULT_ERROR_HANDLER;

    /**
     * The pointer to the webview instance.
     */
    private final Pointer $webview_t;

    /**
     * Weak reference to the thread that created the webview instance.
     */
    private final WeakReference<Thread> threadRef;

    /**
     * Constructs a new {@code WebviewDispatcher} with the specified webview pointer.
     * <p>
     * This constructor initializes the dispatcher with a reference to the native
     * webview instance, represented by a {@link Pointer}.
     *
     * @param $webview_t the {@link Pointer} to the native webview instance.
     * @param threadRef
     */
    WebviewDispatcher(Pointer $webview_t, WeakReference<Thread> threadRef) {
        this.$webview_t = $webview_t;
        this.threadRef = threadRef;
    }

    /**
     * Dispatches a runnable to the webview thread.
     *
     * @implNote Be very mindful of the fact that this will block the webview thread.
     * @param runnable callback to be executed on the webview thread.
     * @return A `CompletableFuture` that completes when the runnable has been executed.
     */
    public CompletableFuture<Void> runAsync(@NotNull Runnable runnable) {
        return CompletableFuture.runAsync(runnable, this);
    }

    /**
     * Dispatches a supplier to the webview thread.
     *
     * @implNote Be very mindful of the fact that this will block the webview thread.
     * @param supplier callback to be executed on the webview thread.
     * @return A `CompletableFuture` that completes with the result of the supplier.
     */
    public <U> CompletableFuture<U> supplyAsync(@NotNull Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, this);
    }

    /**
     * Wraps the execution of a runnable with error handling.
     * <p>
     * This method ensures that any exceptions thrown during the execution of the
     * provided {@link Runnable} are caught and passed to the default error handler.
     *
     * @param runnable the {@link Runnable} to be executed.
     */
    void wrapExec(Runnable runnable) {
        this.execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                DEFAULT_ERROR_HANDLER.accept(e);
            }
        });
    }

    /**
     * Executes a native operation on the webview thread.
     * <p>
     * This method wraps the execution of an {@link IntSupplier} that performs
     * a native operation, ensuring proper error handling.
     *
     * @param supplier the {@link IntSupplier} representing the native operation to execute.
     */
    void execNative(IntSupplier supplier) {
        this.wrapExec(() -> WebviewCore.handleError(supplier.getAsInt()));
    }

    /**
     * Executes a command on the webview thread.
     * <p>
     * This method creates a {@link WebviewNative.DispatchCallback} for the provided
     * {@link Runnable} command, adds it to the set of active dispatch references,
     * and dispatches it to the webview thread.
     *
     * @param command the {@link Runnable} to be executed on the webview thread.
     */
    @Override
    public void execute(@NotNull Runnable command) {
        if (threadRef.refersTo(Thread.currentThread())) {
            try {
                command.run();
            } catch (Exception e) {
                log.log(Level.SEVERE, ERROR_DISPATCH_RUNNABLE, e);
            }
            return;
        }

        WebviewNative.DispatchCallback d = new DispatchRunnable(command);
        dispatchRefs.add(d);
        WebviewCore.handleError(WebviewNative.INSTANCE.webview_dispatch($webview_t, d, null));
    }

    /**
     * A callback implementation for dispatching tasks to the webview thread.
     * <p>
     * This class implements the {@link WebviewNative.DispatchCallback} interface
     * and is responsible for executing a given {@link Runnable} command on the
     * webview thread. It ensures proper error handling and cleanup after execution.
     */
    private class DispatchRunnable implements WebviewNative.DispatchCallback {
        /**
         * The command to be executed on the webview thread.
         */
        private final Runnable command;

        /**
         * Constructs a new {@code DispatchRunnable} with the specified command.
         *
         * @param command the {@link Runnable} to be executed on the webview thread.
         */
        public DispatchRunnable(Runnable command) {
            this.command = command;
        }

        /**
         * Executes the command on the webview thread.
         * <p>
         * This method is called by the webview native layer. It runs the command
         * and logs any exceptions that occur during execution. After execution,
         * the callback is removed from the set of active dispatch references.
         *
         * @param w   a pointer to the webview instance (unused in this implementation).
         * @param arg a pointer to additional arguments (unused in this implementation).
         */
        @Override
        public void callback(Pointer w, Pointer arg) {
            try {
                command.run();
            } catch (Exception e) {
                log.log(Level.SEVERE, ERROR_DISPATCH_RUNNABLE, e);
            }
            dispatchRefs.remove(this);
        }
    }

    private static final String ERROR_DISPATCH_RUNNABLE = "Error happened while executing dispatch runnable.";

    private static final String WARN_UNCAUGHT_EXCEPTION = "Uncaught exception in webview thread. " +
        "Use webview.setErrorHandler() to set a custom error handler.";

    private static final Consumer<Throwable> DEFAULT_ERROR_HANDLER = throwable -> log.log(Level.WARNING, WARN_UNCAUGHT_EXCEPTION, throwable);
}
