package dylan.kwon.versionMigrator.migrator

import android.content.Context
import dylan.kwon.versionMigrator.VersionMigrationPolicy
import dylan.kwon.versionMigrator.datastore.dataStore
import dylan.kwon.versionMigrator.datastore.latestVersionCode
import dylan.kwon.versionMigrator.datastore.updateLatestVersion
import dylan.kwon.versionMigrator.extensions.versionCode
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SequentialVersionMigratorTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK(relaxed = true)
    lateinit var context: Context

    @Before
    fun setup() {
        mockkStatic("dylan.kwon.versionMigrator.datastore.VersionMigrationDataStoreKt")
        mockkStatic("dylan.kwon.versionMigrator.extensions.ContextExtensionKt")

        coEvery {
            context.dataStore.updateLatestVersion(any())
        } returns Unit
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `All migrations successful`() = runTest {
        suspend fun run(
            versionCode: Long,
            latestMigrationVersionCode: Long
        ) {
            every {
                context.versionCode
            } returns versionCode

            every {
                context.dataStore.latestVersionCode
            } returns flowOf(latestMigrationVersionCode)

            val migrations = List(versionCode.toInt()) {
                spyk(
                    SuccessVersionMigration(versionCode = (it + 1L))
                )
            }

            val migrator = SequentialVersionMigrator(
                applicationContext = context,
                versionMigrations = migrations
            )
            migrator.migrate()

            migrations.forEach {
                coVerify(
                    exactly = when {
                        it.versionCode > latestMigrationVersionCode -> 1
                        else -> 0
                    }
                ) {
                    it.migrate()
                }
            }
        }
        run(
            versionCode = 5L,
            latestMigrationVersionCode = 1L
        )
        run(
            versionCode = 5L,
            latestMigrationVersionCode = 5L
        )
        run(
            versionCode = 5L,
            latestMigrationVersionCode = 6L
        )
        run(
            versionCode = 10L,
            latestMigrationVersionCode = 3L
        )
    }

    @Test
    fun `Migrations Failure with Continue`() = runTest {
        suspend fun run(
            versionCode: Long,
            latestMigrationVersionCode: Long,
            failureIndexes: Set<Int>,
        ) {
            every {
                context.versionCode
            } returns versionCode

            every {
                context.dataStore.latestVersionCode
            } returns flowOf(latestMigrationVersionCode)

            val migrations = List(versionCode.toInt()) {
                when {
                    it in failureIndexes -> spyk(
                        FailureVersionMigration(versionCode = it + 1L)
                    )

                    else -> spyk(
                        SuccessVersionMigration(versionCode = it + 1L)
                    )
                }
            }

            val migrator = SequentialVersionMigrator(
                applicationContext = context,
                versionMigrations = migrations
            )
            migrator.migrate()

            migrations.forEach {
                coVerify(
                    exactly = when {
                        it.versionCode > latestMigrationVersionCode -> 1
                        else -> 0
                    }
                ) {
                    it.migrate()
                }
            }
        }
        run(
            versionCode = 10L,
            latestMigrationVersionCode = 3L,
            failureIndexes = mutableSetOf(2, 3, 5)
        )
        run(
            versionCode = 10L,
            latestMigrationVersionCode = 1L,
            failureIndexes = List(10) { it }.toSet()
        )
        run(
            versionCode = 10L,
            latestMigrationVersionCode = 7L,
            failureIndexes = setOf()
        )
    }

    @Test
    fun `Migrations Failure with Stop`() = runTest {
        suspend fun run(
            versionCode: Long,
            latestMigrationVersionCode: Long,
            failureIndex: Long,
        ) {
            every {
                context.versionCode
            } returns versionCode

            every {
                context.dataStore.latestVersionCode
            } returns flowOf(latestMigrationVersionCode)

            val migrations = List(versionCode.toInt()) {
                when (it.toLong()) {
                    failureIndex -> spyk(
                        FailureVersionMigration(
                            versionCode = it + 1L,
                            VersionMigrationPolicy.STOP
                        )
                    )

                    else -> spyk(
                        SuccessVersionMigration(versionCode = it + 1L)
                    )
                }
            }

            val migrator = SequentialVersionMigrator(
                applicationContext = context,
                versionMigrations = migrations
            )
            migrator.migrate()

            migrations.forEachIndexed { index, migration ->
                val isMigrationTarget = migration.versionCode > latestMigrationVersionCode
                val isCalled = index <= failureIndex

                coVerify(
                    exactly = when {
                        isCalled && isMigrationTarget -> 1
                        else -> 0
                    }
                ) {
                    migration.migrate()
                }
            }
        }
        run(
            versionCode = 10L,
            latestMigrationVersionCode = 3L,
            failureIndex = 4L
        )
        run(
            versionCode = 10L,
            latestMigrationVersionCode = 1L,
            failureIndex = 2L
        )
        run(
            versionCode = 10L,
            latestMigrationVersionCode = 7L,
            failureIndex = 9L
        )
    }
}