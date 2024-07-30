package dylan.kwon.versionmigrator.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dylan.kwon.versionMigrator.migrator.SequentialVersionMigrator
import dylan.kwon.versionmigrator.migrations.V10
import dylan.kwon.versionmigrator.migrations.V11
import dylan.kwon.versionmigrator.migrations.V12
import dylan.kwon.versionmigrator.sample.ui.theme.VersionMigratorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val versionMigrator = SequentialVersionMigrator(
        applicationContext = this,
        versionMigrations = listOf(
            V10, V11, V12,
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VersionMigratorTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen(
        coroutineScope: CoroutineScope = rememberCoroutineScope()
    ) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                var enabled by remember {
                    mutableStateOf(true)
                }
                ElevatedButton(
                    modifier = Modifier.align(Alignment.Center),
                    enabled = enabled,
                    onClick = {
                        coroutineScope.launch {
                            enabled = false
                            startMigration()
                            enabled = true
                        }
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.start_migrate)
                    )
                }
            }
        }
    }

    private suspend fun startMigration() {
        try {
            versionMigrator.migrate().getOrThrow()

        } catch (e: Exception) {
            e.printStackTrace()

        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}