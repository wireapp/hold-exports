package com.wire.integrations.hold.exports.utils

import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.HttpUrlConnectorProvider
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.Configuration

object JerseyClientBuilder {
    private fun config(): ClientConfig {
        return ClientConfig()
    }

    fun build(): Client {
        return setSharedProperties(ClientBuilder.newClient(config()))
    }

    fun buildWithProxy(proxyHost: String?, proxyPort: Int, ignoredHosts: Set<String?> = emptySet<String>()): Client {
        // https://stackoverflow.com/a/56300006/7169288
        val jerseyClient = ClientBuilder.newClient(config().connectorProvider { client: Client?, runtimeConfig: Configuration? ->
            val customConnProv = HttpUrlConnectorProvider()
            customConnProv.connectionFactory { url: URL ->
                val connection: HttpURLConnection = if (ignoredHosts.contains(url.host)) {
                    url.openConnection() as HttpURLConnection
                } else {
                    val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort))
                    url.openConnection(proxy) as HttpURLConnection
                }
                connection
            }
            customConnProv.getConnector(client, runtimeConfig)
        })
        return setSharedProperties(jerseyClient)
    }

    private fun setSharedProperties(client: Client): Client {
        val timeoutSeconds = 40
        client.property(ClientProperties.CONNECT_TIMEOUT, timeoutSeconds * 1000)
        client.property(ClientProperties.READ_TIMEOUT, timeoutSeconds * 1000)
        return client
    }
}


