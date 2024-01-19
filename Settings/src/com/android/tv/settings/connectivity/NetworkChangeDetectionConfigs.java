/*
 * Copyright (C) 2024 The Android Open Source Project
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
 * limitations under the License
 */
package com.android.tv.settings.connectivity;

import android.net.Uri;

/**
 * Helper class for maintaining all the values related to network change detection.
 */
public class NetworkChangeDetectionConfigs {

    public static final String ACTION_NETWORK_COUNT_CHANGED =
            "android.intent.action.NETWORK_COUNT_CHANGED";
    public static final String SHARED_PREFERENCES_NAME = "NetworkChangeSharedPreference";
    public static final String PREFERENCE_KEY = "total_network_count";
    public static final String READ_PERMISSION =
            "com.android.settings.connectivity.READ_NETWORK_CHANGE_DATA";
    public static final String AUTHORITY =
            "com.android.settings.connectivity.networkchangedetection";
    public static final Uri CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + PREFERENCE_KEY);

    private NetworkChangeDetectionConfigs() {}
}
