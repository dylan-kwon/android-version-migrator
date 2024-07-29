@file:OptIn(ExperimentalCoroutinesApi::class)

package dylan.kwon.versionMigrator.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

/**
 * Data store for saving the version code of the most recently completed migration. (Internal use only)
 */
internal val Context.dataStore by preferencesDataStore("version-migration")

/**
 * Default value if no migrations have been performed yet.
 */
internal const val UNINITIALIZED_VERSION_CODE = -1L

/**
 * Data Store Keys.
 */
private object Key {
    val LATEST_VERSION_CODE = longPreferencesKey("latest_version_code")
}

/**
 * Version code of the last performed migration.
 */
internal val DataStore<Preferences>.latestVersionCode: Flow<Long>
    get() = data.mapLatest {
        it[Key.LATEST_VERSION_CODE] ?: UNINITIALIZED_VERSION_CODE
    }

/**
 * Update [latestVersionCode].
 */
internal suspend fun DataStore<Preferences>.updateLatestVersion(versionCode: Long) {
    edit { pref ->
        pref[Key.LATEST_VERSION_CODE] = versionCode
    }
}