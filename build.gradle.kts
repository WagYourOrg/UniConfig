plugins {
    id("java")
    id("xyz.wagyourtail.unimined")// version "1.1.0"
    id("org.gradle.gradle-profiler") version "0.0.2"
}

profiler {
    asyncProfilerLocation.set(file("/opt/async-profiler"))
}

operator fun String.invoke(): String? {
    return project.properties[this] as String?
}

group = "group"()!!
version = "version"()!!

base {
    archivesName.set("archives_base_name"()!!)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    maven("https://maven.wagyourtail.xyz/releases")
    mavenCentral()
}

val forge: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}
val fabric: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

val testForge: SourceSet by sourceSets.creating {
    compileClasspath += forge.output + sourceSets.test.get().compileClasspath
    runtimeClasspath += forge.output + sourceSets.test.get().runtimeClasspath
}

val testFabric: SourceSet by sourceSets.creating {
    compileClasspath += fabric.output + sourceSets.test.get().compileClasspath
    runtimeClasspath += fabric.output + sourceSets.test.get().runtimeClasspath
}

unimined.minecraft(sourceSets.main.get(), sourceSets.test.get()) {
    version("minecraft_version"()!!)

    mappings {
        mojmap()
        intermediary()
        parchment("1.20.2", "2023.10.22")
    }
    defaultRemapJar = false

    minecraftRemapper.config {
        ignoreConflicts(true)
    }
}

unimined.minecraft(fabric) {
    combineWith(sourceSets.main.get())

    fabric {
        loader("fabric_version"()!!)
    }

    defaultRemapJar = true
}

unimined.minecraft(forge) {
    combineWith(sourceSets.main.get())

    neoForged {
        loader("forge_version"()!!)
    }

    defaultRemapJar = true
}

unimined.minecraft(testFabric) {
    combineWith(sourceSets.test.get())

    fabric {
        loader("fabric_version"()!!)
    }
}

unimined.minecraft(testForge) {
    combineWith(sourceSets.test.get())

    neoForged {
        loader("forge_version"()!!)
    }
}

dependencies {

    compileOnly("com.demonwav.mcdev:annotations:2.0.0")

    "fabricInclude"("fabricModImplementation"(fabricApi.fabricModule("fabric-api-base", "fabric_api_version"()!!))!!)
    "fabricInclude"(
        "fabricModImplementation"(
            fabricApi.fabricModule(
                "fabric-resource-loader-v0",
                "fabric_api_version"()!!
            )
        )!!
    )
    "fabricInclude"(
        "fabricModImplementation"(
            fabricApi.fabricModule(
                "fabric-command-api-v2",
                "fabric_api_version"()!!
            )
        )!!
    )

    "testFabricModImplementation"("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()!!}")

    "fabricInclude"(implementation("com.electronwill.night-config:core:3.6.7") {})
    "fabricInclude"(implementation("com.electronwill.night-config:toml:3.6.7") {})
    "fabricInclude"(implementation("com.electronwill.night-config:yaml:3.6.7") {})
    "fabricInclude"(implementation("com.electronwill.night-config:json:3.6.7") {})
    "fabricInclude"(implementation("com.electronwill.night-config:hocon:3.6.7") {})

    implementation("org.jetbrains:annotations:24.0.1")
    implementation("com.google.code.findbugs:jsr305:3.0.2")


}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.named<ProcessResources>("processFabricResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.named<ProcessResources>("processTestFabricResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}


tasks.named<ProcessResources>("processForgeResources") {
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}

tasks.named<ProcessResources>("processTestForgeResources") {
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}
