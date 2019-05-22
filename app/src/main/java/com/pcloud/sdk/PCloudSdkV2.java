package com.pcloud.sdk;

import com.pcloud.sdk.internal.InternalV2;
import com.pcloud.sdk.internal.Version;

public class PCloudSdkV2 {

    private PCloudSdkV2() {
        // Instances are not allowed.
    }

    /**
     * @return the version of this PCloud SDK
     */
    public static String versionName(){
        return Version.NAME;
    }

    /**
     * @return a new {@link ApiClient.Builder} instance.
     */
    public static ApiClient.Builder newClientBuilder(){
        return InternalV2.newBuilder();
    }
}
