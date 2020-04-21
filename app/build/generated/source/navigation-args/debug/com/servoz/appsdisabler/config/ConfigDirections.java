package com.servoz.appsdisabler.config;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import com.servoz.appsdisabler.MobileNavigationDirections;

public class ConfigDirections {
  private ConfigDirections() {
  }

  @NonNull
  public static NavDirections actionGlobalConfig() {
    return MobileNavigationDirections.actionGlobalConfig();
  }
}
