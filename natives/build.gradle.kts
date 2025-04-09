import kotlinx.serialization.json.*
import java.net.URI

// We need a Json parser to decode the JSON response from the GitHub API.
buildscript {
    repositories { mavenCentral() }
    dependencies { classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") }
}

dependencies {
    // ":natives" has a dependency on all subprojects
    subprojects.forEach { runtimeOnly(it) }
}

allprojects {
    // Set Java compatibility for all projects
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks {
    // Task to download metadata from a GitHub release
    val downloadMetadata by creating {
        // Retrieve properties for the GitHub repository and release
        val repo = providers.gradleProperty("natives.github-repo").get()
        val release = providers.gradleProperty("natives.github-release").get()

        // Declare task inputs for caching purposes
        this.inputs.properties("repo" to repo, "release" to release)

        doLast {
            // Fetch release metadata from the GitHub API
            val releaseUrls = URI("https://api.github.com/repos/$repo/releases/tags/$release")
                .toURL().openStream().use { it.readAllBytes().toString(Charsets.UTF_8) }
                .let { Json.decodeFromString<JsonObject>(it) }
                .getValue("assets").jsonArray.map {
                    // Extract download URLs for assets
                    it.jsonObject.getValue("browser_download_url").jsonPrimitive.content
                }

            // Store the release URLs in the task's extra properties
            this.extra.set("releaseUrls", releaseUrls)
        }
    }
}

subprojects {
    tasks {
        // Task to download native libraries for the current subproject
        val downloadNatives by creating {
            // Construct the file name for the native library
            val fileName = "${providers.gradleProperty("natives.editions.${project.name}.identifier").get()}-lib.tar.gz"

            // Define the output directory for the downloaded file
            val outputDir = project.layout.buildDirectory.dir("download").get().asFile

            // Declare the output directory for caching purposes
            outputs.dir(outputDir)

            // Depend on the parent project's 'downloadMetadata' task
            val downloadMetadata by project.parent!!.tasks.getting
            dependsOn(downloadMetadata)

            this.doFirst {
                // Retrieve the release URLs from the 'downloadMetadata' task
                val releaseUrls: List<String> by downloadMetadata.extra
                val url = releaseUrls.firstOrNull { it.endsWith(fileName) }
                    ?: error("Unsupported native package for ${project.name} (${fileName})")

                // Ensure the output directory exists
                outputDir.mkdirs()

                // Download the file from the URL and save it to the output directory
                URI(url).toURL().openStream().use { input ->
                    outputDir.resolve("webview-${project.name}-lib.tar.gz").outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        // Task to extract the downloaded native libraries
        val extractNatives by creating(Copy::class) {
            dependsOn(downloadNatives) // Depend on the 'downloadNatives' task
            val inputFile = project.layout.buildDirectory.file("download/webview-${project.name}-lib.tar.gz")
            val outputDir = project.layout.buildDirectory.dir("natives")

            // Extract the tar.gz file into the output directory
            this.from(tarTree(inputFile))
            this.into(outputDir)
        }

        // Task to package the extracted native libraries into a ZIP file
        val pack by creating(Zip::class) {
            archiveBaseName = "native-${project.name}" // Set the base name for the archive
            dependsOn(extractNatives) // Depend on the 'extractNatives' task
            includeEmptyDirs = false // Exclude empty directories from the archive

            if (project.name.startsWith("windows")) {
                // Include the 'webview.dll' file for Windows projects
                val files = extractNatives.outputs.files.asFileTree.filter {
                    it.parentFile.name == "bin" && it.name == "webview.dll"
                }

                from(files)
                into("net/notjustanna/webview/natives/${project.name}")
            } else {
                // Include the appropriate shared library file for Linux or macOS projects
                val extension = if (project.name.startsWith("linux")) "so" else "dylib"
                val fileName = "libwebview"
                val singleFile = extractNatives.outputs.files.asFileTree
                    .filter { it.name.startsWith("$fileName.") && it.name.contains(".$extension") && it.length() > 0 }

                from(singleFile) { rename { "$fileName.$extension" } }
                into("net/notjustanna/webview/natives/${project.name}")
            }
        }

        // Modify the existing 'jar' task to include the packaged native libraries
        val jar by getting(Jar::class) {
            dependsOn(pack) // Depend on the 'pack' task
            from(zipTree(pack.outputs.files.singleFile)) // Include the contents of the ZIP file
        }
    }
}

allprojects {
    publishing {
        publications {
            register<MavenPublication>("default") {
                from(components["java"])
                groupId = project.group.toString()
                artifactId = if (project.name == "natives") "${rootProject.name}-all-natives"
                else "${rootProject.name}-native-${project.name}"
                version = project.version.toString()
            }
        }
    }
}