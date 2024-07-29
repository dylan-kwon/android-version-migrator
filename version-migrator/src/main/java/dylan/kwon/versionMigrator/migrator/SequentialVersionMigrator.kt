package dylan.kwon.versionMigrator.migrator

import android.content.Context
import dylan.kwon.versionMigrator.VersionMigration
import dylan.kwon.versionMigrator.VersionMigrationPolicy
import dylan.kwon.versionMigrator.datastore.UNINITIALIZED_VERSION_CODE
import dylan.kwon.versionMigrator.datastore.dataStore
import dylan.kwon.versionMigrator.datastore.latestVersionCode
import dylan.kwon.versionMigrator.datastore.updateLatestVersion
import dylan.kwon.versionMigrator.extensions.versionCode
import kotlinx.coroutines.flow.first

/**
 * [SequentialVersionMigrator] is a migrator that executes all provided migration tasks sequentially.
 */
class SequentialVersionMigrator(

    /**
     * Android Context.
     */
    private val applicationContext: Context,

    /**
     * List of migration tasks to be executed.
     */
    override val versionMigrations: List<VersionMigration>

) : VersionMigrator {

    /**
     * Executes migration tasks sequentially from the last migrated version up to latest version.
     * Uses the version code of the currently running app.
     */
    override suspend fun migrate(): Result<Unit> {
        // Get Version Code.
        val versionCode = applicationContext.versionCode

        // Validate.
        validate(versionMigrations, versionCode)

        // Get the version of the last performed migration.
        val latestVersionCode = getLatestVersionCode()

        // On the first run, consider it the latest version and do not perform migrations.
        if (isFirstLaunch(latestVersionCode)) {
            updateLatestVersionCode(versionCode)
            return Result.success(Unit)
        }

        // Sort the migration list in ascending order by version.
        // Filter out migrations that have already been completed.
        val migrations = versionMigrations
            .sortedByVersionCode()
            .filterGraterThan(latestVersionCode)

        // Proceed with migrations sequentially.
        for (migration in migrations) {
            // migrate.
            val result = migration.migrate()

            // Migration successful.
            if (result.isSuccess) {
                // Go to next migration.
                updateLatestVersionCode(migration.versionCode)
                continue
            }

            // Migration failed.
            when (migration.policy) {
                VersionMigrationPolicy.CONTINUE -> {
                    // Go to next migration.
                    updateLatestVersionCode(migration.versionCode)
                    continue
                }

                VersionMigrationPolicy.STOP -> {
                    // Stop migration and return.
                    return result
                }
            }
        }

        // All migrations completed successfully.
        updateLatestVersionCode(versionCode)
        return Result.success(Unit)
    }

    /**
     * Determine if it is the first run based on whether the DataStore has been initialized.
     */
    private fun isFirstLaunch(latestVersionCode: Long) =
        latestVersionCode == UNINITIALIZED_VERSION_CODE

    /**
     * the version code of the last performed migration.
     */
    private suspend fun getLatestVersionCode(): Long =
        applicationContext.dataStore.latestVersionCode.first()

    /**
     * Update the version code of the last performed migration.
     */
    private suspend fun updateLatestVersionCode(versionCode: Long) {
        applicationContext.dataStore.updateLatestVersion(versionCode)
    }

    /**
     * Sort the migration list in ascending order by version code.
     */
    private fun List<VersionMigration>.sortedByVersionCode() = sortedBy {
        it.versionCode
    }

    /**
     * Filter out migrations with a version code smaller than latestVersionCode.
     */
    private fun List<VersionMigration>.filterGraterThan(latestVersionCode: Long) = filter {
        it.versionCode > latestVersionCode
    }

    /**
     * Validate the migration list.
     * Migration version cannot be greater than the current app version.
     */
    private fun validate(versionMigrations: List<VersionMigration>, versionCode: Long) {
        if (versionMigrations.isEmpty()) {
            return
        }
        val max = versionMigrations.maxBy {
            it.versionCode
        }
        require(max.versionCode <= versionCode) {
            "Migration version cannot be greater than the current app version."
        }
    }
}