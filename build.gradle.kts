plugins {
    id("application")
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")


    implementation("org.postgresql:postgresql:42.2.20")
    implementation ("org.openjfx:javafx-controls:20")
    implementation ("org.openjfx:javafx-fxml:20")
    implementation ("org.openjfx:javafx-base:20")
    implementation ("org.openjfx:javafx-graphics:20")
    implementation ("org.controlsfx:controlsfx:11.1.1")
    implementation ("org.kordamp.ikonli:ikonli-javafx:12.2.0")
    implementation ("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


application {
    mainClass.set("org.example.Main")

    applicationDefaultJvmArgs = listOf(
        "--module-path", "C:\\Users\\ioana\\Downloads\\openjfx-21.0.5_windows-x64_bin-sdk.zip\\javafx-sdk-21.0.5\\lib",
        "--add-modules", "javafx.controls,javafx.fxml,javafx.base,javafx.graphics"
    )
}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.base", "javafx.graphics")
}

