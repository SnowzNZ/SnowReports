plugins {
    java
    `java-library`
}

group = "dev.snowz.snowreports"
version = "1.1.1"

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "${rootProject.group}.${project.name}"
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        api("org.jspecify:jspecify:1.0.0")

        compileOnly("org.projectlombok:lombok:1.18.38")
        annotationProcessor("org.projectlombok:lombok:1.18.38")

        testCompileOnly("org.projectlombok:lombok:1.18.38")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }
}
