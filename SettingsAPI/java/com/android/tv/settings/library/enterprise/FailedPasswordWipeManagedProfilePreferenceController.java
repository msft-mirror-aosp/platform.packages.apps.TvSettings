/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.settings.library.enterprise;

import android.content.Context;
import com.android.tv.settings.library.UIUpdateCallback;

public class FailedPasswordWipeManagedProfilePreferenceController
        extends FailedPasswordWipePreferenceControllerBase {
    private static final String KEY_FAILED_PASSWORD_WIPE_MANAGED_PROFILE =
            "failed_password_wipe_managed_profile";

    public FailedPasswordWipeManagedProfilePreferenceController(
            Context context, UIUpdateCallback callback, int stateIdentifier) {
        super(context, callback, stateIdentifier);
    }

    @Override
    protected int getMaximumFailedPasswordsBeforeWipe() {
        return mFeatureProvider.getMaximumFailedPasswordsBeforeWipeInManagedProfile();
    }

    @Override
    public String[] getPreferenceKey() {
        return new String[] {KEY_FAILED_PASSWORD_WIPE_MANAGED_PROFILE};
    }
}
