package net.notjustanna.webview.natives;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import net.notjustanna.webview.WebviewCore;

/**
 * Helper class for Windows-specific functionality.
 * <p>
 * This class is internal to the library! Class structure and methods may change at any release without notice.
 *
 * @author Alex Bowles, Anna Silva, isinvon
 */
public class WinHelper {
    private static WinDef.HWND hwndOf(WebviewCore webview) {
        return new WinDef.HWND(WebviewNative.INSTANCE.webview_get_window(WebviewCore.nativePointer(webview)));
    }

    public static void setWindowAppearance(WebviewCore webview, boolean shouldBeDark) {
        // References:
        // https://docs.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute
        // https://winscp.net/forum/viewtopic.php?t=30088
        // https://gist.github.com/rossy/ebd83ba8f22339ce25ef68bfc007dfd2
        //
        // This is the code that we're mimicking (in c):
        /*
        DwmSetWindowAttribute(
            hwnd,
            DWMWA_USE_IMMERSIVE_DARK_MODE,
            &(BOOL) { TRUE },
            sizeof(BOOL)
        );
        InvalidateRect(hwnd, null, FALSE);
        */

        WinDef.HWND hwnd = WinHelper.hwndOf(webview);
        WinDef.BOOLByReference pvAttribute = new WinDef.BOOLByReference(new WinDef.BOOL(shouldBeDark));

        if (Dwmapi.INSTANCE.DwmSetWindowAttribute(
            hwnd,
            Dwmapi.DWMWA_USE_IMMERSIVE_DARK_MODE,
            pvAttribute,
            WinDef.BOOL.SIZE
        ) != 0) {
            throw new RuntimeException("Failed to set window attribute");
        }

        if (Dwmapi.INSTANCE.DwmSetWindowAttribute(
            hwnd,
            Dwmapi.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1,
            pvAttribute,
            WinDef.BOOL.SIZE
        ) != 0) {
            throw new RuntimeException("Failed to set window attribute");
        }

        if (!User32.INSTANCE.InvalidateRect(hwnd, null, false)) {
            throw new RuntimeException("Failed to set window attribute");
        }
    }

    public static void bringToFront(WebviewCore webview) {
        WinDef.HWND hwnd = WinHelper.hwndOf(webview);
        if (!User32.INSTANCE.SetForegroundWindow(hwnd)) {
            throw new RuntimeException("Failed to bring window to front");
        }
    }

    public static void alwaysOnTop(WebviewCore webview) {
        WinDef.HWND hwnd = WinHelper.hwndOf(webview);
        WinDef.HWND topmost = new WinDef.HWND(new Pointer(-1));

        // ... why? no idea.
        for (int i = 0; i < 2; i++) {
            if (!User32.INSTANCE.SetWindowPos(
                hwnd,
                topmost,
                0, 0, 0, 0,
                User32.SWP_NOMOVE | User32.SWP_NOSIZE
            )) {
                throw new RuntimeException("Failed to set window position");
            }
        }
    }

    public static void fullscreenWindow(WebviewCore webview) {
        WinDef.HWND hwnd = WinHelper.hwndOf(webview);
        // Get the screen dimensions
        WinDef.RECT rect = new WinDef.RECT();
        if (!User32.INSTANCE.GetWindowRect(hwnd, rect)) {
            throw new RuntimeException("Failed to full screen window");
        }

        // Get screen width and height
        int screenWidth = User32.INSTANCE.GetSystemMetrics(User32.SM_CXSCREEN);
        int screenHeight = User32.INSTANCE.GetSystemMetrics(User32.SM_CYSCREEN);

        // Set the window to cover the entire screen
        if (User32.INSTANCE.SetWindowLong(hwnd, User32.GWL_STYLE, User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_STYLE) & ~User32.WS_OVERLAPPEDWINDOW) == 0) {
            throw new RuntimeException("Failed to full screen window");
        }
        if (!User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, screenWidth, screenHeight, User32.SWP_NOZORDER | User32.SWP_SHOWWINDOW)) {
            throw new RuntimeException("Failed to full screen window");
        }
    }

    public static void maximizeWindow(WebviewCore webview) {
        WinDef.HWND hwnd = WinHelper.hwndOf(webview);
        // Maximize the window
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_MAXIMIZE);
    }

    interface Dwmapi extends Library {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

        int DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19;
        int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, PointerType pvAttribute, int cbAttribute);
    }
}