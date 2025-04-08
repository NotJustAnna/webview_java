package net.notjustanna.webview.natives;

import com.sun.jna.*;
import org.jetbrains.annotations.Nullable;

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
 *
 * @author Alex Bowles, Anna Silva
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
     * Top-level window. GtkWindow pointer (GTK), NSWindow pointer (Cocoa) or HWND (Win32).
     */
    int NATIVE_HANDLE_KIND_UI_WINDOW = 0;
    /**
     * Browser widget. GtkWidget pointer (GTK), NSView pointer (Cocoa) or HWND (Win32).
     */
    int NATIVE_HANDLE_KIND_UI_WIDGET = 1;
    /**
     * Browser controller. WebKitWebView pointer (WebKitGTK), WKWebView pointer (Cocoa/WebKit) or
     * ICoreWebView2Controller pointer (Win32/WebView2).
     */
    int NATIVE_HANDLE_KIND_BROWSER_CONTROLLER = 2;

    /**
     * Creates a new webview instance.
     * <p>
     * Remarks:
     * <p>
     * - Win32: The function also accepts a pointer to HWND (Win32) in the
     * window parameter for backward compatibility.
     * <p>
     * - Win32/WebView2: CoInitializeEx should be called with
     * COINIT_APARTMENTTHREADED before attempting to call this function
     * with an existing window. Omitting this step may cause WebView2
     * initialization to fail.
     *
     * @param debug  Enable developer tools if supported by the backend.
     * @param window Optional native window handle, i.e. GtkWindow pointer
     *               NSWindow pointer (Cocoa) or HWND (Win32). If non-null,
     *               the webview widget is embedded into the given window, and the
     *               caller is expected to assume responsibility for the window as
     *               well as application lifecycle. If the window handle is null,
     *               a new window is created and both the window and application
     *               lifecycle are managed by the webview instance.
     * @return NULL on failure. Creation can fail for various reasons such
     * as when required runtime dependencies are missing or when window
     * creation fails. WEBVIEW_ERROR_MISSING_DEPENDENCY
     * may be returned if WebView2 is unavailable on Windows.
     * @implNote You shouldn't worry about the remarks regarding Win32/WebView2 unless
     * you are doing more stuff with JNA.
     */
    // WEBVIEW_API webview_t webview_create(int debug, void *window);
    Pointer webview_create(boolean debug, @Nullable Pointer window);


    /**
     * Destroys a webview instance and closes the native window.
     *
     * @param w The webview instance.
     */
    // WEBVIEW_API webview_error_t webview_destroy(webview_t w);
    int webview_destroy(Pointer w);

    /**
     * Runs the main loop until it's terminated.
     *
     * @param w The webview instance.
     */
    // WEBVIEW_API webview_error_t webview_run(webview_t w);
    int webview_run(Pointer w);

    /**
     * Stops the main loop. It is safe to call this function from another other
     * background thread.
     *
     * @param w The webview instance.
     */
    // WEBVIEW_API webview_error_t webview_terminate(webview_t w);
    int webview_terminate(Pointer w);

    /**
     * Schedules a function to be invoked on the thread with the run/event loop.
     * <p>
     * Since library functions generally do not have thread safety guarantees,
     * this function can be used to schedule code to execute on the main/GUI
     * thread and thereby make that execution safe in multi-threaded applications.
     *
     * @param w   The webview instance.
     * @param fn  The function to be invoked.
     * @param arg An optional argument passed along to the callback function.
     */
    // WEBVIEW_API webview_error_t webview_dispatch(webview_t w, void (*fn)(webview_t w, void *arg), void *arg);
    int webview_dispatch(Pointer w, DispatchCallback fn, Pointer arg);

    /**
     * Function to be invoked on the thread with the run/event loop.
     */
    @FunctionalInterface
    interface DispatchCallback extends Callback {
        /**
         * @param w   The webview instance.
         * @param arg An optional argument passed by the dispatch function.
         */
        void callback(Pointer w, Pointer arg);
    }

    /**
     * Returns the native handle of the window associated with the webview instance.
     * The handle can be a GtkWindow pointer (GTK), NSWindow pointer (Cocoa)
     * or HWND (Win32).
     *
     * @param w The webview instance.
     * @return The handle of the native window.
     */
    // WEBVIEW_API void *webview_get_window(webview_t w);
    Pointer webview_get_window(Pointer w);

    /**
     * Get a native handle of choice.
     *
     * @param w    The webview instance.
     * @param kind The kind of handle to retrieve.
     * @return The native handle or NULL.
     * @since 0.11
     */
    // WEBVIEW_API void *webview_get_native_handle(webview_t w, webview_native_handle_kind_t kind);
    Pointer webview_get_context(Pointer w, int kind);

    /**
     * Updates the title of the native window.
     *
     * @param w     The webview instance.
     * @param title The new title.
     * @return {@link WebviewNative#ERROR_OK} on success, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_set_title(webview_t w, const char *title);
    int webview_set_title(Pointer w, String title);

    /**
     * Updates the size of the native window.
     * <p>
     * Remarks:
     * <p> - Using WEBVIEW_HINT_MAX for setting the maximum window size is not
     * supported with GTK 4 because X11-specific functions such as
     * gtk_window_set_geometry_hints were removed. This option has no effect
     * when using GTK 4.
     * <p> - GTK 4 can set a default/initial window size if done early enough;
     * otherwise, this function has no effect. GTK 4 (unlike 3) can't resize
     * a window after it has been set up.
     *
     * @param w      The webview instance.
     * @param width  New width.
     * @param height New height.
     * @param hints  Size hints.
     * @return {@link WebviewNative#ERROR_OK} on success, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_set_size(webview_t w, int width, int height, webview_hint_t hints);
    int webview_set_size(Pointer w, int width, int height, int hints);

    /**
     * Navigates webview to the given URL. URL may be a properly encoded data URI.
     * <p>
     * Valid URLs include, but are not limited to: <code>https://github.com/webview/webview</code>,
     * <code>data:text/html,%3Ch1%3EHello%3C%2Fh1%3E</code>,
     * <code>data:text/html;base64,PGgxPkhlbGxvPC9oMT4=</code>
     *
     * @param w   The webview instance.
     * @param url URL.
     * @return {@link WebviewNative#ERROR_OK} on success, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_navigate(webview_t w, const char *url);
    int webview_navigate(Pointer w, String url);

    /**
     * Load HTML content into the webview.
     *
     * @param w    The webview instance.
     * @param html HTML content.
     * @return {@link WebviewNative#ERROR_OK} on success, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_set_html(webview_t w, const char *html);
    int webview_set_html(Pointer w, String html);

    /**
     * Injects JavaScript code to be executed immediately upon loading a page.
     * The code will be executed before window.onload.
     *
     * @param w  The webview instance.
     * @param js JS content.
     * @return {@link WebviewNative#ERROR_OK} on success, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_init(webview_t w, const char *js);
    int webview_init(Pointer w, String js);

    /**
     * Evaluates arbitrary JavaScript code.
     * <p>
     * Use bindings if you need to communicate the result of the evaluation.
     *
     * @param w  The webview instance.
     * @param js JS content.
     * @return {@link WebviewNative#ERROR_OK} on success, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_eval(webview_t w, const char *js);
    int webview_eval(Pointer w, String js);


    /**
     * Binds a function pointer to a new global JavaScript function.
     * <p>
     * Internally, JS glue code is injected to create the JS function by the
     * given name. The callback function is passed a request identifier,
     * a request string and a user-provided argument. The request string is
     * a JSON array of the arguments passed to the JS function.
     *
     * @param w    The webview instance.
     * @param name Name of the JS function.
     * @param fn   Callback function.
     * @param arg  User argument.
     * @return {@link WebviewNative#ERROR_OK} on success, WEBVIEW_ERROR_DUPLICATE, if a binding already exists with the specified name, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_bind(webview_t w, const char *name, void (*fn)(const char *id, const char *req, void *arg), void *arg);
    int webview_bind(Pointer w, String name, BindCallback fn, Pointer arg);

    @FunctionalInterface
    interface BindCallback extends Callback {
        /**
         * @param id  The identifier of the binding call.
         * @param req A JSON array of the arguments passed to the JS function.
         * @param arg User argument.
         */
        void callback(String id, String req, Pointer arg);
    }

    /**
     * Removes a binding created with webview_bind().
     *
     * @param w    The webview instance.
     * @param name Name of the binding.
     * @return {@link WebviewNative#ERROR_OK} on success, EBVIEW_ERROR_NOT_FOUND, if no binding exists with the specified name, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_unbind(webview_t w, const char *name);
    int webview_unbind(Pointer w, String name);

    /**
     * Responds to a binding call from the JS side.
     * <p>
     * This function is safe to call from another thread.
     *
     * @param w      The webview instance.
     * @param id     The identifier of the binding call. Pass along the value received
     *               in the binding handler (see webview_bind()).
     * @param status A status of zero tells the JS side that the binding call was
     *               successful; any other value indicates an error.
     * @param result The result of the binding call to be returned to the JS side.
     *               This must either be a valid JSON value or an empty string for
     *               the primitive JS value undefined.
     * @return {@link WebviewNative#ERROR_OK} on success, other error codes on failure.
     */
    // WEBVIEW_API webview_error_t webview_return(webview_t w, const char *id, int status, const char *result);
    int webview_return(Pointer w, String id, int status, String result);

    /**
     * Returns the version info.
     */
    VersionInfoStruct webview_version();

    /**
     * Holds the library's version information.
     */
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

    /**
     * Converts a C-style string to a Java String.
     * @param arr The byte array to convert.
     * @return The converted string.
     */
    static String cString(byte[] arr) {
        int len;
        for (len = 0; len < arr.length; len++) {
            if (arr[len] == 0) {
                break;
            }
        }
        return new String(arr, 0, len);
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
