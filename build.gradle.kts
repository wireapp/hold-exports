plugins {
    kotlin("jvm") version "1.4.31"
    id("net.nemerosa.versioning") version "2.14.0"
}

group = "com.wire.integrations.hold.exports"
version = versioning.info?.tag ?: versioning.info?.lastTag ?: "development"

repositories {
    jcenter()
    mavenCentral()
}

tasks {
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


subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "kotlin")

    repositories {
        jcenter()
        mavenCentral()

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
        // stdlib
        implementation(kotlin("stdlib-jdk8"))
        // extension functions
        implementation("pw.forst.tools", "katlib", "1.2.1")

        // Jackson JSON
        val jacksonVersion = "2.12.1"
        implementation("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
        implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)
        implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", jacksonVersion)

        // logging
        implementation("io.github.microutils", "kotlin-logging", "2.0.4")
        implementation("ch.qos.logback", "logback-classic", "1.2.3")
        // if-else in logback.xml
        implementation("org.codehaus.janino", "janino", "3.1.3")

        // DI
        implementation("org.kodein.di", "kodein-di-jvm", "7.4.0")

        // tests
        testImplementation("io.mockk", "mockk", "1.10.6")
        testImplementation(kotlin("test"))
        testImplementation(kotlin("test-junit5"))

        val junitVersion = "5.7.1"
        testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion) // junit testing framework
        testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion) // generated parameters for tests
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }

        withType<Test> {
            useJUnitPlatform()
        }
    }
}
