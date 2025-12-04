import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("io.ktor.plugin") version "3.3.3"
    id("com.github.node-gradle.node") version "7.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
    id("com.github.autostyle") version "4.0.1"
}

application {
    group = "com.lunatech"
    version = "0.0.1"
    mainClass = "io.ktor.server.netty.EngineMain"
}

sourceSets {
    main {
        java.srcDirs("src/main")
    }
    test {
        java.srcDirs("src/test")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.21")
    implementation("com.newrelic.logging:logback:3.4.0")
    implementation("org.apache.logging.log4j:log4j-core:2.25.2")

    implementation("com.google.api-client:google-api-client:2.8.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.github.config4k:config4k:0.7.0")
    implementation("org.flywaydb:flyway-core:11.18.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.18.0")
    implementation("org.ktorm:ktorm-core:4.1.1")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("org.quartz-scheduler:quartz:2.5.1")

    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-sessions-jvm")
    implementation("io.ktor:ktor-client-apache-jvm")
    implementation("io.ktor:ktor-serialization-jackson")

    implementation("org.apache.poi:poi-ooxml:5.5.0")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-logging-jvm")

    implementation("org.simplejavamail:simple-java-mail:8.12.6")

    testImplementation("io.ktor:ktor-server-test-host")
}

tasks.register("buildAll") {
    dependsOn(tasks.assemble, buildFrontApp)
}

val buildFrontApp by tasks.registering(NpmTask::class) {
    dependsOn(installReactApp)
    args = listOf("run", "build", "--prefix", "./frontend/")
}

val installReactApp by tasks.registering(NpmTask::class) {
    args = listOf("install", "--prefix", "./frontend/")
}
