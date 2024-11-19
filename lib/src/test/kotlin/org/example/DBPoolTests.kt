package org.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class DBPoolTests {

    companion object {
        private val dbContainer = PostgreSQLContainer<Nothing>("postgres:latest")

        @BeforeAll
        @JvmStatic
        fun startContainer() {
            dbContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun stopContainer() {
            dbContainer.stop()
        }
    }

    @Test
    @Throws(TimeoutException::class)
    fun exampleTest() {
        val config = HikariConfig().apply {
            jdbcUrl = dbContainer.jdbcUrl
            username = dbContainer.username
            password = dbContainer.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
        }
        val dataSource = HikariDataSource(config)

        runWithTimeout(5_000) {
            for (i in 1..6) {
               val connection = dataSource.getConnection()
                println(connection.metaData.url)
            }
        }
    }

    fun <T> runWithTimeout(timeoutMillis: Long, block: () -> T): T {
        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor.submit(Callable { block() })
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            throw TimeoutException("The block execution exceeded the timeout of $timeoutMillis milliseconds.")
        } finally {
            executor.shutdownNow()
        }
    }
}
