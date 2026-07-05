plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.gamedevbaden.crucified"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.gamedevbaden.crucified"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.setSrcDirs(
                listOf("src")
            )
            assets.srcDirs("assets")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<JavaCompile>().configureEach {
    javaCompiler.set(
        javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    )
}

dependencies {
    implementation(fileTree("lib") {
        include("*.jar")
        exclude("*-sources.jar", "*-javadoc.jar")
    })

    implementation(libs.bundles.jmonkeyengine)

    // jme3-bullet(-native) was discontinued upstream after 3.3.0-alpha1; Minie
    // is the maintained drop-in replacement (same com.jme3.bullet.* API),
    // here using its "+droid" build which bundles Android native libraries.
    implementation(libs.minie)
}
