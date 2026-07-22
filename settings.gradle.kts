pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TripPlanner"

// :tripplanner — módulo biblioteca (a feature em si)
// :app         — runner standalone (APK para rodar isolado)
//
// Para integrar no Portfolio, aponte o projectDir para ../TripPlanner/tripplanner:
//   include(":feature:tripplanner")
//   project(":feature:tripplanner").projectDir = file("../TripPlanner/tripplanner")
include(":tripplanner")
include(":app")
