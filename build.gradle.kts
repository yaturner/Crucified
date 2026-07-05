group = "de.gamedevbaden"
version = "0.1"

plugins {
    java
    application
    idea
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass.set("de.gamedevbaden.crucified.tests.SimpleClientServerTest.ClientTest")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
        }
    }
    test {
        java {
            setSrcDirs(listOf("test"))
        }
    }
}

dependencies {
    implementation(fileTree("lib") { include("*.jar") })

    implementation(libs.bundles.jmonkeyengine)

    // jme3-bullet(-native) was discontinued upstream after 3.3.0-alpha1;
    // Minie is the maintained drop-in replacement (same com.jme3.bullet.* API).
    implementation(libs.minie)

    runtimeOnly(project(":assets"))
}

tasks.register<JavaExec>("runServer") {
    classpath = sourceSets.test.get().runtimeClasspath + files("assets", ".")
    mainClass.set("de.gamedevbaden.crucified.tests.SimpleClientServerTest.ServerTest")
    standardInput = System.`in`
}

tasks.register<JavaExec>("runClient") {
    classpath = sourceSets.test.get().runtimeClasspath + files("assets", ".")
    mainClass.set("de.gamedevbaden.crucified.tests.SimpleClientServerTest.ClientTest")
    standardInput = System.`in`
}

tasks.register<JavaExec>("runSingleplayer") {
    classpath = sourceSets.test.get().runtimeClasspath + files("assets", ".")
    mainClass.set("de.gamedevbaden.crucified.tests.SingleplayerTest")
    standardInput = System.`in`
}

tasks.register<JavaExec>("runChunk") {
    classpath = sourceSets.test.get().runtimeClasspath + files("assets", ".")
    mainClass.set("de.gamedevbaden.crucified.tests.ChunkTest")
    standardInput = System.`in`
}
