plugins {
    java
    application
}

application {
    mainClass = "com.quizapp.client.Main"
    applicationDefaultJvmArgs = listOf(
        "--add-modules", "javafx.controls,javafx.fxml",
        "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED"
    )
}

dependencies {
    implementation(project(":shared"))
    implementation("org.openjfx:javafx-controls:21.0.5:linux")
    implementation("org.openjfx:javafx-fxml:21.0.5:linux")
}
