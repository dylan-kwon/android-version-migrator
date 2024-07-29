package dylan.kwon.versionMigrator

enum class VersionMigrationPolicy {

    /**
     * On migration failure, stop all migrations.
     */
    STOP,

    /**
     * On migration failure, ignore and continue with the next migration.
     */
    CONTINUE

}