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

package com.android.tv.settings.connectivity.setup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.android.tv.settings.connectivity.util.State;
import com.android.tv.settings.connectivity.util.StateMachine;

/**
 * State responsible for starting the network configuration.
 */
public class AddStartState implements State {
    private final FragmentActivity mActivity;
    private final StateMachine mStateMachine;
    private final UserChoiceInfo mUserChoiceInfo;
    private Fragment mFragment;

    public AddStartState(FragmentActivity activity) {
        mActivity = activity;
        mUserChoiceInfo = ViewModelProviders.of(mActivity).get(UserChoiceInfo.class);
        mStateMachine = ViewModelProviders.of(mActivity).get(StateMachine.class);
    }

    @Override
    public void processForward() {
        mFragment = null;
        mStateMachine.getListener().onComplete(
                mUserChoiceInfo.getWifiEntry().shouldEditBeforeConnect()
                        || mUserChoiceInfo.getWifiEntry().needsWifiConfiguration()
                ? StateMachine.PASSWORD : StateMachine.CONNECT);
    }

    @Override
    public void processBackward() {
        mStateMachine.back();
    }

    @Override
    public Fragment getFragment() {
        return mFragment;
    }
}
