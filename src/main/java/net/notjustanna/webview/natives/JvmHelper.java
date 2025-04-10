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
        return Thread.currentThread().getId() == 1;
    }

    /**
     * Checks if the current thread is a virtual thread.
     * <p>
     * This method always returns false as virtual threads are JVM 21+ specific.
     *
     * @return {@code false} indicating the current thread is not virtual.
     */
    public static boolean isCurrentThreadVirtual() {
        return false;
    }
}