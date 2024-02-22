package org.jenkinsci.plugins.yamlaxis.util;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

public final class DescriptorUtils {

  private DescriptorUtils() {}

  public static FormValidation checkFieldNotEmpty(String value, String field) {
    if (StringUtils.isBlank(value)) {
      return FormValidation.error(field + " can not be empty");
    }
    return FormValidation.ok();
  }
}
