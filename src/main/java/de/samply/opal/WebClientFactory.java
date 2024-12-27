package de.samply.opal;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

public class WebClientFactory {

    private final int webClientMaxNumberOfRetries;
    private final int webClientTimeInSecondsAfterRetryWithFailure;
    private final int webClientRequestTimeoutInSeconds;
    private final int webClientConnectionTimeoutInSeconds;
    private final int webClientTcpKeepIdleInSeconds;
    private final int webClientTcpKeepIntervalInSeconds;
    private final int webClientTcpKeepConnectionNumberOfTries;
    private final int webClientBufferSizeInBytes;

    public WebClientFactory(
            Integer webClientRequestTimeoutInSeconds,
            Integer webClientConnectionTimeoutInSeconds,
            Integer webClientTcpKeepIdleInSeconds,
            Integer webClientTcpKeepIntervalInSeconds,
            Integer webClientTcpKeepConnectionNumberOfTries,
            Integer webClientMaxNumberOfRetries,
            Integer webClientTimeInSecondsAfterRetryWithFailure,
            Integer webClientBufferSizeInBytes
    ) {
        this.webClientMaxNumberOfRetries = webClientMaxNumberOfRetries;
        this.webClientTimeInSecondsAfterRetryWithFailure = webClientTimeInSecondsAfterRetryWithFailure;
        this.webClientRequestTimeoutInSeconds = webClientRequestTimeoutInSeconds;
        this.webClientConnectionTimeoutInSeconds = webClientConnectionTimeoutInSeconds;
        this.webClientTcpKeepIdleInSeconds = webClientTcpKeepIdleInSeconds;
        this.webClientTcpKeepIntervalInSeconds = webClientTcpKeepIntervalInSeconds;
        this.webClientTcpKeepConnectionNumberOfTries = webClientTcpKeepConnectionNumberOfTries;
        this.webClientBufferSizeInBytes = webClientBufferSizeInBytes;
    }

    public WebClient createWebClient(String baseUrl, String username, String password) {
        WebClient.Builder webClientBuilder = WebClient.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(webClientBufferSizeInBytes))
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(webClientRequestTimeoutInSeconds))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientConnectionTimeoutInSeconds * 1000)
                                .option(ChannelOption.SO_KEEPALIVE, true)
                                .option(EpollChannelOption.TCP_KEEPIDLE, webClientTcpKeepIdleInSeconds)
                                .option(EpollChannelOption.TCP_KEEPINTVL, webClientTcpKeepIntervalInSeconds)
                                .option(EpollChannelOption.TCP_KEEPCNT, webClientTcpKeepConnectionNumberOfTries)
                ))
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(username, password)
                );

        return webClientBuilder.build();
    }

    public int getWebClientMaxNumberOfRetries() {
        return webClientMaxNumberOfRetries;
    }

    public int getWebClientTimeInSecondsAfterRetryWithFailure() {
        return webClientTimeInSecondsAfterRetryWithFailure;
    }

}