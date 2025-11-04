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
        // 👇 nếu file zpdk-release-v3.1.aar nằm trong app/libs thì nên ghi rõ:
        flatDir {
            dirs("app/libs")
        }
    }
}

rootProject.name = "Book_Store_MobileApp"
include(":app")
include(":mylibrary")
