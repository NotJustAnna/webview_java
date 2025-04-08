package net.notjustanna.webview.natives;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.BOOLByReference;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.PointerByReference;

import static net.notjustanna.webview.natives.WinHelper.Dwmapi.dwmapi;
import static net.notjustanna.webview.natives.WinHelper.User32.user32;

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

        dwmapi.DwmSetWindowAttribute(
            hwnd,
            Dwmapi.DWMWA_USE_IMMERSIVE_DARK_MODE,
            pvAttribute,
            BOOL.SIZE
        );

        dwmapi.DwmSetWindowAttribute(
            hwnd,
            Dwmapi.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1,
            pvAttribute,
            BOOL.SIZE
        );

        user32.InvalidateRect(hwnd, null, 0); // Repaint
    }


    interface Dwmapi extends Library {
        static final Dwmapi dwmapi = Native.load("dwmapi", Dwmapi.class);

        int DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19;
        int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

        @SuppressWarnings("UnusedReturnValue")
        int DwmSetWindowAttribute(HWND hwnd, int dwAttribute, PointerType pvAttribute, int cbAttribute);
    }

    interface User32 extends Library {
        static final User32 user32 = Native.load("user32", User32.class);

        @SuppressWarnings("UnusedReturnValue")
        int InvalidateRect(HWND hwnd, PointerByReference rect, int erase);
    }
}