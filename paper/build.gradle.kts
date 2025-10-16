plugins {
    id("com.gradleup.shadow") version "9.2.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.alessiodp.com/releases/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    // Common
    implementation(project(":common"))

    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("dev.jorel:commandapi-paper-shade:11.0.0")
    implementation("xyz.xenondevs.invui:invui:1.47") {
        (0..15).forEach { version ->
            exclude(group = "xyz.xenondevs.inventoryaccess", module = "inventoryaccess-$version")
        }
    }
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks {
    processResources {
        filesMatching("**/paper-plugin.yml") {
            expand("version" to project.version)
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
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}
