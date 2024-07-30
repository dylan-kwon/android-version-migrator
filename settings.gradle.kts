import org.gradle.util.internal.GUtil.loadProperties

private val deployProperties = loadProperties(
    file("publish.properties")
)

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

        maven(deployProperties["githubRepoUrl"].toString()) {
            credentials {
                username = deployProperties["githubUserName"].toString()
                password = deployProperties["githubToken"].toString()
            }
        }
    }
}

rootProject.name = "VersionMigrator"

include(":sample")
include(":version-migrator")