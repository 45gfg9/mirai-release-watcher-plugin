plugins {
    val kt = "1.4.31"

    kotlin("jvm") version kt
    kotlin("plugin.serialization") version kt
    id("net.mamoe.mirai-console") version "2.6.4"
}

group = "net.im45.bot"
version = "1.0.0-dev01"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    val ktor = "1.5.4"

    implementation("com.google.code.gson:gson:2.8.6")
    implementation("io.ktor:ktor-client-auth:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")
}
