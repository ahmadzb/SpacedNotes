package com.pcloud.sdk.internal;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Authenticator;

import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;

public class RealApiServiceBuilderV2 extends RealApiServiceBuilder {

    RealApiServiceBuilderV2(OkHttpClient okHttpClient, Executor callbackExecutor, long progressCallbackThresholdBytes, Authenticator authenticator) {
        super(okHttpClient, callbackExecutor, progressCallbackThresholdBytes, authenticator);
    }

    RealApiServiceBuilderV2() {
        super();
    }

    @Override
    public ApiClient create() {
        return new RealApiClientV2(this);
    }
}
