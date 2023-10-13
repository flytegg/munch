plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "gg.joshbaker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    /*compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }*/

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