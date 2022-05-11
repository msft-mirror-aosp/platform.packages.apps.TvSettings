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

package com.android.tv.settings.inputmethod;

import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.Preference;

import com.android.tv.settings.R;
import com.android.tv.settings.compat.HasKeys;
import com.android.tv.settings.compat.PreferenceControllerFragmentCompat;
import com.android.tv.settings.compat.RenderUtil;
import com.android.tv.settings.compat.TsPreference;
import com.android.tv.settings.compat.TsSwitchPreference;
import com.android.tv.settings.library.ManagerUtil;
import com.android.tv.settings.library.PreferenceCompat;

import java.util.List;

/**
 * Fragment compat for enabling/disabling virtual keyboard IMEs
 */
@Keep
public class AvailableVirtualKeyboardFragmentCompat extends PreferenceControllerFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.available_virtual_keyboard_compat, null);
    }

    @Override
    public void updateAllPref(List<PreferenceCompat> preferenceCompatList) {
        if (preferenceCompatList == null) {
            return;
        }
        preferenceCompatList.stream()
                .forEach(preferenceCompat -> updatePref(preferenceCompat));
    }

    @Override
    public HasKeys updatePref(PreferenceCompat pref) {
        if (pref == null) {
            return null;
        }
        String[] key = pref.getKey();
        Preference preference = findTargetPreference(key);
        if (preference == null) {
            if (pref.getType() == PreferenceCompat.TYPE_SWITCH) {
                preference = new TsSwitchPreference(getContext(), key);
            } else {
                preference = new TsPreference(getContext(), key);
            }
            getPreferenceScreen().addPreference(preference);
        }
        RenderUtil.updatePreference(
                getContext(), (HasKeys) preference, pref, preference.getOrder());
        if (pref.hasOnPreferenceChangeListener()) {
            preference.setOnPreferenceChangeListener(this);
        }
        return (HasKeys) preference;
    }

    @Override
    public int getStateIdentifier() {
        return ManagerUtil.STATE_AVAILABLE_KEYBOARD;
    }
}
