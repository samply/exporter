package de.samply.utils;

import de.samply.exporter.ExporterConst;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
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
            @Value(ExporterConst.WEBCLIENT_REQUEST_TIMEOUT_IN_SECONDS_SV) Integer webClientRequestTimeoutInSeconds,
            @Value(ExporterConst.WEBCLIENT_CONNECTION_TIMEOUT_IN_SECONDS_SV) Integer webClientConnectionTimeoutInSeconds,
            @Value(ExporterConst.WEBCLIENT_TCP_KEEP_IDLE_IN_SECONDS_SV) Integer webClientTcpKeepIdleInSeconds,
            @Value(ExporterConst.WEBCLIENT_TCP_KEEP_INTERVAL_IN_SECONDS_SV) Integer webClientTcpKeepIntervalInSeconds,
            @Value(ExporterConst.WEBCLIENT_TCP_KEEP_CONNECTION_NUMBER_OF_TRIES_SV) Integer webClientTcpKeepConnectionNumberOfTries,
            @Value(ExporterConst.WEBCLIENT_MAX_NUMBER_OF_RETRIES_SV) Integer webClientMaxNumberOfRetries,
            @Value(ExporterConst.WEBCLIENT_TIME_IN_SECONDS_AFTER_RETRY_WITH_FAILURE_SV) Integer webClientTimeInSecondsAfterRetryWithFailure,
            @Value(ExporterConst.WEBCLIENT_BUFFER_SIZE_IN_BYTES_SV) Integer webClientBufferSizeInBytes
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

}