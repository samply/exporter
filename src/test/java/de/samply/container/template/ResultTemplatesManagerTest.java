package de.samply.container.template;

import de.samply.result.container.template.ResultTemplatesManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ResultTemplatesManagerTest {

  private ResultTemplatesManager templates;
  private String templatesDirectory = "./templates";

  @BeforeEach
  void setUp() {
    this.templates = new ResultTemplatesManager(templatesDirectory);
    //TODO
  }

  @Test
  void getResultTemplate() {
    //TODO
  }
}
