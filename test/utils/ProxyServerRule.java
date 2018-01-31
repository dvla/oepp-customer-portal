package utils;

import org.junit.rules.ExternalResource;

public class ProxyServerRule extends ExternalResource {

    private final String remoteHost;
    private final int remotePort;

    private final ProxyServer proxyServer;

    public ProxyServerRule(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.proxyServer = new ProxyServer();
    }

    public int getPort() {
        return proxyServer.getProxyPort().orElseGet(() -> {
            throw new RuntimeException("Proxy server must be started prior to obtaining port number");
        });
    }

    public void startServer() {
        proxyServer.start(remoteHost, remotePort);
    }

    public void stopServer() {
        proxyServer.stop();
    }

    @Override
    protected void before() throws Throwable {
        proxyServer.start(remoteHost, remotePort);
    }

    @Override
    protected void after() {
        proxyServer.stop();
    }

}
