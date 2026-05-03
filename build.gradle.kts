subprojects {
    apply(plugin = "java")

    group = "com.quizapp"
    version = "1.0.0"

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
    }
}