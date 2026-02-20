package com.lunatech.chef.api.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.ApplicationTestBuilder

/**
 * Shared test utilities for route tests.
 * Provides consistent Jackson configuration and helper functions.
 */
object RouteTestHelpers {
    /**
     * Creates a configured ObjectMapper with JavaTimeModule and KotlinModule.
     */
    fun configuredObjectMapper(): ObjectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

    /**
     * Creates a configured Jackson converter for use in server ContentNegotiation.
     * Supports LocalDate serialization and Kotlin data classes.
     */
    fun jacksonConverter(): JacksonConverter = JacksonConverter(configuredObjectMapper())

    /**
     * ContentType for JSON requests/responses.
     */
    val jsonContentType: ContentType = ContentType.Application.Json
}

/**
 * Creates an HTTP client configured with Jackson for JSON serialization.
 * Use this for POST/PUT requests that need to serialize request bodies.
 */
fun ApplicationTestBuilder.jsonClient(): HttpClient =
    createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                registerModule(KotlinModule.Builder().build())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }
    }
