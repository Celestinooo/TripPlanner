// Root project — gerencia plugins para todos os subprojetos.
// Nenhum módulo Android é configurado aqui.
plugins {
    alias(libs.plugins.android.library)      apply false
    alias(libs.plugins.android.application)  apply false
    alias(libs.plugins.kotlin.android)       apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp)                  apply false
    alias(libs.plugins.hilt)                 apply false
}
