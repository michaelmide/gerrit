// Copyright (C) 2019 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.extensions.common;

import com.google.gerrit.common.Nullable;

/**
 * A generic input with a message only.
 *
 * <p>See also {@link InputWithCommitMessage}.
 */
public class InputWithMessage {
  @Nullable public String message;

  public InputWithMessage() {
    this(null);
  }

  public InputWithMessage(@Nullable String message) {
    this.message = message;
  }
}
