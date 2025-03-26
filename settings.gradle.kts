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

rootProject.name = "OnlyMine"
include(":app")
include(":OnlyMyPart")
include(":OnlyMyPart:adapter")
include(":OnlyMyPart:part")
include(":OnlyMineDelegate")
include(":OnlyMylifecycle")
include(":OnlyMyPart:fragment")
