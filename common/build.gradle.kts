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
    // dependencies necessary to download the assets
    implementation("com.wire", "helium", "1.0-SNAPSHOT")
    implementation("org.glassfish.jersey.inject", "jersey-hk2", "2.32")
    implementation("org.glassfish.jersey.media", "jersey-media-json-jackson", "2.32")
    implementation("javax.activation", "activation", "1.1.1")
}
