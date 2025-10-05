plugins {
    id("java")
    id("application")
}

group = "info.dawns"
version = "1.0-SNAPSHOT"

application {
    mainClass = "info.dawns.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("joda-time:joda-time:2.14.0")

    implementation("net.dv8tion:JDA:5.6.1")
    implementation("net.dv8tion:JDA:5.6.1")

    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
}

tasks.test {
    useJUnitPlatform()
}