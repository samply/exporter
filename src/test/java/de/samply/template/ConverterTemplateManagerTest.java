package de.samply.template;

import de.samply.template.ConverterTemplateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ConverterTemplateManagerTest {

  private ConverterTemplateManager templates;
  private String templatesDirectory = "./templates";

  @BeforeEach
  void setUp() {
    this.templates = new ConverterTemplateManager(templatesDirectory);
    //TODO
  }

  @Test
  void getResultTemplate() {
    //TODO
  }
}
