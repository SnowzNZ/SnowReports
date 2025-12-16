plugins {
    id("com.gradleup.shadow") version "9.3.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    // Common
    implementation(project(":common"))

    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("xyz.xenondevs.invui:invui:1.47")

    implementation("dev.jorel:commandapi-paper-shade:11.1.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks {
    processResources {
        filesMatching("**/paper-plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("${rootProject.name}-${project.name[0].uppercase() + project.name.substring(1)}")
        archiveVersion.set("${project.version}")

        mergeServiceFiles()

        enableAutoRelocation = true
        relocationPrefix = "dev.snowz.snowreports.libs"

        minimize()
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}
