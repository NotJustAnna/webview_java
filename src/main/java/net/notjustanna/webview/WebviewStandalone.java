package net.notjustanna.webview;

import com.sun.jna.Pointer;
import lombok.Getter;
import net.notjustanna.webview.natives.PlatformSpecific;
import net.notjustanna.webview.natives.WebviewNative;
import net.notjustanna.webview.natives.WinHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

@Getter
public class WebviewStandalone implements Closeable, Runnable {
    @NotNull
    private final WebviewCore webview;

    public WebviewStandalone(Boolean debug) {
        this.webview = WebviewCore.newStandalone(debug);
    }

    public WebviewStandalone setInitScript(@NotNull String script) {
        webview.setInitScript(script);
        return this;
    }

    public WebviewStandalone setHtml(@Nullable String html) {
        webview.setHtml(html);
        return this;
    }

    public WebviewStandalone navigate(@Nullable String url) {
        webview.navigate(url);
        return this;
    }

    public WebviewStandalone setTitle(@NotNull String title) {
        webview.setTitle(title);
        return this;
    }

    public WebviewStandalone dispatch(@NotNull Runnable handler) {
        webview.dispatch(handler);
        return this;
    }

    public WebviewStandalone setMinSize(int width, int height) {
        webview.setMinSize(width, height);
        return this;
    }

    public WebviewStandalone setMaxSize(int width, int height) {
        webview.setMaxSize(width, height);
        return this;
    }

    public void run() {
        webview.run();
    }

    public WebviewStandalone setSize(int width, int height) {
        webview.setSize(width, height);
        return this;
    }

    public WebviewStandalone setFixedSize(int width, int height) {
        webview.setFixedSize(width, height);
        return this;
    }

    public WebviewStandalone evaluate(@NotNull String script) {
        webview.evaluate(script);
        return this;
    }

    public WebviewStandalone setDarkMode(boolean darkMode) {
        if (PlatformSpecific.current.getPackageName().startsWith("windows")) {
            WinHelper.setWindowAppearance(new Pointer(this.webview.$pointer), darkMode);
        }
        return this;
    }

    public WebviewStandalone bind(@NotNull String name, WebviewNative.@NotNull NativeBindCallback callback) {
        webview.bind(name, callback);
        return this;
    }

    public void close() {
        webview.close();
    }

    public WebviewStandalone unbind(@NotNull String name) {
        webview.unbind(name);
        return this;
    }

    public Thread runAsync() {
        return webview.runAsync();
    }
}
