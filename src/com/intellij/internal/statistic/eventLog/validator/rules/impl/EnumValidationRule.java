// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.internal.statistic.eventLog.validator.rules.impl;

import com.intellij.internal.statistic.eventLog.StatisticsEventEscaper;
import com.intellij.internal.statistic.eventLog.util.ValidatorContainerUtil;
import com.intellij.internal.statistic.eventLog.validator.ValidationResultType;
import com.intellij.internal.statistic.eventLog.validator.rules.EventContext;
import com.intellij.internal.statistic.eventLog.validator.rules.FUSRegexpAwareRule;
import com.intellij.internal.statistic.eventLog.validator.rules.PerformanceCareRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.intellij.internal.statistic.eventLog.validator.ValidationResultType.*;

public class EnumValidationRule extends PerformanceCareRule implements FUSRegexpAwareRule {
  private final Collection<String> myEnumValues;

  public EnumValidationRule(@Nullable Collection<String> strings) {
    myEnumValues = strings == null ? Collections.emptySet() : ValidatorContainerUtil.unmodifiableOrEmptyCollection(strings);
  }

  @NotNull
  @Override
  public ValidationResultType doValidate(@NotNull String data, @NotNull EventContext context) {
    if (myEnumValues.isEmpty()) return INCORRECT_RULE;

    String escaped = StatisticsEventEscaper.escapeEventIdOrFieldValue(data);
    if (myEnumValues.contains(escaped)) {
      return ACCEPTED;
    }

    // for backward compatibility with rules created before allowed symbols were changed
    String legacyData = StatisticsEventEscaper.cleanupForLegacyRulesIfNeeded(escaped);
    return legacyData != null && myEnumValues.contains(legacyData) ? ACCEPTED : REJECTED;
  }

  @NotNull
  @Override
  public String asRegexp() {
    return myEnumValues.stream()
      .map(s -> RegexpValidationRule.escapeText(s))
      .collect(Collectors.joining("|"));
  }

  @Override
  public String toString() {
    return "EnumValidationRule: myEnumValues=" + asRegexp();
  }

}
