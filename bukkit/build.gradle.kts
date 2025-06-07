plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://libraries.minecraft.net")
    maven("https://repo.alessiodp.com/releases/")
}

dependencies {
    // Common
    implementation(project(":common"))

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")

    // Commands
    implementation("dev.jorel:commandapi-bukkit-shade:10.0.1")
    compileOnly("com.mojang:brigadier:1.0.18")

    // Database
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    // Inventories
    implementation("xyz.xenondevs.invui:invui:1.45")

    // Metrics
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // Libby (dependency management)
    implementation("net.byteflux:libby-bukkit:1.3.1")
}

tasks {
    processResources {
        filesMatching("**/plugin.yml") {
            expand("project" to project)
        }
    }

    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")

        archiveBaseName.set("${rootProject.name}-${project.name}")
        archiveVersion.set("${project.version}")

        relocate("de.exlll.configlib", "dev.snowz.snowreports.libs.configlib")
        relocate("dev.jorel.commandapi", "dev.snowz.snowreports.libs.commandapi")
        relocate("com.j256.ormlite", "dev.snowz.snowreports.libs.ormlite")
        relocate("xyz.xenondevs.invui", "dev.snowz.snowreports.libs.invui")
        relocate("org.bstats", "dev.snowz.snowreports.libs.bstats")
        relocate("net.byteflux.libby", "dev.snowz.snowreports.libs.libby")
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}
