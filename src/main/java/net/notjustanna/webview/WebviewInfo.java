package net.notjustanna.webview;

import com.sun.jna.Native;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.notjustanna.webview.natives.WebviewNative;

import java.nio.charset.StandardCharsets;

/**
 * Singleton class that provides information about the webview version.
 *
 * @author Alex Bowles, Anna Silva
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebviewInfo {
    public static final WebviewInfo INSTANCE = init();

    private final int major;
    private final int minor;
    private final int patch;
    private final String versionNumber;
    private final String preRelease;
    private final String buildMetadata;

    private static WebviewInfo init() {
        WebviewNative.VersionInfoStruct struct = WebviewNative.INSTANCE.webview_version();

        return new WebviewInfo(
            struct.major,
            struct.minor,
            struct.patch,
            Native.toString(struct.version_number, StandardCharsets.UTF_8),
            Native.toString(struct.pre_release, StandardCharsets.UTF_8),
            Native.toString(struct.build_metadata, StandardCharsets.UTF_8)
        );
    }
}
