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
        toolchain.languageVersion = JavaLanguageVersion.of(17)
        withSourcesJar()
        withJavadocJar()
    }

    fun artifactName(project: Project): String {
        return artifactName(project.parent ?: return project.name) + "-${project.name}"
    }

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
        api(project.parent!!)
    }
}