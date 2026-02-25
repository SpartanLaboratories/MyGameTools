plugins {
    kotlin("jvm") version "2.1.10"
    `java-library`
    `maven-publish`
}

group = "com.spartanlabs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("D:/Documents/Programming")
}

dependencies {
    // *********** TESTING ****************//
    testImplementation(kotlin("test"))
    // *********** LOGGING ****************//
    //implementation("org.slf4j:slf4j-api:2.0.13") // Or the latest stable version
    //implementation("ch.qos.logback:logback-classic:1.5.6") // Or the latest stable version
    // ********* MY LIBRARIES *************//
    api("com.spartanlabs:GeneralTools:LATEST")
    //api("com.spartanlabs:WebTools:LATEST")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
publishing{
    publications{
        create<MavenPublication>("generaltools").from(components["java"])
        create<MavenPublication>("generaltools-snapshot"){
            version = "LATEST"
        }.from(components["java"])
    }
    repositories{
        maven("D:/Documents/Programming")
        /*
        maven{
            name = "generaltools"
            url= uri("https://maven.pkg.github.com/Spartan-Laboratories/GeneralTools")
            credentials{
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
         */
    }
}