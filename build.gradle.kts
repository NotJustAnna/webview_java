plugins {
    // Building a java library which publishes artifacts to a Maven repository.
    `java-library`
    `maven-publish`
    id("me.champeau.mrjar") version "0.1"
}

// Properties from gradle.properties file
val baseVersion = providers.gradleProperty("webview_java.version").get()
val nativeVersion = providers.gradleProperty("natives.github-release").get().split('+').first()

allprojects {
    // Apply the Java Library and Maven Publish plugins to all subprojects.
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    // Set the group ID and version for all projects.
    group = "net.notjustanna.webview"
    version = "$baseVersion+wv$nativeVersion"

    // Configure the local repository for all projects, located at `.repo` in the root directory.
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
    // Set the Java toolchain to use Java 21.
    // Include source and Javadoc JAR in the build.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
    withJavadocJar()
}

// Configures the multi-release JAR settings.
// Specifies the target Java versions for the multi-release JAR.
multiRelease {
    targetVersions(17, 21) // Defines the Java versions for which specific class files can be included.
}

// Sets the source and target compatibility of the main Java code to Java 17.
tasks.compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
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
    // Configure Javadoc options to suppress warnings and link to the Java 17 API documentation.
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