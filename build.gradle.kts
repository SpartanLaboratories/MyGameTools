plugins {
    // This is a plain Kotlin library, not a Gradle plugin. `kotlin-dsl` would drag in
    // gradleApi(), whose bundled slf4j provider outranks logback and silently discards
    // every INFO/DEBUG record the library logs.
    `java-library`
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Spartan Laboratories Tools
    // Drop WebTools' bundled GeneralTools so the explicit 2.0.1 below is the only one on the path.
    api("io.github.spartanlaboratories:WebTools:2.0.0") {
        exclude(group = "io.github.spartanlaboratories", module = "GeneralTools")
    }
    // Even though its included in webtools, the newest version (2.0.1) is needed for the Color class
    api("io.github.spartanlaboratories:GeneralTools:2.0.1")

    // Serialization
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Logging - the library only ever binds to the slf4j facade, so that consumers
    // remain free to pick their own implementation.
    api("org.slf4j:slf4j-api:2.0.16")

    // Test
    testImplementation(kotlin("test"))

    // Tests supply the logback implementation the facade binds to, so that the
    // library's structured logging is actually exercised and visible.
    testImplementation("ch.qos.logback:logback-classic:1.5.18")
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.spartanlaboratories", "GameTools", "1.6.0")

    pom {
        name.set("Game Tools")
        description.set("A set of generic game tools.")
        inceptionYear.set("2026")
        url.set("https://github.com/SpartanLaboratories/MyGameTools")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("SpaSinghOut")
                name.set("Spartak Singh")
                url.set("https://github.com/SpaSinghOut")
            }
        }
        scm {
            url.set("https://github.com/SpartanLaboratories/MyGameTools/")
            connection.set("scm:git:git://github.com/SpartanLaboratories/MyGameTools.git")
            developerConnection.set("scm:git:ssh://git@github.com/SpartanLaboratories/MyGameTools.git")
        }
    }
}