package net.notjustanna.webview.natives;

import lombok.Getter;
import lombok.ToString;

/**
 * Enum that represents the platform-specific details for loading webview binaries.
 * @author Alex Bowles, Anna Silva
 */
@Getter
@ToString
public enum PlatformSpecific {
    DARWIN("darwin", "libwebview.dylib"),
    LINUX_X86_64("linux-x86-64", "libwebview.so"),
    LINUX_X86("linux-x86", "libwebview.so"),
    LINUX_ARM64("linux-arm64", "libwebview.so"),
    WINDOWS_X86_64("windows-x86-64", "webview.dll"),
    WINDOWS_X86("windows-x86", "webview.dll"),
    WINDOWS_ARM64("windows-arm64", "webview.dll");

    private final String packageName;
    private final String binaryName;
    private final String fileName;
    private final String fileExtension;
    private final boolean isWindows;

    public static final PlatformSpecific current = detectCurrent();

    PlatformSpecific(String packageName, String binaryName) {
        String[] parts = binaryName.split("\\.", 2);
        this.packageName = packageName;
        this.binaryName = binaryName;
        this.fileName = parts[0];
        this.fileExtension = parts.length > 1 ? parts[1] : "bin";
        this.isWindows = this.name().startsWith("WINDOWS");
    }

    private static PlatformSpecific detectCurrent() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (os.contains("win")) {
            if (arch.contains("64")) {
                return WINDOWS_X86_64;
            } else if (arch.contains("32")) {
                return WINDOWS_X86;
            } else if (arch.contains("arm64")) {
                return WINDOWS_ARM64;
            }
        } else if (os.contains("mac") || os.contains("darwin")) {
            return DARWIN;
        } else if (os.contains("nix") || os.contains("nux")) {
            if (arch.contains("64")) {
                return LINUX_X86_64;
            } else if (arch.contains("32")) {
                return LINUX_X86;
            } else if (arch.contains("arm64")) {
                return LINUX_ARM64;
            }
        }
        throw new UnsupportedOperationException("Unsupported platform: " + os + " " + arch);
    }
}
