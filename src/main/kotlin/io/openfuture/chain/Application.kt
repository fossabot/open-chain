package io.openfuture.chain

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class Application {

    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            flyway.clean()
            flyway.migrate()
        }
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}