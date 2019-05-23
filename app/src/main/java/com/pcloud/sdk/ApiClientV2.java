package com.pcloud.sdk;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public interface ApiClientV2 extends ApiClient {

    public Call<FileLink> createFileLink(String path, DownloadOptions options);


    interface BuilderV2 extends Builder {

        BuilderV2 cacheV2(Cache cache);

        BuilderV2 connectionPoolV2(ConnectionPool connectionPool);

        BuilderV2 dispatcherV2(Dispatcher dispatcher);

        BuilderV2 withClientV2(OkHttpClient client);

        BuilderV2 readTimeoutV2(long timeout, TimeUnit timeUnit);

        BuilderV2 writeTimeoutV2(long timeout, TimeUnit timeUnit);

        BuilderV2 connectTimeoutV2(long timeout, TimeUnit timeUnit);

        BuilderV2 authenticatorV2(Authenticator authenticator);

        BuilderV2 callbackExecutorV2(Executor callbackExecutor);

        BuilderV2 progressCallbackThresholdV2(long bytes);

        ApiClientV2 createV2();
    }
}
