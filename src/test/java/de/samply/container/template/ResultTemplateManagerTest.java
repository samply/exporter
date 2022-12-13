package de.samply.container.template;

import de.samply.result.container.template.ResultTemplateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ResultTemplateManagerTest {

  private ResultTemplateManager templates;
  private String templatesDirectory = "./templates";

  @BeforeEach
  void setUp() {
    this.templates = new ResultTemplateManager(templatesDirectory);
    //TODO
  }

  @Test
  void getResultTemplate() {
    //TODO
  }
}
