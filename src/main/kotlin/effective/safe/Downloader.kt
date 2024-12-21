package effective.safe.downloader

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.test.assertEquals

data class User(val name: String)

interface NetworkService {
    suspend fun getUser(id: Int): User
}

class FakeNetworkService : NetworkService {
    override suspend fun getUser(id: Int): User {
        delay(2)
        return User("User$id")
    }
}

class UserDownloader(private val api: NetworkService) {

    val dispatcher = Dispatchers.IO.limitedParallelism(1)

    private var users = listOf<User>()

    fun downloaded(): List<User> = users

    suspend fun getUser(id: Int) = withContext(dispatcher) {
        val newUser = api.getUser(id)
        users += newUser
    }
}

suspend fun main() = coroutineScope {
    val downloader = UserDownloader(FakeNetworkService())
    coroutineScope {
        repeat(1_000_000) {
            launch {
                downloader.getUser(it)
            }
        }
    }
    print(downloader.downloaded().size) // ~714725
}

class UserDownloaderTest {
    @Test
    fun test() = runBlocking {
        val downloader = UserDownloader(FakeNetworkService())
        coroutineScope {
            repeat(1_000_000) {
                launch(Dispatchers.Default) {
                    downloader.getUser(it)
                }
            }
        }
        assertEquals(1_000_000, downloader.downloaded().size)
    }
}
