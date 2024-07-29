package dylan.kwon.versionMigrator

/**
 * Interface for migrations executed by [dylan.kwon.versionMigrator.migrator.VersionMigrator]
 * Extend this interface to define migration tasks that must be performed when updating to a specific version.*
 */
interface VersionMigration {

    /**
     * Version Code of this migration.
     */
    val versionCode: Long

    /**
     * Specifies the policy for handling migration failures.
     */
    val policy: VersionMigrationPolicy

    /**
     * Performs the migration.
     */
    suspend fun migrate(): Result<Unit>

}