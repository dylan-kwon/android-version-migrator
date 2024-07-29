package dylan.kwon.versionMigrator.extensions

import android.content.Context
import android.os.Build

/**
 * Returns the application version code.
 */
internal val Context.versionCode: Long
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
        } else {
            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
        }
    }