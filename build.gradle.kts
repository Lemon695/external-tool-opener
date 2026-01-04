plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.lemon.externaltool"
version = "1.1.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.0")
    testImplementation("junit:junit:4.13.2")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.5")
    type.set("IC") // Target IDE Platform (IC = IntelliJ IDEA Community)
//    downloadSources.set(false)

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("201")
        untilBuild.set("") // Empty = no upper limit
        
        changeNotes.set("""
            <h3>Version 1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>Right-click menu to open files with external tools</li>
                <li>Configurable external tool list</li>
                <li>Support for multiple file type associations</li>
                <li>Tool sorting and management</li>
                <li>Custom icons support</li>
            </ul>
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
