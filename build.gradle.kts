plugins {
    java
    `java-library`
}

val snapshot = true

group = "dev.snowz.snowreports"
version = "1.2.0" + if (snapshot) "-SNAPSHOT" else ""

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "${rootProject.group}.${project.name}"
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    dependencies {
        api("org.jspecify:jspecify:1.0.0")

        compileOnly("org.projectlombok:lombok:1.18.42")
        annotationProcessor("org.projectlombok:lombok:1.18.42")

        testCompileOnly("org.projectlombok:lombok:1.18.42")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }
}
