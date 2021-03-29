plugins {
    application
    distribution
}

val mClass = "com.wire.integrations.hold.exports.MainKt"

application {
    mainClass.set(mClass)
}

repositories {
    // lithium
    maven {
        url = uri("https://packagecloud.io/dkovacevic/helium/maven2")
    }
    maven {
        url = uri("https://packagecloud.io/dkovacevic/xenon/maven2")
    }
    maven {
        url = uri("https://packagecloud.io/dkovacevic/cryptobox4j/maven2")
    }

}

dependencies {
    // ------- Wire dependencies -------
    implementation("com.wire", "helium", "1.0-SNAPSHOT")
    implementation("org.glassfish.jersey.inject", "jersey-hk2", "2.32")
    implementation("org.glassfish.jersey.media", "jersey-media-json-jackson", "2.32")
    implementation("javax.activation", "activation", "1.1.1")
    // //----- Wire dependencies -------

    // DI
    implementation("org.kodein.di", "kodein-di-jvm", "7.4.0")

    // database
    implementation("org.postgresql", "postgresql", "42.2.19")

    val exposedVersion = "0.29.1"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)
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
