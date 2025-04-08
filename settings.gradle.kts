rootProject.name = "webview_java"

val editions = providers.gradleProperty("natives.editions")
    .getOrElse("")
    .split(',')
    .map { ":natives:$it" }
    .toTypedArray()

include(":natives", *editions)