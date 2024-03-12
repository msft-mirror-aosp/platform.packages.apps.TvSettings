/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.tv.settings.accessibility;

import static com.android.tv.settings.util.InstrumentationUtils.logEntrySelected;

import android.app.tvsettings.TvSettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Keep;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import com.android.tv.settings.R;
import com.android.tv.settings.RadioPreference;
import com.android.tv.settings.SettingsPreferenceFragment;
import com.android.tv.settings.overlay.FlavorUtils;

@Keep
public class AccessibilityTimeoutFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String A11Y_TIMEOUT_GROUP = "a11y_timeout_group";

    private int mCurrentA11yTimeout;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.accessibility_timeout, null);
        PreferenceGroup a11yTimeoutGroup = (PreferenceGroup) findPreference(A11Y_TIMEOUT_GROUP);
        final Context themedContext = getPreferenceManager().getContext();
        final boolean isTwoPanel = FlavorUtils.isTwoPanel(getContext());
        final String[] entries =
                getContext().getResources().getStringArray(R.array.a11y_timeout_entries);
        final String[] entryValues =
                getContext().getResources().getStringArray(R.array.a11y_timeout_values);
        initA11yTimeoutValue();

        for (int i = 0; i < entries.length; i++) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setRadioGroup(A11Y_TIMEOUT_GROUP);
            radioPreference.setTitle(entries[i]);
            radioPreference.setKey(entryValues[i]);
            radioPreference.setOnPreferenceChangeListener(this);
            radioPreference.setPersistent(false);
            if (mCurrentA11yTimeout == Integer.parseInt(entryValues[i])) {
                radioPreference.setChecked(true);
            }
            if (isTwoPanel) {
                radioPreference.setFragment(AccessibilityTimeoutInfoFragment.class.getName());
            }
            a11yTimeoutGroup.addPreference(radioPreference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        RadioPreference radioPreference = (RadioPreference) preference;
        if (radioPreference.isChecked()) {
            return false;
        }
        PreferenceGroup a11yTimeoutGroup = (PreferenceGroup) findPreference(A11Y_TIMEOUT_GROUP);
        radioPreference.clearOtherRadioPreferences(a11yTimeoutGroup);
        mCurrentA11yTimeout = Integer.parseInt(radioPreference.getKey());
        commit();
        radioPreference.setChecked(true);
        logNewA11yTimeoutSelection(radioPreference.getKey());
        return true;
    }

    @Override
    protected int getPageId() {
        return TvSettingsEnums.SYSTEM_A11Y_TIMEOUT;
    }

    private void initA11yTimeoutValue() {
        final ContentResolver resolver = getContext().getContentResolver();
        mCurrentA11yTimeout =
                Settings.Secure.getInt(
                        resolver, Settings.Secure.ACCESSIBILITY_INTERACTIVE_UI_TIMEOUT_MS, 0);
    }

    private void commit() {
        final ContentResolver resolver = getContext().getContentResolver();
        Settings.Secure.putInt(
                resolver,
                Settings.Secure.ACCESSIBILITY_INTERACTIVE_UI_TIMEOUT_MS,
                mCurrentA11yTimeout);
        Settings.Secure.putInt(
                resolver,
                Settings.Secure.ACCESSIBILITY_NON_INTERACTIVE_UI_TIMEOUT_MS,
                mCurrentA11yTimeout);
    }

    private void logNewA11yTimeoutSelection(String entryValue) {
        final int[] a11yTimeoutOptions = {
            TvSettingsEnums.SYSTEM_A11Y_TIMEOUT_DEFAULT,
            TvSettingsEnums.SYSTEM_A11Y_TIMEOUT_TEN_SECONDS,
            TvSettingsEnums.SYSTEM_A11Y_TIMEOUT_THIRTY_SECONDS,
            TvSettingsEnums.SYSTEM_A11Y_TIMEOUT_ONE_MINUTE,
            TvSettingsEnums.SYSTEM_A11Y_TIMEOUT_TWO_MINUTE,
        };
        final String[] entryValues =
                getContext().getResources().getStringArray(R.array.a11y_timeout_values);
        for (int i = 0; i < entryValues.length; i++) {
            if (entryValue.equals(entryValues[i])) {
                logEntrySelected(a11yTimeoutOptions[i]);
                return;
            }
        }
    }
}
