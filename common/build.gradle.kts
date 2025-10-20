repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.alessiodp.com/releases/")
}

dependencies {
    api(project(":api"))

    api("de.exlll:configlib-yaml:4.6.3")
    api("com.j256.ormlite:ormlite-core:6.1")
    api("com.j256.ormlite:ormlite-jdbc:6.1")
    api("net.byteflux:libby-bukkit:1.3.1")

    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
}
