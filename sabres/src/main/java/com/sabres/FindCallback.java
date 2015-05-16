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

import java.util.List;

/**
 * A FindCallback is used to run code after a SabresQuery is used to find objects matching a
 * query in a background thread.
 * <p>
 * The easiest way to use a FindCallback is through an anonymous inner class.
 * Override the done function to specify what the callback should do after the find is complete.
 * The done function will be run in the UI thread, while the find happens in a background thread.
 * This ensures that the UI does not freeze while find happens.
 * <p>
 * For example, this sample code finds objects of class "MyClass".
 *
 * <pre>
 * {@code
 * SabresQuery<MyClass> query = SabresQuery.getQuery("MyClass.class);
 * query.findInBackground(new FindCallback() {
 *     public void done(List<MyClass> objects, SabresException e) {
 *         if (e == null) {
 *             if (objects.isEmpty()) {
 *                 noObjectsWereFound();
 *             } else {
 *                 objectsWereFoundSuccessfully(objects);
 *             }
 *         } else {
 *             objectsFindFailed();
 *         }
 *     }
 * });
 * }
 * </pre>
 */
public interface FindCallback<T extends SabresObject> {
    /**
     * Override this function with the code you want to run after find is complete.
     * @param objects   The objects that were retrieved, or null if it did not succeed.
     * @param e         The exception raised by the find, or null if it succeeded.
     */
    void done(List<T> objects, SabresException e);
}
