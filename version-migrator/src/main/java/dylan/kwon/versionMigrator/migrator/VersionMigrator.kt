package dylan.kwon.versionMigrator.migrator

import dylan.kwon.versionMigrator.VersionMigration

/**
 * Interface for performing version migrations.
 * Implementations of this interface must define the logic for performing
 * [VersionMigration] tasks using the version code provided by the caller.
 */
interface VersionMigrator {

    /**
     * List of migration tasks to be executed.
     */
    val versionMigrations: List<VersionMigration>

    /**
     * Executes migration tasks corresponding to the input version code and returns the result.
     */
    suspend fun migrate(): Result<Unit>

}