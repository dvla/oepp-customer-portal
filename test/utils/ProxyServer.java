package utils;

import com.google.common.io.ByteStreams;
import play.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

class ProxyServer {

    private Integer proxyPort;
    // proxy threads
    private ProxyServerRunnable proxyServerRunnable;
    private Thread proxyServerThread;

    Optional<Integer> getProxyPort() {
        return Optional.of(proxyPort);
    }

    void start(String remoteHost, int remotePort) {
        proxyPort = findAvailableTcpPort();

        proxyServerRunnable = new ProxyServerRunnable(proxyPort, remoteHost, remotePort);
        proxyServerThread = new Thread(proxyServerRunnable);
        proxyServerThread.start();
    }

    void stop() {
        proxyServerRunnable.stop();
        try {
            proxyServerThread.join();
        } catch (InterruptedException ex) {
            Logger.error("Failed to stop proxy server", ex);
        }
    }

    private class ProxyServerRunnable implements Runnable {

        private final int proxyPort;
        // remote server properties
        private final String remoteHost;
        private final int remotePort;

        private boolean listening = true;

        private ProxyServerRunnable(int proxyPort, String remoteHost, int remotePort) {
            this.proxyPort = proxyPort;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        @Override
        public void run() {
            ServerSocket proxySocket = null;

            try {
                proxySocket = new ServerSocket(proxyPort);
                proxySocket.setSoTimeout(100);
            } catch (IOException ex) {
                Logger.error("Failed to start proxy server", ex);
                tryClose(proxySocket);
                return;
            }

            try {
                while (listening) {
                    try {
                        new Thread(new ConnectionRunnable(proxySocket.accept(), remoteHost, remotePort)).start();
                    } catch (SocketTimeoutException ex) {
                        // ignore as awaiting for connection will start again
                    }
                }
            } catch (IOException ex) {
                Logger.error("Failed to start proxy server", ex);
            } finally {
                tryClose(proxySocket);
            }
        }

        private void tryClose(ServerSocket proxySocket) {
            if (proxySocket != null && !proxySocket.isClosed()) {
                try {
                    proxySocket.close();
                } catch (IOException ex) {
                    Logger.error("Failed to close proxy socket", ex);
                }
            }
        }

        private void stop() {
            listening = false;
        }
    }

    private class ConnectionRunnable implements Runnable {

        private final Socket clientSocket;
        // remote server properties
        private final String remoteHost;
        private final int remotePort;

        private ConnectionRunnable(Socket clientSocket, String remoteHost, int remotePort) {
            this.clientSocket = clientSocket;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        @Override
        public void run() {
            try {
                Socket serverSocket = new Socket(remoteHost, remotePort);

                new Thread(() -> {
                    try {
                        ByteStreams.copy(clientSocket.getInputStream(), serverSocket.getOutputStream());
                    } catch (IOException ex) {
                        Logger.error("Connection error occured", ex);
                    }
                }).start();

                ByteStreams.copy(serverSocket.getInputStream(), clientSocket.getOutputStream());
            } catch (IOException ex) {
                Logger.error("Connection error occured", ex);
            }
        }
    }

}
