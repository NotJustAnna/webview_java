import kotlinx.serialization.json.*
import java.net.URI

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") }
}

dependencies {
    subprojects.forEach { runtimeOnly(it) }
}

allprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks {
    val downloadMetadata by creating {
        val repo = providers.gradleProperty("natives.github-repo").get()
        val release = providers.gradleProperty("natives.github-release").get()

        this.inputs.properties("repo" to repo, "release" to release)

        doLast {
            val releaseUrls = URI("https://api.github.com/repos/$repo/releases/tags/$release")
                .toURL().openStream().use { it.readAllBytes().toString(Charsets.UTF_8) }
                .let { Json.decodeFromString<JsonObject>(it) }
                .getValue("assets").jsonArray.map {
                    it.jsonObject.getValue("browser_download_url").jsonPrimitive.content
                }

            this.extra.set("releaseUrls", releaseUrls)
        }
    }
}

subprojects {
    tasks {
        val assemble by getting

        val downloadNatives by creating {
            val fileName = "${providers.gradleProperty("natives.editions.${project.name}.identifier").get()}-lib.tar.gz"

            val outputDir = project.layout.buildDirectory.dir("download").get().asFile

            outputs.dir(outputDir)

            val downloadMetadata by project.parent!!.tasks.getting
            dependsOn(downloadMetadata)

            this.doFirst {
                val releaseUrls: List<String> by downloadMetadata.extra
                val url = releaseUrls.firstOrNull { it.endsWith(fileName) }
                    ?: error("Unsupported native package for ${project.name} (${fileName})")

                outputDir.mkdirs()

                URI(url).toURL().openStream().use { input ->
                    outputDir.resolve("webview-${project.name}-lib.tar.gz").outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        val extractNatives by creating(Copy::class) {
            dependsOn(downloadNatives)
            val inputFile = project.layout.buildDirectory.file("download/webview-${project.name}-lib.tar.gz")
            val outputDir = project.layout.buildDirectory.dir("natives")

            this.from(tarTree(inputFile))
            this.into(outputDir)
        }

        val pack by creating(Zip::class) {
            archiveBaseName = "native-${project.name}"
            dependsOn(extractNatives)
            includeEmptyDirs = false

            if (project.name.startsWith("windows")) {
                val files = extractNatives.outputs.files.asFileTree.filter {
                    it.parentFile.name == "bin" && it.name == "webview.dll"
                }

                from(files)
                into("net/notjustanna/webview/natives/${project.name}")
            } else {
                val extension = if (project.name.startsWith("linux")) "so" else "dylib"
                val fileName = "libwebview"
                val singleFile = extractNatives.outputs.files.asFileTree
                    .filter { it.name.startsWith("$fileName.") && it.name.contains(".$extension") && it.length() > 0 }

                from(singleFile) { rename { "$fileName.$extension" } }
                into("net/notjustanna/webview/natives/${project.name}")
            }
        }

        val jar by getting(Jar::class) {
            dependsOn(pack)
            from(zipTree(pack.outputs.files.singleFile))
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
