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
 * A GetCallback is used to run code after a SabresQuery is used to get a SabresObject in a
 * background thread.
 * <p>
 * The easiest way to use a GetCallback is through an anonymous inner class.
 * Override the done function to specify what the callback should do after the get is complete.
 * The done function will be run in the UI thread, while the get happens in a background thread.
 * This ensures that the UI does not freeze while the get happens.
 * <p>
 * For example, this sample code gets an object of class "MyClass" for id myId.
 * <pre>
 *{@code
 * SabresQuery<MyClass> query = ParseQuery.getQuery(MyClass.class);
 * query.getInBackground(myId, new GetCallback<MyClass>() {
 *     public void done(MyClass object, SabresException e) {
 *         if (e == null) {
 *             objectWasRetrievedSuccessfully(object);
 *         } else {
 *             objectRetrievalFailed();
 *         }
 *     }
 * });
 * }
 * </pre>
 */
public interface GetCallback<T extends SabresObject> {
    /**
     * Override this function with the code you want to run after get is complete.
     *
     * @param object The object that was retrieved, or null if it did not succeed.
     * @param e      The exception raised by the get, or null if it succeeded.
     */
    void done(T object, SabresException e);
}
