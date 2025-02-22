package com.dropbox.android.external.store3

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.get
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.time.ExperimentalTime

@ExperimentalTime
@RunWith(Parameterized::class)
class SequentialTest(
    storeType: TestStoreType
) {
    private val testScope = TestCoroutineScope()

    var networkCalls = 0
    private val store = TestStoreBuilder.from<Pair<String, String>, Int>(
        scope = testScope,
        cached = true,
        fetcher = Fetcher.of {
            networkCalls++
        }
    ).build(storeType)

    @Test
    fun sequentially() = testScope.runBlockingTest {
        val b = "one" to "two"
        store.get(b)
        store.get(b)

        assertThat(networkCalls).isEqualTo(1)
    }

    @Test
    fun parallel() = testScope.runBlockingTest {
        val b = "one" to "two"
        val deferred = async { store.get(b) }
        store.get(b)
        deferred.await()

        assertThat(networkCalls).isEqualTo(1)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun params() = TestStoreType.values()
    }
}
