plugins {
    id("java")
    id("application")
}

group = "me.hydos"
version = "1.0-SNAPSHOT"
val lwjglVersion = project.properties["lwjglVersion"]
val jomlVersion = project.properties["jomlVersion"]
val lwjglNatives = project.properties["lwjglNatives"]

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

fun DependencyHandlerScope.lwjgl(name: String, useNatives: Boolean = true) {
    implementation("org.lwjgl:lwjgl-$name")
    if (useNatives) runtimeOnly("org.lwjgl:lwjgl-" + name + "::${project.properties["lwjglNatives"]}")
}

dependencies {
    // Google
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.flatbuffers:flatbuffers-java:23.3.3")

    // Annotations
    implementation("org.jetbrains:annotations:24.0.1")

    // Logger
    implementation("org.jline:jline:3.16.0")
    implementation("org.jline:jline-reader:3.12.1")
    implementation("net.minecrell:terminalconsoleappender:1.2.0") { isTransitive = false }
    implementation("org.slf4j:slf4j-api:2.0.7")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.19.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")

    // LWJGL
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.joml:joml:${jomlVersion}")
    implementation("org.lwjgl:lwjgl")
    lwjgl("glfw")
    lwjgl("assimp")
    lwjgl("stb")
    lwjgl("vulkan", false)
    lwjgl("shaderc")
    lwjgl("vma")
    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
}

java {
    java.sourceCompatibility = JavaVersion.VERSION_20
    java.targetCompatibility = JavaVersion.VERSION_20
}

tasks.withType<JavaCompile> {
     options.compilerArgs.add("--enable-preview")
}
