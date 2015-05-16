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
 * A SaveCallback is used to run code after saving a SabresObject in a background thread.
 * <p>
 * The easiest way to use a SaveCallback is through an anonymous inner class.
 * Override the done function to specify what the callback should do after the save is complete.
 * The done function will be run in the UI thread, while the save happens in a background thread.
 * This ensures that the UI does not freeze while the save happens.
 *<p>
 * For example, this sample code saves the object myObject.
 * <pre>
 * {@code
 * myObject.saveInBackground(new SaveCallback() {
 *     public void done(SabresException e) {
 *         if (e == null) {
 *             myObjectWasSavedSuccessfully();
 *         } else {
 *             myObjectSaveDidNotSucceed();
 *         }
 *     }
 * });
 * }
 * </pre>
 */
public interface SaveCallback {
    /**
     * Override this function with the code you want to run after the save is complete.
     *
     * @param e The exception raised by the save, or null if it succeeded.
     */
    void done(SabresException e);
}
