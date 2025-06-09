plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    // Common
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")

    implementation("dev.jorel:commandapi-bukkit-shade:10.0.1")
    implementation("xyz.xenondevs.invui:invui:1.45")
    implementation("org.bstats:bstats-bukkit:3.0.2")
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
        relocate("xyz.xenondevs.inventoryaccess", "dev.snowz.snowreports.libs.inventoryaccess")
        relocate("org.bstats", "dev.snowz.snowreports.libs.bstats")
        relocate("net.byteflux.libby", "dev.snowz.snowreports.libs.libby")
        relocate("org.intellij.lang", "dev.snowz.snowreports.libs.intellij")
        relocate("org.jetbrains.annotations", "dev.snowz.snowreports.libs.jetbrains")
        relocate("org.snakeyaml.engine", "dev.snowz.snowreports.libs.snakeyaml")
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}
