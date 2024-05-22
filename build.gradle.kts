import xyz.wagyourtail.unimined.api.unimined

plugins {
    id("xyz.wagyourtail.unimined") version "1.2.5-SNAPSHOT"
    `maven-publish`
}

base {
    archivesName = project.properties["archives_base_name"] as String
    group = project.properties["maven_group"] as String
    version = project.properties["version"] as String
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val enabledPlatforms: List<String> = project.properties["enabled_platforms"]?.toString()?.split(",") ?: emptyList()

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

unimined.minecraft(sourceSets.main.get(), sourceSets.test.get()) {
    version(project.properties["minecraft_version"] as String)

    mappings {
        mojmap()

        devFallbackNamespace("official")
    }

    accessWidener {

    }

    defaultRemapJar = false
}

val fabricApiVersion = project.properties["fabric_api_version"] as String

if (enabledPlatforms.contains("fabric")) {
    val fabric by sourceSets.creating
    val testFabric by sourceSets.creating {
        compileClasspath += fabric.output + fabric.compileClasspath + sourceSets.test.get().output + sourceSets.test.get().compileClasspath
        runtimeClasspath += fabric.output + fabric.runtimeClasspath + sourceSets.test.get().output + sourceSets.test.get().runtimeClasspath
    }

    configurations {
        get("fabricImplementation").extendsFrom(configurations.getByName("implementation"))
        get("fabricCompileOnly").extendsFrom(configurations.getByName("compileOnly"))

        get("testFabricImplementation").extendsFrom(configurations.getByName("testImplementation"))
        get("testFabricCompileOnly").extendsFrom(configurations.getByName("testCompileOnly"))
    }

    unimined.minecraft(fabric, testFabric) {
        if (sourceSet == fabric) {
            combineWith(sourceSets.main.get())
        } else {
            combineWith(sourceSets.test.get())
        }
        defaultRemapJar = true

        fabric {
            loader(project.properties["fabric_loader_version"] as String)
        }
    }

    dependencies {
        "fabricModImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
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
} else {
    configurations {
        create("fabricInclude")
        create("fabricModImplementation")
        create("testFabricModImplementation")
    }
}

if (enabledPlatforms.contains("neoforge")) {
    val neoforge by sourceSets.creating
    val testNeoforge by sourceSets.creating {
        compileClasspath += neoforge.output + neoforge.compileClasspath + sourceSets.test.get().output + sourceSets.test.get().compileClasspath
        runtimeClasspath += neoforge.output + neoforge.runtimeClasspath + sourceSets.test.get().output + sourceSets.test.get().runtimeClasspath
    }

    configurations {
        get("neoforgeImplementation").extendsFrom(configurations.getByName("implementation"))
        get("neoforgeCompileOnly").extendsFrom(configurations.getByName("compileOnly"))

        get("testNeoforgeImplementation").extendsFrom(configurations.getByName("testImplementation"))
        get("testNeoforgeCompileOnly").extendsFrom(configurations.getByName("testCompileOnly"))
    }

    unimined.minecraft(neoforge, testNeoforge) {
        if (sourceSet == neoforge) {
            combineWith(sourceSets.main.get())
        } else {
            combineWith(sourceSets.test.get())
        }
        defaultRemapJar = true

        neoForged {
            loader(project.properties["neoforge_version"] as String)
        }
    }

    tasks.named<ProcessResources>("processNeoforgeResources") {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }

    tasks.named<ProcessResources>("processTestNeoforgeResources") {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }
} else {
    configurations {
        create("neoforgeInclude")
        create("neoforgeModImplementation")
        create("testNeoforgeModImplementation")
    }
}


val fabric_api_version: String by project.properties

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    compileOnly("org.ow2.asm:asm:9.5")
    compileOnly("com.demonwav.mcdev:annotations:2.1.0")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    "fabricInclude"("fabricModImplementation"(fabricApi.fabricModule("fabric-api-base", fabric_api_version))!!)
    "fabricInclude"(
        "fabricModImplementation"(
            fabricApi.fabricModule(
                "fabric-resource-loader-v0",
                fabric_api_version
            )
        )!!
    )
    "fabricInclude"(
        "fabricModImplementation"(
            fabricApi.fabricModule(
                "fabric-command-api-v2",
                fabric_api_version
            )
        )!!
    )

    "testFabricModImplementation"("net.fabricmc.fabric-api:fabric-api:${fabric_api_version}")

    "fabricInclude"(implementation("com.electronwill.night-config:core:3.6.7")!!)
    "fabricInclude"(implementation("com.electronwill.night-config:toml:3.6.7")!!)
    "fabricInclude"(implementation("com.electronwill.night-config:yaml:3.6.7")!!)
    "fabricInclude"(implementation("com.electronwill.night-config:json:3.6.7")!!)
    "fabricInclude"(implementation("com.electronwill.night-config:hocon:3.6.7")!!)
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "WagYourMaven"
            url = if (project.hasProperty("version_snapshot")) {
                uri("https://maven.wagyourtail.xyz/snapshots/")
            } else {
                uri("https://maven.wagyourtail.xyz/releases/")
            }
            credentials {
                username = project.findProperty("mvn.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("mvn.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.properties["archives_base_name"] as String? ?: project.name
            version = project.version as String

            artifact(tasks["jar"]) {
                classifier = null
            }

            artifact(tasks["sourcesJar"]) {
                classifier = "sources"
            }

            if (enabledPlatforms.contains("neoforge")) {
                artifact(tasks["remapNeoforgeJar"]) {
                    classifier = "neoforge"
                }
            }

            if (enabledPlatforms.contains("fabric")) {
                artifact(tasks["remapFabricJar"]) {
                    classifier = "fabric"
                }
            }
        }
    }
}
