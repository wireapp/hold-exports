plugins {
    kotlin("jvm") version "1.4.30"
    application
    distribution
    id("net.nemerosa.versioning") version "2.14.0"
}

group = "com.wire.integrations.hold"
version = versioning.info?.tag ?: versioning.info?.lastTag ?: "development"

val mClass = "com.wire.integrations.hold.exports.MainKt"

application {
    mainClass.set(mClass)
}

repositories {
    jcenter()
}

dependencies {
    // stdlib
    implementation(kotlin("stdlib-jdk8"))
    // extension functions
    implementation("pw.forst.tools", "katlib", "1.2.1")

    // Jackson JSON
    val jacksonVersion = "2.12.1"
    implementation("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)

    // logging
    implementation("io.github.microutils", "kotlin-logging", "2.0.4")

    // DI
    implementation("org.kodein.di", "kodein-di-jvm", "7.3.1")

    // database
    implementation("org.postgresql", "postgresql", "42.2.2")

    val exposedVersion = "0.29.1"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)

    val coroutinesVersion = "1.4.2"
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutinesVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutinesVersion)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    distTar {
        archiveFileName.set("app.tar")
    }

    withType<Test> {
        useJUnitPlatform()
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

    register("resolveDependencies") {
        doLast {
            project.allprojects.forEach { subProject ->
                with(subProject) {
                    buildscript.configurations.forEach { if (it.isCanBeResolved) it.resolve() }
                    configurations.compileClasspath.get().resolve()
                    configurations.testCompileClasspath.get().resolve()
                }
            }
        }
    }
}
