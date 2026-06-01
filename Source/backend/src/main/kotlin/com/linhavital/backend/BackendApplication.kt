package com.linhavital.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.linhavital.backend"])
@EnableJpaRepositories(basePackages = ["com.linhavital.backend.repository"])
@EntityScan(basePackages = ["com.linhavital.backend.model"])
@EnableScheduling
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}