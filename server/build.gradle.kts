plugins {
    java
    application
}

application {
    mainClass = "com.quizapp.server.Main"
}

val smokeTest by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Runs lightweight server smoke tests"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass = "com.quizapp.server.ServerSmokeTest"
    dependsOn(tasks.named("testClasses"))
}

tasks.named("check") {
    dependsOn(smokeTest)
}

tasks.named<Test>("test") {
    failOnNoDiscoveredTests = false
}

dependencies {
    implementation(project(":shared"))
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.mysql:mysql-connector-j:9.3.0")
}
