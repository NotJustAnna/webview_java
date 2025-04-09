plugins {
    `java-library`
    `maven-publish`
}

val baseVersion = providers.gradleProperty("webview_java.version").get()
val nativeVersion = providers.gradleProperty("natives.github-release").get().split('+').first()

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = "net.notjustanna.webview"
    version = "$baseVersion+wv$nativeVersion"

    publishing {
        repositories {
            this.maven {
                name = "local"
                url = uri("${project.rootDir}/.repo")
            }
        }
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")

    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
    testRuntimeOnly(project(":natives"))
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    (options as StandardJavadocDocletOptions).links("https://docs.oracle.com/en/java/javase/17/docs/api/")
}

publishing {
    publications {
        register<MavenPublication>("default") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}