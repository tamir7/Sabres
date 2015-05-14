/*
 * Copyright 2015 Tamir Shomer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sabres;

public class SabresException extends Exception {
    public static final int SQL_ERROR = 2;
    public static final int OTHER_CAUSE = 3;
    public static final int OBJECT_NOT_FOUND = 4;

    private final int code;

    public SabresException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public SabresException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    public int getErrorCode() {
        return code;
    }

    static SabresException construct(Exception e) {
        if (e == null) {
            return null;
        }

        if (e instanceof SabresException) {
            return (SabresException)e;
        }

        return new SabresException(OTHER_CAUSE, e.getMessage(), e);
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", getErrorCode(), super.getMessage());
    }
}
