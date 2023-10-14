plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "gg.joshbaker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.9.0")
}

application {
    mainClass.set("gg.joshbaker.munch.Test")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    shadowJar {
        val `package` = "gg.flyte.twilight.shaded"
        relocate("kotlin", "$`package`.kotlin")
        relocate("com.mongodb", "$`package`.mongodb")
        relocate("org.bson", "$`package`.bson")
        relocate("org.intellij", "$`package`.intellij")
        relocate("org.jetbrains", "$`package`.jetbrains")
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

kotlin {
    jvmToolchain(17)
}