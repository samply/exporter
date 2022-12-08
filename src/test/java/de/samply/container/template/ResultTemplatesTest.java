package de.samply.container.template;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ResultTemplatesTest {

  private ResultTemplates templates;
  private String templatesDirectory = "./templates";

  @BeforeEach
  void setUp() {
    this.templates = new ResultTemplates(templatesDirectory);
    //TODO
  }

  @Test
  void getResultTemplate() {
    //TODO
  }
}
