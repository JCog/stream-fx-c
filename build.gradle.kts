plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    modules("javafx.media")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(group = "com.github.twitch4j", name = "twitch4j", version = "1.26.0")
    implementation(group = "io.obs-websocket.community", name = "client", version = "2.0.0")
}

tasks.test {
    useJUnitPlatform()
}