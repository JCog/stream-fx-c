plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.4.0"
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
    implementation("com.github.twitch4j:twitch4j:1.26.0")
    implementation("io.obs-websocket.community:client:2.0.0")
    implementation("ch.qos.logback:logback-classic:1.5.32")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveVersion = ""
    archiveClassifier.set("")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.jcog.streamfxc.Main"
    }
}
