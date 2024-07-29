package dylan.kwon.versionmigrator.migrations

import android.util.Log
import dylan.kwon.versionMigrator.VersionMigration
import dylan.kwon.versionMigrator.VersionMigrationPolicy

object V10 : VersionMigration {
    override val versionCode: Long = 10

    override val policy: VersionMigrationPolicy =
        VersionMigrationPolicy.CONTINUE

    override suspend fun migrate(): Result<Unit> {
        return try {
            Log.d("v${versionCode}", "Running migration for version code $versionCode")
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}