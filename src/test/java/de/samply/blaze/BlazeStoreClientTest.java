package de.samply.blaze;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class BlazeStoreClientTest {

  private BlazeStoreClient blazeStoreClient;
  private String blazeStoreUrl = "http://localhost:8091";
  private String query;
  private String queryFormat;
  @BeforeEach
  void setUp() {
     this.blazeStoreClient = new BlazeStoreClient(blazeStoreUrl);
  }

  @Test
  void retrieve() {
    blazeStoreClient.retrieve(query, queryFormat);
    //TODO
  }

}
