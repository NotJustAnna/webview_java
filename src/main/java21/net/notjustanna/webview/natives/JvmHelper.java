package net.notjustanna.webview.natives;

/**
 * Helper class for JVM specific operations.
 * <p>
 * This class is internal to the library! Class structure and methods may change at any release without notice.
 *
 * <p>
 * Provides utility methods for JVM-related tasks.
 *
 * @author Anna Silva
 */
public class JvmHelper {

    /**
     * Checks if the current thread is the main thread.
     * <p>
     * This method determines if the current thread has an ID of 1, which is typically
     * associated with the main thread in JVM-based applications.
     *
     * @return {@code true} if the current thread is the main thread, {@code false} otherwise.
     */
    public static boolean isMainThread() {
        return Thread.currentThread().threadId() == 1;
    }

    /**
     * Checks if the current thread is a virtual thread.
     * <p>
     * This method uses the {@link Thread#isVirtual()} API to determine if the current thread
     * is a virtual thread.
     *
     * @return {@code true} if the current thread is a virtual thread, {@code false} otherwise.
     */
    public static boolean isCurrentThreadVirtual() {
        return Thread.currentThread().isVirtual();
    }
}