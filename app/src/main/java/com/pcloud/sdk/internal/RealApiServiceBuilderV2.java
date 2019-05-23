package com.pcloud.sdk.internal;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiClientV2;
import com.pcloud.sdk.Authenticator;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public class RealApiServiceBuilderV2 extends RealApiServiceBuilder implements ApiClientV2.BuilderV2 {

    RealApiServiceBuilderV2(OkHttpClient okHttpClient, Executor callbackExecutor, long progressCallbackThresholdBytes, Authenticator authenticator) {
        super(okHttpClient, callbackExecutor, progressCallbackThresholdBytes, authenticator);
    }

    RealApiServiceBuilderV2() {
        super();
    }

    @Override
    public ApiClientV2.BuilderV2 cacheV2(Cache cache) {
        cache(cache);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 connectionPoolV2(ConnectionPool connectionPool) {
        connectionPool(connectionPool);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 dispatcherV2(Dispatcher dispatcher) {
        dispatcher(dispatcher);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 withClientV2(OkHttpClient client) {
        return cacheV2(client.cache())
                .connectionPoolV2(client.connectionPool())
                .dispatcherV2(client.dispatcher())
                .readTimeoutV2(client.readTimeoutMillis(), TimeUnit.MILLISECONDS)
                .writeTimeoutV2(client.writeTimeoutMillis(), TimeUnit.MILLISECONDS)
                .connectTimeoutV2(client.connectTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ApiClientV2.BuilderV2 readTimeoutV2(long timeout, TimeUnit timeUnit) {
        readTimeout(timeout, timeUnit);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 writeTimeoutV2(long timeout, TimeUnit timeUnit) {
        writeTimeout(timeout, timeUnit);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 connectTimeoutV2(long timeout, TimeUnit timeUnit) {
        connectTimeout(timeout, timeUnit);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 authenticatorV2(Authenticator authenticator) {
        authenticator(authenticator);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 callbackExecutorV2(Executor callbackExecutor) {
        callbackExecutor(callbackExecutor);
        return this;
    }

    @Override
    public ApiClientV2.BuilderV2 progressCallbackThresholdV2(long bytes) {
        progressCallbackThreshold(bytes);
        return this;
    }

    @Override
    public ApiClientV2 createV2() {
        return new RealApiClientV2(this);
    }
}
