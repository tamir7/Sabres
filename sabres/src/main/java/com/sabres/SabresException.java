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

/**
 * A SabresException gets raised whenever a SabresObject issues an invalid request,
 * such as deleting or editing an object that no longer exists in the database.
 */
public class SabresException extends Exception {
    /**
     * Error code indicating that the SqliteDatabase received a command it can't process.
     */
    public static final int SQL_ERROR = 2;
    /**
     * Error code indicating a problem that is not covered by other error codes.
     */
    public static final int OTHER_CAUSE = 3;
    /**
     * Error code indicating the specified object doesn't exist.
     */
    public static final int OBJECT_NOT_FOUND = 4;

    private final int code;

    /**
     * Construct a new SabresException with a particular error code and message.
     *
     * @param code  The error code to identify the type of exception.
     * @param msg   A message describing the error in more detail.
     */
    public SabresException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * Construct a new SabresException with a particular error code, message and cause.
     * @param code      The error code to identify the type of exception.
     * @param msg       A message describing the error in more detail.
     * @param cause     The cause of the error.
     */
    public SabresException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
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

    /**
     * Access the code for this error.
     *
     * @return The numerical code for this error.
     */
    public int getErrorCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", getErrorCode(), super.getMessage());
    }
}
