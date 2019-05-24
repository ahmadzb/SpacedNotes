package com.pcloud.sdk.internal.networking;

import java.io.IOException;

public class JSONParseException extends IOException {

    public JSONParseException(String message) {
        super(message);
    }

    public JSONParseException(String message, Exception e) {
        super(message, e);
    }
}
