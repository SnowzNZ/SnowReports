repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // API
    api(project(":api"))

    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks {
    jar {
        archiveBaseName.set("SnowReports-velocity")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")
    }
}
