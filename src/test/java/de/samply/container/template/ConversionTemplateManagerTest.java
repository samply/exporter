package de.samply.container.template;

import de.samply.template.conversion.ConversionTemplateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ConversionTemplateManagerTest {

  private ConversionTemplateManager templates;
  private String templatesDirectory = "./templates";

  @BeforeEach
  void setUp() {
    this.templates = new ConversionTemplateManager(templatesDirectory);
    //TODO
  }

  @Test
  void getResultTemplate() {
    //TODO
  }
}
