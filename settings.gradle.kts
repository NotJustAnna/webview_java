rootProject.name = "webview_java"

val nativeEditions = providers.gradleProperty("natives.editions")
    .getOrElse("")
    .split(',')
    .map { ":natives:$it" }
    .toTypedArray()

val interopEditions = providers.gradleProperty("interop.editions")
    .getOrElse("")
    .split(',')
    .map { ":interop:$it" }
    .toTypedArray()

include(":natives", ":interop", *nativeEditions, *interopEditions)