package de.samply.opal;

public class OpalServer {

  private String url;
  private String user;
  private String password;
  private String database;
  private String filesDirectory;

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

}
