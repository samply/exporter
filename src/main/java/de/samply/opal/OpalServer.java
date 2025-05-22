package de.samply.opal;

import de.samply.utils.WebClientFactory;
import org.springframework.web.reactive.function.client.WebClient;

public class OpalServer {

    private final String url;
    private final String user;
    private final String password;
    private final String database;
    private final String filesDirectory;

    public OpalServer(String url, String user, String password, String database,
                      String filesDirectory) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.database = database;
        this.filesDirectory = filesDirectory;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getFilesDirectory() {
        return filesDirectory;
    }

    public WebClient createWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.createWebClient(this.url.trim(), this.user, this.password);
    }

}
