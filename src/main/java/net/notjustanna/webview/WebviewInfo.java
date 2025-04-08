package net.notjustanna.webview;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.notjustanna.webview.natives.WebviewNative;

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
            cString(struct.version_number),
            cString(struct.pre_release),
            cString(struct.build_metadata)
        );
    }

    private static String cString(byte[] arr) {
        int len;
        for (len = 0; len < arr.length; len++) {
            if (arr[len] == 0) {
                break;
            }
        }
        return new String(arr, 0, len);
    }
}
