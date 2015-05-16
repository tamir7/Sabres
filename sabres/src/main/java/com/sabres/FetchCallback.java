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
 * A FetchCallback is used to run code after fetching a SabresObject in a background thread.
 * <p>
 * The easiest way to use a FetchCallback is through an anonymous inner class.
 * Override the done function to specify what the callback should do after the fetch is complete.
 * The done function will be run in the UI thread, while the fetch happens in a background thread.
 * This ensures that the UI does not freeze while the fetch happens.
 *
 * For example, this sample code fetches the object myObject.
 * <pre>
 * {@code
 * myObject.fetchInBackground(new FetchCallback() {
 *     public void done(SabresException e) {
 *         if (e == null) {
 *             myObjectWasFetchedSuccessfully();
 *         } else {
 *             myObjectFetchDidNotSucceed();
 *         }
 *     }
 * });
 * }
 * </pre>
 */
public interface FetchCallback {
    /**
     * Override this function with the code you want to run after fetch is complete.
     *
     * @param e     The exception raised by fetch or null if successful.
     */
    void done(SabresException e);
}
