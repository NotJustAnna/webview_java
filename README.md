# Webview Java

![Maven badge](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fgithub.com%2FNotJustAnna%2Fwebview_java%2Fraw%2Fmaven%2Fnet%2Fnotjustanna%2Fwebview%2Fwebview_java%2Fmaven-metadata.xml)
![Build status](https://img.shields.io/github/actions/workflow/status/NotJustAnna/webview_java/build.yml)


Java wrapper of [webview](https://github.com/webview/webview) for building web-based GUIs in Java, leveraging [JNA](https://github.com/java-native-access/jna).

## Supported Platforms
| Operating System | Architectures      |
|------------------|--------------------|
| Windows          | x86, x86_64, arm64 |
| macOS            | x86_64, arm64      |
| Linux            | x86_64             |   

Binaries are built at [NotJustAnna/webview_binaries](https://github.com/NotJustAnna/webview_binaries).

## Install

<details>
<summary>Gradle</summary>

```kts
repositories {
    mavenCentral()
    maven {
        url = uri("https://github.com/NotJustAnna/webview_java/raw/maven")
        content { includeGroup("net.notjustanna.webview") }
    }
}

dependencies {
    // base dependency
    implementation("net.notjustanna.webview:webview_java:1.0.0+wv0.12.0-nightly.1")
    
    // all native dependencies
    implementation("net.notjustanna.webview:webview_java-all-natives:1.0.0+wv0.12.0-nightly.1")

    // or add specific native dependencies
    implementation("net.notjustanna.webview:webview_java-native-windows-x86-64:1.0.0+wv0.12.0-nightly.1")
    implementation("net.notjustanna.webview:webview_java-native-darwin:1.0.0+wv0.12.0-nightly.1")
}
```

</details>
<details>
<summary>Maven</summary>

```xml
<project>
    <repositories>
        <repository>
            <id>webview-java-repo</id>
            <url>https://github.com/NotJustAnna/webview_java/raw/maven</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- base dependency -->
        <dependency>
            <groupId>net.notjustanna.webview</groupId>
            <artifactId>webview_java</artifactId>
            <version>1.0.0+wv0.12.0-nightly.1</version>
        </dependency>

        <!-- all native dependencies -->
        <dependency>
            <groupId>net.notjustanna.webview</groupId>
            <artifactId>webview_java-all-natives</artifactId>
            <version>1.0.0+wv0.12.0-nightly.1</version>
        </dependency>

        <!-- or add specific native dependencies -->
        <dependency>
            <groupId>net.notjustanna.webview</groupId>
            <artifactId>webview_java-native-windows-x86-64</artifactId>
            <version>1.0.0+wv0.12.0-nightly.1</version>
        </dependency>
        <dependency>
            <groupId>net.notjustanna.webview</groupId>
            <artifactId>webview_java-native-darwin</artifactId>
            <version>1.0.0+wv0.12.0-nightly.1</version>
        </dependency>
    </dependencies>
</project>
```

</details>
<details>
<summary>SBT</summary>

```scala
resolvers += "Webview Java Repo" at "https://github.com/NotJustAnna/webview_java/raw/maven"

libraryDependencies ++= Seq(
  // base dependency
  "net.notjustanna.webview" % "webview_java" % "1.0.0+wv0.12.0-nightly.1",

  // all native dependencies
  "net.notjustanna.webview" % "webview_java-all-natives" % "1.0.0+wv0.12.0-nightly.1",

  // or add specific native dependencies
  "net.notjustanna.webview" % "webview_java-native-windows-x86-64" % "1.0.0+wv0.12.0-nightly.1",
  "net.notjustanna.webview" % "webview_java-native-darwin" % "1.0.0+wv0.12.0-nightly.1"
)
```

</details>

## Examples

[Standalone Example](https://github.com/NotJustAnna/webview_java/blob/master/src/test/java/net/notjustanna/webview/StandaloneExample.java)