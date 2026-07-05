plugins {
    java
}

layout.buildDirectory.set(rootProject.file("build/assets"))

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

sourceSets {
    main {
        resources {
            srcDir(".")
            exclude("out")
        }
    }
}
