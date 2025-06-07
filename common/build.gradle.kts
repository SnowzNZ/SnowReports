plugins {
    `java-library`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.alessiodp.com/releases/")
}


dependencies {
    // API
    api(project(":api"))

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")

    // Configs
    implementation("de.exlll:configlib-yaml:4.6.0")

    // Database
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    // Libby (dependency management)
    implementation("net.byteflux:libby-bukkit:1.3.1")
}
