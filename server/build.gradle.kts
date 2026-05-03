plugins {
    java
    application
}

application {
    mainClass = "com.quizapp.server.Main"
}

dependencies {
    implementation(project(":shared"))
    implementation("org.mindrot:jbcrypt:0.4")
}