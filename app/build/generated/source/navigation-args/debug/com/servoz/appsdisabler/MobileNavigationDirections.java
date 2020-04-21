package com.servoz.appsdisabler;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

public class MobileNavigationDirections {
  private MobileNavigationDirections() {
  }

  @NonNull
  public static NavDirections actionGlobalConfig() {
    return new ActionOnlyNavDirections(R.id.action_global_config);
  }
}
