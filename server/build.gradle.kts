plugins {
    application
    distribution
}

val mClass = "com.wire.integrations.hold.exports.MainKt"

application {
    mainClass.set(mClass)
}

dependencies {
    implementation(project(":common"))

    // Ktor server dependencies
    val ktorVersion = "1.5.2"
    implementation("io.ktor", "ktor-server-core", ktorVersion)
    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor", "ktor-jackson", ktorVersion)
    implementation("io.ktor", "ktor-websockets", ktorVersion)
    implementation("io.ktor", "ktor-auth", ktorVersion)
    implementation("io.ktor", "ktor-auth-jwt", ktorVersion)
    // ktor swagger
    implementation("com.github.papsign", "Ktor-OpenAPI-Generator", "0.2-beta.15")
}

tasks {
    distTar {
        archiveFileName.set("app.tar")
    }

    register<Jar>("fatJar") {
        manifest {
            attributes["Main-Class"] = mClass
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("app.jar")
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        from(sourceSets.main.get().output)
    }
}
