plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace  = "dev.lucas.portfolio.feature.tripplanner.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.lucas.portfolio.feature.tripplanner"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Módulo biblioteca
    implementation(project(":tripplanner"))

    // Hilt — necessário no :app para geração do componente raiz
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
