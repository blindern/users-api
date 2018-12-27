package no.foreningenbs.usersapi.ldap

import java.net.InetAddress
import java.net.Socket
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class CustomSSLSocketFactory : SSLSocketFactory() {
  private val sf = createSf()

  private fun createKeyStore(): KeyStore {
    val ks = KeyStore.getInstance(KeyStore.getDefaultType())

    val certificateFactoryX509 = CertificateFactory.getInstance("X.509")
    val certificate = javaClass.classLoader.getResource("foreningenbs_slapd_cert.pem")
      .openStream()
      .use {
        certificateFactoryX509.generateCertificate(it)
      }

    ks.load(null, null)
    ks.setCertificateEntry("ldapmaster.vpn.foreningenbs.no", certificate)

    return ks
  }

  private fun createSf(): SSLSocketFactory {
    val keyStore: KeyStore = createKeyStore()
    val tmf = TrustManagerFactory.getInstance("SunX509")
    tmf.init(keyStore)
    val ctx = SSLContext.getInstance("TLS")
    ctx.init(null, tmf.trustManagers, null)
    return ctx.socketFactory
  }

  override fun createSocket(host: String?, port: Int): Socket =
    sf.createSocket(host, port)

  override fun createSocket(
    host: String?,
    port: Int,
    localHost: InetAddress?,
    localPort: Int
  ): Socket =
    sf.createSocket(host, port, localHost, localPort)

  override fun createSocket(host: InetAddress?, port: Int): Socket =
    sf.createSocket(host, port)

  override fun createSocket(
    address: InetAddress?,
    port: Int,
    localAddress: InetAddress?,
    localPort: Int
  ): Socket =
    sf.createSocket(address, port, localAddress, localPort)

  override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket =
    sf.createSocket(s, host, port, autoClose)

  override fun getDefaultCipherSuites(): Array<String> =
    sf.defaultCipherSuites

  override fun getSupportedCipherSuites(): Array<String> =
    sf.supportedCipherSuites
}
