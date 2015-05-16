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
 * A CountCallback is used to run code after a SabresQuery is used to count objects matching a
 * query in a background thread.
 * <p>
 * The easiest way to use a CountCallback is through an anonymous inner class.
 * Override the done function to specify what the callback should do after the count is complete.
 * The done function will be run in the UI thread, while the count happens in a background thread.
 * This ensures that the UI does not freeze while the fetch happens.
 * <p>
 * For example, this sample code counts objects of class "MyClass".
 *
 * <pre>
 * {@code
 * SabresQuery<MyClass> query = SabresQuery.getQuery("MyClass.class);
 * query.countInBackground(new CountCallback() {
 *     public void done(int count, SabresException e) {
 *         if (e == null) {
 *             objectsWereCountedSuccessfully(count);
 *         } else {
 *             objectCountingFailed();
 *         }
 *     }
 * });
 * }
 * </pre>
 */
public interface CountCallback {
    /**
     * Override this function with the code you want to run after the count is complete.
     *
     * @param count The number of objects matching the query.
     * @param e     The exception raised by the count, or null if it succeeded.
     */
    void done(Long count, SabresException e);
}
