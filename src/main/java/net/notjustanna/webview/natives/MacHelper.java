package net.notjustanna.webview.natives;

import com.sun.jna.platform.mac.SystemB;

/**
 * Helper class for Mac-specific functionality.
 * <p>
 * This class is internal to the library! Class structure and methods may change at any release without notice.
 *
 * @author Anna Silva
 */
public class MacHelper {
    public static boolean startedOnFirstThread() {
        // https://github.com/crykn/guacamole/blob/v0.3.5/gdx-desktop/src/main/java/de/damios/guacamole/gdx/StartOnFirstThreadHelper.java
        return "1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + SystemB.INSTANCE.getpid()));
    }
}
