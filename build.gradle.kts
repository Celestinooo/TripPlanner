plugins {
    alias(libs.plugins.portfolio.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.lucas.portfolio.feature.tripplanner"
}

dependencies {
    implementation(project(":core:network"))

    implementation(libs.datastore.preferences)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.core)
}
