package dylan.kwon.versionMigrator.migrator

import dylan.kwon.versionMigrator.VersionMigration
import dylan.kwon.versionMigrator.VersionMigrationPolicy

class SuccessVersionMigration(
    override val versionCode: Long,
    private val showLog: Boolean = false,
) : VersionMigration {

    override val policy: VersionMigrationPolicy = VersionMigrationPolicy.CONTINUE

    override suspend fun migrate(): Result<Unit> {
        if (showLog) {
            println("versionCode: $versionCode is success.")
        }
        return Result.success(Unit)
    }
}

class FailureVersionMigration(
    override val versionCode: Long,
    override val policy: VersionMigrationPolicy = VersionMigrationPolicy.CONTINUE,
    private val exception: Exception = Exception(),
    private val showLog: Boolean = false,
) : VersionMigration {

    override suspend fun migrate(): Result<Unit> {
        if (showLog) {
            println("versionCode: $versionCode is failed.")
        }
        return Result.failure(exception = exception)
    }
}