package de.samply.opal;

import org.springframework.web.reactive.function.client.WebClient;

public class OpalServer {

  private String url;
  private String user;
  private String password;
  private String database;
  private String filesDirectory;
  private WebClientFactory webClientFactory;

  public OpalServer(String url, String user, String password, String database,
      String filesDirectory, WebClientFactory webClientFactory) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.database = database;
    this.filesDirectory = filesDirectory;
    this.webClientFactory = webClientFactory;
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

  public WebClient createWebClient() {
    return webClientFactory.createWebClient(this.url.trim(), this.user, this.password);
  }

}
