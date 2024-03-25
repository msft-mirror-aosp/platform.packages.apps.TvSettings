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
 * limitations under the License.
 */
package com.android.tv.settings.connectivity.util;


import android.content.Context;
import android.content.pm.PackageManager;
import android.os.OutcomeReceiver;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.Executor;

/** Helper class to perform ThreadNetwork service related APIs using reflection. Once Android V
 * is available we can remove this and use native methods. See b/328038364 */
public class ThreadNetworkHelper {

    private static final String TAG = "ThreadNetworkHelper";
    private static final String THREAD_NETWORK_SERVICE_NAME = "thread_network";

    // StateCallback and methods
    private static final String STATE_CALLBACK_CLASS_NAME =
            "android.net.thread.ThreadNetworkController$StateCallback";
    private static final String ON_DEVICE_ROLE_CHANGED_METHOD_NAME = "onDeviceRoleChanged";
    private static final String ON_PARTITION_ID_CHANGED_METHOD_NAME = "onPartitionIdChanged";
    private static final String ON_THREAD_ENABLED_STATE_CHANGED = "onThreadEnableStateChanged";
    private static final String HASH_CODE_METHOD_NAME = "hashCode";
    private static final String EQUALS_METHOD_NAME = "equals";
    private static final String TO_STRING_METHOD_NAME = "toString";
    private static final int THREAD_NETWORK_ENABLED = 1;

    // ThreadNetworkManager method names
    private static final String GET_ALL_THREAD_NETWORK_CONTROLLERS =
            "getAllThreadNetworkControllers";

    // ThreadNetworkController method names
    private static final String REGISTER_STATE_CALLBACK_METHOD_NAME = "registerStateCallback";
    private static final String UNREGISTER_STATE_CALLBACK_METHOD_NAME =
            "unregisterStateCallback";
    private static final String SET_ENABLED_METOD_NAME = "setEnabled";

    private final Executor mExecutor;
    private final Class<?> mStateCallbackClass;
    private final Object mThreadNetworkController;
    private final Object mStateCallback;
    private final OutcomeReceiver<Void, ? extends Exception> mThreadNetworkOutComeReceiver =
            new OutcomeReceiver<>() {
                @Override
                public void onResult(Void result) {
                    // Do nothing, state is handled with mStateCallback
                }

                @Override
                public void onError(@NonNull Exception error) {
                    Log.w(TAG, error);
                }
            };
    private OnStateChangeListener mOnStateChangeListener;

    private ThreadNetworkHelper(Object threadNetworkController, Executor executor) {
        this.mThreadNetworkController = threadNetworkController;
        this.mExecutor = executor;
        this.mStateCallbackClass = getStateCallbackClass();
        this.mStateCallback = getStateCallback();
    }

    /** Creates new instance if Thread Network service is available or else null */
    public static ThreadNetworkHelper getInstance(Context context) {
        if (context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_THREAD_NETWORK)) {
            Object threadNetworkManager =
                    context.getSystemService(THREAD_NETWORK_SERVICE_NAME);
            if (threadNetworkManager != null) {
                try {
                    List<?> networkControllers =
                            (List<?>)threadNetworkManager.getClass()
                                    .getMethod(GET_ALL_THREAD_NETWORK_CONTROLLERS)
                                    .invoke(threadNetworkManager);
                    if (!networkControllers.isEmpty()) {
                        return new ThreadNetworkHelper(
                                networkControllers.get(0), context.getMainExecutor());
                    }
                } catch (IllegalAccessException | NoSuchMethodException |
                         InvocationTargetException e) {
                    Log.w(TAG, e);
                }
            }
        }
        return null;
    }
    private Class<?> getStateCallbackClass () {
        try {
            return Class.forName(STATE_CALLBACK_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "StateCallback not found " + e);
            return null;
        }
    }

    private Object getStateCallback() {
        return Proxy.newProxyInstance(mStateCallbackClass.getClassLoader(),
                new Class<?>[]{mStateCallbackClass}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        // Proxy requires to implement hashCode, equals, toString.
                        // Use default implementations from Object
                        case HASH_CODE_METHOD_NAME:
                            return System.identityHashCode(proxy);
                        case EQUALS_METHOD_NAME:
                            return proxy == args[0];
                        case TO_STRING_METHOD_NAME:
                            return proxy.getClass().getName() + "@" +
                                    Integer.toHexString(this.hashCode());
                        // Not used
                        case ON_DEVICE_ROLE_CHANGED_METHOD_NAME,
                                ON_PARTITION_ID_CHANGED_METHOD_NAME:
                            return null;
                        case ON_THREAD_ENABLED_STATE_CHANGED:
                            // There might be race condition when listener is detached so check
                            // if listener is available
                            if (mOnStateChangeListener != null) {
                                int enabledState = (int) args[0];
                                mOnStateChangeListener.isEnabled(
                                        enabledState == THREAD_NETWORK_ENABLED);
                            }
                            return null;
                        // Will throw error if other methods are added to interface
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    /** Registers the StateCallback to ThreadNetworkController */
    public void registerStateCallback() {
        try {
            mThreadNetworkController.getClass().getMethod(REGISTER_STATE_CALLBACK_METHOD_NAME,
                            Executor.class, mStateCallbackClass)
                    .invoke(mThreadNetworkController,
                            mExecutor,
                            mStateCallback);
        } catch (IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Log.w(TAG, e);
        }
    }

    /** Unregisters the StateCallback to ThreadNetworkController */
    public void unregisterStateCallback() {
        try {
            mThreadNetworkController.getClass()
                    .getMethod(UNREGISTER_STATE_CALLBACK_METHOD_NAME, mStateCallbackClass)
                    .invoke(mThreadNetworkController,
                            mStateCallback);
        } catch (IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Log.w(TAG, e);
        }
    }

    /** Enables/Disables Thread Network */
    public void setEnabled(boolean enable) {
        try {
            mThreadNetworkController.getClass().getMethod(SET_ENABLED_METOD_NAME,
                            boolean.class, Executor.class, OutcomeReceiver.class)
                    .invoke(mThreadNetworkController,
                            enable,
                            mExecutor,
                            mThreadNetworkOutComeReceiver);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
    }

    /** Sets the OnStateChangeListener */
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.mOnStateChangeListener = onStateChangeListener;
    }

    /** Listener to notify state of Thread Network*/
    public interface OnStateChangeListener {
        void isEnabled(boolean enabled);
    }
}
