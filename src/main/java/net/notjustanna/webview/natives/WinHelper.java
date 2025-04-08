package net.notjustanna.webview.natives;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.BOOLByReference;
import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * Helper class for Wincows-specific functionality.
 *
 * @author Alex Bowles, Anna Silva, isinvon
 */
public class WinHelper {
    public static void setWindowAppearance(Pointer nativeWindowPointer, boolean shouldBeDark) {
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

        HWND hwnd = new HWND(nativeWindowPointer);
        BOOLByReference pvAttribute = new BOOLByReference(new BOOL(shouldBeDark));

        if (Dwmapi.INSTANCE.DwmSetWindowAttribute(
            hwnd,
            Dwmapi.DWMWA_USE_IMMERSIVE_DARK_MODE,
            pvAttribute,
            BOOL.SIZE
        ) != 0) {
            throw new RuntimeException("Failed to set window attribute");
        }

        if (Dwmapi.INSTANCE.DwmSetWindowAttribute(
            hwnd,
            Dwmapi.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1,
            pvAttribute,
            BOOL.SIZE
        ) != 0) {
            throw new RuntimeException("Failed to set window attribute");
        }

        if (!User32.INSTANCE.InvalidateRect(hwnd, null, false)) {
            throw new RuntimeException("Failed to set window attribute");
        }
    }

    public static void bringToFront(Pointer nativeWindowPointer) {
        HWND HWND_TOPMOST = new HWND(new Pointer(-1));
        HWND hwnd = new HWND(nativeWindowPointer);

        // ... why? no idea.
        for (int i = 0; i < 2; i++) {
            if (!User32.INSTANCE.SetWindowPos(
                hwnd,
                HWND_TOPMOST,
                0, 0, 0, 0,
                User32.SWP_NOMOVE | User32.SWP_NOSIZE
            )) {
                throw new RuntimeException("Failed to set window position");
            }
        }
    }

    interface Dwmapi extends Library {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

        int DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19;
        int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

        @SuppressWarnings("UnusedReturnValue")
        int DwmSetWindowAttribute(HWND hwnd, int dwAttribute, PointerType pvAttribute, int cbAttribute);
    }

}