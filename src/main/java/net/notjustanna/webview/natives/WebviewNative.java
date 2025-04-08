package net.notjustanna.webview.natives;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This interface is used to load the native library for Webview.
 * @author e3ndr, NotJustAnna
 */
/*
 * DEVELOPER NOTE:
 * TODO Sync this with the C header file.
 */
public interface WebviewNative extends Library {
    WebviewNative INSTANCE = init();

    /**
     * Width and height are default size.
      */
    int HINT_NONE = 0;
    /**
     * Width and height are minimum bounds.
     */
    int HINT_MIN = 1;
    /**
     * Width and height are maximum bounds.
     */
    int HINT_MAX = 2;
    /**
     * Width and height are fixed size.
     */
    int HINT_FIXED = 3;

    /**
     * Missing dependency.
     */
    int ERROR_MISSING_DEPENDENCY = -5;
    /**
     * Operation canceled.
     */
    int ERROR_CANCELED = -4;
    /**
     * Invalid state detected.
     */
    int ERROR_INVALID_STATE = -3;
    /**
     * One or more invalid arguments have been specified e.g. in a function call.
     */
    int ERROR_INVALID_ARGUMENT = -2;
    /**
     * An unspecified error occurred. A more specific error code may be needed.
     */
    int ERROR_UNSPECIFIED = -1;
    /**
     * OK/Success. Functions that return error codes will typically return this
     * to signify successful operations.
     */
    int ERROR_OK = 0;
    /**
     * Signifies that something already exists.
     */
    int ERROR_DUPLICATE = 1;
    /**
     * Signifies that something does not exist.
     */
    int ERROR_NOT_FOUND = 2;

    /**
     * Creates a new webview instance.
     *
     * @param debug Enable developer tools if supported by the backend.
     * @param window Optional native window handle, i.e.  GtkWindow pointer
     *         NSWindow pointer (Cocoa) or  HWND (Win32). If non-null,
     *        the webview widget is embedded into the given window, and the
     *        caller is expected to assume responsibility for the window as
     *        well as application lifecycle. If the window handle is null,
     *        a new window is created and both the window and application
     *        lifecycle are managed by the webview instance.
     * @apiNote Win32: The function also accepts a pointer to  HWND (Win32) in the
     *         window parameter for backward compatibility.
     * @apiNote Win32/WebView2:  CoInitializeEx should be called with
     *          COINIT_APARTMENTTHREADED before attempting to call this function
     *         with an existing window. Omitting this step may cause WebView2
     *         initialization to fail.
     * @return NULL on failure. Creation can fail for various reasons such
     *         as when required runtime dependencies are missing or when window
     *         creation fails. WEBVIEW_ERROR_MISSING_DEPENDENCY
     *         May be returned if WebView2 is unavailable on Windows.
     */
    // WEBVIEW_API webview_t webview_create(int debug, void *window);
    long webview_create(boolean debug, PointerByReference window);

    /**
     * @return            a native window handle pointer.
     *
     * @param    $pointer The instance pointer of the webview
     *
     * @implNote          This is either a pointer to a GtkWindow, NSWindow, or
     *                    HWND.
     */
    long webview_get_window(long $pointer);

    /**
     * Load raw HTML content onto the window.
     *
     * @param $pointer The instance pointer of the webview
     * @param html     The raw HTML string.
     */
    void webview_set_html(long $pointer, String html);

    /**
     * Navigates to the given URL.
     *
     * @param $pointer The instance pointer of the webview
     * @param url      The target url, can be a data uri.
     */
    void webview_navigate(long $pointer, String url);

    /**
     * Sets the title of the webview window.
     *
     * @param $pointer The instance pointer of the webview
     * @param title
     */
    void webview_set_title(long $pointer, String title);

    /**
     * Updates the webview's window size, see {@link WebviewNative#HINT_NONE},
     * {@link WebviewNative#HINT_MIN}, {@link WebviewNative#HINT_MAX}, and {@link WebviewNative#HINT_FIXED}
     *
     * @param $pointer The instance pointer of the webview
     * @param width
     * @param height
     * @param hint
     */
    void webview_set_size(long $pointer, int width, int height, int hint);

    /**
     * Runs the main loop until it's terminated. You must destroy the webview after
     * this method returns.
     *
     * @param $pointer The instance pointer of the webview
     */
    void webview_run(long $pointer);

    /**
     * Destroys a webview and closes the native window.
     *
     * @param $pointer The instance pointer of the webview
     */
    void webview_destroy(long $pointer);

    /**
     * Stops the webview loop, which causes {@link WebviewNative##webview_run(long)} to return.
     *
     * @param $pointer The instance pointer of the webview
     */
    void webview_terminate(long $pointer);

    /**
     * Evaluates arbitrary JavaScript code asynchronously.
     *
     * @param $pointer The instance pointer of the webview
     * @param js       The script to execute
     */
    void webview_eval(long $pointer, @NotNull String js);

    /**
     * Injects JavaScript code at the initialization of the new page.
     *
     * @implSpec          It is guaranteed to be called before window.onload.
     *
     * @param    $pointer The instance pointer of the webview
     * @param    js       The script to execute
     */
    void webview_init(long $pointer, @NotNull String js);

    /**
     * Binds a native callback so that it will appear under the given name as a
     * global JavaScript function. Internally it uses webview_init().
     *
     * @param $pointer The instance pointer of the webview
     * @param name     The name of the function to be exposed in Javascript
     * @param callback The callback to be called
     * @param arg      Unused
     */
    void webview_bind(long $pointer, @NotNull String name, @NotNull WebviewNative.NativeBindCallback callback, long arg);

    /**
     * Remove the native callback specified.
     *
     * @param $pointer The instance pointer of the webview
     * @param name     The name of the callback
     */
    void webview_unbind(long $pointer, @NotNull String name);

    /**
     * Allows to return a value from the native binding. Original request pointer
     * must be provided to help internal RPC engine match requests with responses.
     *
     * @param $pointer The instance pointer of the webview
     * @param seq      TODO FIXME
     * @param isError  Whether `result` should be thrown as an exception
     * @param result   The result (in json)
     */
    void webview_return(long $pointer, long seq, boolean isError, String result);

    /**
     * Dispatches the callback on the UI thread, only effective while
     * {@link WebviewNative##webview_run(long)} is blocking.
     *
     * @param $pointer The instance pointer of the webview
     * @param callback The callback to be called
     * @param arg      Unused
     */
    void webview_dispatch(long $pointer, @NotNull WebviewNative.NativeDispatchCallback callback, long arg);

    /**
     * Used in {@link WebviewNative#webview_bind}
     */
    @FunctionalInterface
    interface NativeBindCallback extends Callback {
        /**
         * @param seq The request id, used in {@link WebviewNative#webview_return}
         * @param req The javascript arguments converted to a json array (string)
         * @param arg Unused
         */
        void callback(long seq, String req, long arg);
    }

    /**
     * Used in {@link WebviewNative#webview_dispatch}
     */
    @FunctionalInterface
    interface NativeDispatchCallback extends Callback {
        /**
         * @param $pointer The pointer of the webview
         * @param arg      Unused
         */
        void callback(long $pointer, long arg);
    }

    /**
     * Returns the version info.
     */
    VersionInfoStruct webview_version();

    class VersionInfoStruct extends Structure {
        /*
         * Developer note:
         * Technically, `major`, `minor`, and `patch` are in a sub-struct,
         * but due to how C structs work (they are inlined), we can inline
         * fields without having to create a new struct.
         *
         * Also, the `version_number`, `pre_release`, and `build_metadata`
         * are strings. They are parsed by WebviewInfo.
         */

        public int major;
        public int minor;
        public int patch;
        public byte[] version_number = new byte[32];
        public byte[] pre_release = new byte[48];
        public byte[] build_metadata = new byte[48];

        @Override
        protected List<String> getFieldOrder() {
            return List.of("major", "minor", "patch", "version_number", "pre_release", "build_metadata");
        }
    }

    private static WebviewNative init() {
        String libName = PlatformSpecific.current.getBinaryName();
        String packageName = PlatformSpecific.current.getPackageName();

        Class<WebviewNative> cls = WebviewNative.class;
        String lib = "/" + cls.getPackage().getName().replace('.', '/') + "/" + packageName + "/" + libName;

        try (InputStream in = cls.getResourceAsStream(lib)) {
            Objects.requireNonNull(in, "Native library not found. It might be a missing dependency.");
            Files.copy(in, Path.of(libName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library: " + libName, e);
        }

        System.load(new File(libName).getAbsolutePath());
        System.setProperty("jna.library.path", ".");

        return Native.load("webview", cls, Map.of(Library.OPTION_STRING_ENCODING, "UTF-8"));
    }
}
