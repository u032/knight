package wiki.chess

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import wiki.chess.model.Config
import java.io.File
import java.nio.file.Paths

val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

val config: Config = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
    .readValue(File("${Paths.get("").toAbsolutePath()}/application.yml"), Config::class.java)
