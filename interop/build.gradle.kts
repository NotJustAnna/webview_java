allprojects {
    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2")
        compileOnly("org.projectlombok:lombok:1.18.38")
        annotationProcessor("org.projectlombok:lombok:1.18.38")

        implementation(rootProject)
        implementation("net.java.dev.jna:jna:5.17.0")
        implementation("net.java.dev.jna:jna-platform:5.17.0")

        testCompileOnly("org.projectlombok:lombok:1.18.38")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
        testRuntimeOnly(project(":natives"))
    }

    java {
        // Set the Java toolchain to use Java 17.
        // Include source and Javadoc JAR in the build.
        toolchain.languageVersion = JavaLanguageVersion.of(17)
        withSourcesJar()
        withJavadocJar()
    }

    // Helper function to generate artifact names based on project hierarchy
    fun artifactName(project: Project): String {
        return artifactName(project.parent ?: return project.name) + "-${project.name}"
    }

    // Configures publishing settings for all projects
    publishing {
        publications {
            register<MavenPublication>("default") {
                from(components["java"])
                groupId = project.group.toString()
                artifactId = artifactName(project)
                version = project.version.toString()
            }
        }
    }
}

subprojects {
    dependencies {
        // Adds the parent project as an API dependency
        api(project.parent!!)
    }
}