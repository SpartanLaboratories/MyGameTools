plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.9.24"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Spartan Laboratories Tools
    api("io.github.spartanlaboratories:WebTools:1.0.1")
    api("io.github.spartanlaboratories:GeneralTools:1.0.5")

    // Serialization
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Test
    testApi(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.spartanlaboratories", "GameTools", "1.0.7")

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