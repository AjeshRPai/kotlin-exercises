package effective.safe.cancellingrefresher

import coroutines.dispatcher.experiments.dispatcher
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CancellingRefresher(
    private val scope: CoroutineScope,
    private val refreshData: suspend () -> Unit,
) {
    private var refreshJob: Job? = null
    private val refreshLock = Mutex()
    private val refreshLoc = Any()

    suspend fun refresh() = withContext(dispatcher){
        refreshJob?.cancel()
        refreshJob = scope.launch {
            refreshData()
        }
    }
}



class CancellingRefresherTest {
    @Test
    fun `should cancel previous refresh when starting new one`(): Unit = runTest {
        val userRefresher = CancellingRefresher(
            scope = backgroundScope,
            refreshData = {
                delay(1000)
            }
        )
        coroutineScope {
            repeat(1000) {
                launch { userRefresher.refresh() }
            }
            delay(1000)
            repeat(1000) {
                launch { userRefresher.refresh() }
            }
            delay(1000)
            repeat(1000) {
                launch { userRefresher.refresh() }
            }
        }
        assertEquals(2000, currentTime) // Delays
        val children = backgroundScope.coroutineContext[Job]!!.children
        assertEquals(1, children.count { it.isActive })
        children.forEach { it.join() }
        assertEquals(3000, currentTime)
    }

    @Test
    fun `should cancel all previous jobs`(): Unit = runTest {
        val userRefresher = CancellingRefresher(
            scope = backgroundScope,
            refreshData = { delay(Long.MAX_VALUE) }
        )
        coroutineScope {
            repeat(50_000) {
                launch {
                    userRefresher.refresh()
                }
            }
        }
        delay(1000)
        assertEquals(1, backgroundScope.coroutineContext.job.children.count { it.isActive })
    }

    @Test
    fun `should cancel all previous jobs (real time)`(): Unit = runBlocking(Dispatchers.Default) {
        val backgroundScope = CoroutineScope(Job() + Dispatchers.Default)
        val userRefresher = CancellingRefresher(
            scope = backgroundScope,
            refreshData = { delay(Long.MAX_VALUE) }
        )
        coroutineScope {
            repeat(50_000) {
                launch {
                    userRefresher.refresh()
                }
            }
        }
        delay(1000)
        assertEquals(1, backgroundScope.coroutineContext.job.children.count { it.isActive })
        backgroundScope.cancel()
    }
}
