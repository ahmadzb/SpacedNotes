/*
 * Copyright (c) 2017 pCloud AG
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
 *
 */

package com.pcloud.sdk.internal;

import com.pcloud.sdk.ProgressListener;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

class ExecutorProgressListener implements ProgressListener, Runnable{

    private ProgressListener delegate;
    private Executor executor;

    private volatile boolean pending;
    private volatile long done;
    private volatile long total;

    ExecutorProgressListener(ProgressListener delegate, Executor executor) {
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public void run() {
        delegate.onProgress(done, total);
        pending = false;
    }

    @Override
    public void onProgress(long done, long total) {
        if (!pending || done == total) {
            this.done = done;
            this.total = total;
            try {
                pending = true;
                executor.execute(this);
            } catch (RejectedExecutionException ignored) {
                pending = false;
            }
        }
    }
}
