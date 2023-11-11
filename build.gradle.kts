import xyz.wagyourtail.unimined.internal.minecraft.patch.forge.fg3.FG3MinecraftTransformer

plugins {
    id("java")
    id("xyz.wagyourtail.unimined")// version "1.1.0"
}

operator fun String.invoke(): String? {
    return project.properties[this] as String?
}

group = "group"()!!
version = "version"()!!

base {
    archivesName.set("archives_base_name"()!!)
}

repositories {
    maven("https://maven.wagyourtail.xyz/releases")
    mavenCentral()
}

val forge: SourceSet by sourceSets.creating
val fabric: SourceSet by sourceSets.creating

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
        (forgeTransformer as FG3MinecraftTransformer).binpatchFile = project.projectDir.toPath().resolve("output.lzma")
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
        (forgeTransformer as FG3MinecraftTransformer).binpatchFile = project.projectDir.toPath().resolve("output.lzma")
    }
}

dependencies {

    

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
