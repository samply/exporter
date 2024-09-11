package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.apache.ApacheRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import de.samply.converter.ConverterException;
import de.samply.converter.EmptySession;
import de.samply.converter.SourceConverterImpl;
import de.samply.exporter.ExporterConst;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

public abstract class FhirRelatedConverter<O> extends SourceConverterImpl<String, O, EmptySession> {

    protected IGenericClient client;
    protected final String sourceId;
    private String proxyHost = null;
    private Integer proxyPort = null;
    private String proxyUser = null;
    private String proxyPassword = null;
    private final Set<String> noProxy;


    public FhirRelatedConverter(String fhirStoreUrl, String sourceId) throws ConverterException {
        this(fhirStoreUrl, sourceId, null, null);
    }

    public FhirRelatedConverter(String fhirStoreUrl,
                                String sourceId,
                                String httpProxy,
                                String noProxy) throws ConverterException {
        this.sourceId = sourceId;
        setProxyConfiguration(httpProxy);
        this.noProxy = (StringUtils.hasText(noProxy)) ?
                Set.of(noProxy.replace(" ", "").split(",")) : null;
        this.client = recreateFhirClient(fhirStoreUrl);
    }

    private void setProxyConfiguration(String httpProxy) throws ConverterException {
        if (StringUtils.hasText(httpProxy)) {
            URL url = createUrl(httpProxy);
            proxyHost = url.getHost();
            proxyPort = url.getPort();
            setProxyUserAndPassword(url.getUserInfo());
        }
    }

    private URL createUrl(String httpProxy) throws ConverterException {
        try {
            return new URL(httpProxy);
        } catch (MalformedURLException e) {
            throw new ConverterException(e);
        }
    }

    private void setProxyUserAndPassword(String userInfo) {
        if (userInfo != null) {
            String[] credentials = userInfo.split(":");
            if (credentials.length == 2) {
                proxyUser = credentials[0];
                proxyPassword = credentials[1];
            }
        }
    }

    private IGenericClient recreateFhirClient(String blazeStoreUrl) {
        FhirContext fhirContext = FhirContext.forR4();
        createIRestfulClientFactoryWithProxy(fhirContext).ifPresent(fhirContext::setRestfulClientFactory);
        return configureHttpClient(fhirContext).newRestfulGenericClient(blazeStoreUrl);
    }

    private Optional<IRestfulClientFactory> createIRestfulClientFactoryWithProxy(FhirContext fhirContext) {
        if (StringUtils.hasText(proxyHost) && proxyPort != null) {
            HttpHost httpHost = new HttpHost(proxyHost, proxyPort);
            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            Optional<HttpRoutePlanner> httpRoutePlanner = createHttpRoutePlanner(httpHost);
            httpRoutePlanner.ifPresentOrElse(httpClientBuilder::setRoutePlanner, () -> httpClientBuilder.setProxy(httpHost));
            createCredentialsProvider().ifPresent(httpClientBuilder::setDefaultCredentialsProvider);
            ApacheRestfulClientFactory clientFactory = new ApacheRestfulClientFactory(fhirContext);
            clientFactory.setHttpClient(httpClientBuilder.build());
            return Optional.of(clientFactory);
        }
        return Optional.empty();
    }

    // Note: Call it only if proxyHost and proxyPort are already set.
    private Optional<CredentialsProvider> createCredentialsProvider() {
        if (StringUtils.hasText(proxyUser) && StringUtils.hasText(proxyPassword)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            AuthScope authScope = new AuthScope(proxyHost, proxyPort);
            Credentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            credentialsProvider.setCredentials(authScope, credentials);
            return Optional.of(credentialsProvider);
        }
        return Optional.empty();
    }

    private Optional<HttpRoutePlanner> createHttpRoutePlanner(@NotNull HttpHost proxy) {
        if (noProxy != null && noProxy.size() > 0) {
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy) {
                @Override
                public HttpHost determineProxy(HttpHost target, org.apache.http.HttpRequest request,
                                               org.apache.http.protocol.HttpContext context) throws HttpException {
                    // Check if the target host is in the "no proxy" set
                    if (noProxy.contains(target.getHostName())) {
                        return null;  // No proxy for this host
                    }
                    return super.determineProxy(target, request, context);
                }
            };
            return Optional.of(routePlanner);
        }
        return Optional.empty();
    }

    private FhirContext configureHttpClient(FhirContext fhirContext) {
        IRestfulClientFactory restfulClientFactory = fhirContext.getRestfulClientFactory();
        restfulClientFactory.setConnectTimeout(ExporterConst.DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS * 1000);
        restfulClientFactory.setConnectionRequestTimeout(ExporterConst.DEFAULT_CONNECTION_REQUEST_TIMEOUT_IN_SECONDS * 1000);
        restfulClientFactory.setSocketTimeout(ExporterConst.DEFAULT_SOCKET_TIMEOUT_IN_SECONDS * 1000);
        return fhirContext;
    }

    @Override
    public String getSourceId() {
        return this.sourceId;
    }


}
