package net.notjustanna.webview;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.notjustanna.webview.natives.WebviewNative;

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
            WebviewNative.cString(struct.version_number),
            WebviewNative.cString(struct.pre_release),
            WebviewNative.cString(struct.build_metadata)
        );
    }
}
