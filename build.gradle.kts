plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application")
    id("maven-publish")
}

group = "gg.flyte"
version = "0.1.5-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.9.0")
    testImplementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}

application {
    mainClass.set("gg.flyte.munch.Test")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        val `package` = "gg.flyte.munch.shaded"
        relocate("kotlin", "$`package`.kotlin")
        relocate("com.mongodb", "$`package`.mongodb")
        relocate("org.bson", "$`package`.bson")
        relocate("org.intellij", "$`package`.intellij")
        relocate("org.jetbrains", "$`package`.jetbrains")
    }
}

kotlin {
    jvmToolchain(17)
}

publishing {
    repositories {
        maven {
            name = "flyte-repository"
            url = uri(
                "https://repo.flyte.gg/${
                    if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
                }"
            )
            credentials {
                username = System.getenv("MAVEN_NAME") ?: property("mavenUser").toString()
                password = System.getenv("MAVEN_SECRET") ?: property("mavenPassword").toString()
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = group.toString()
                artifactId = "munch"
                version = version.toString()

                from(components["java"])
            }
        }
    }
}