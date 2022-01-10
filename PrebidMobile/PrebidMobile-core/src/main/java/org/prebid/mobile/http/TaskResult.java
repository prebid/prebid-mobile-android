/*
 *    Copyright 2020-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.ResultCode;

public class TaskResult<T> {
    @Nullable
    private T result;
    @Nullable
    private ResultCode resultCode;
    @Nullable
    private Exception error;

    @Nullable
    public T getResult() {
        return result;
    }

    @Nullable
    public ResultCode getResultCode() {
        return resultCode;
    }

    @Nullable
    public Exception getError() {
        return error;
    }

    protected TaskResult(@NonNull T result) {
        this.result = result;
    }

    protected TaskResult(@NonNull ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    protected TaskResult(@NonNull Exception error) {
        this.error = error;
    }
}

