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

import android.database.Cursor;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class SabresQuery<T extends SabresObject> {
    private final String name;
    private final Class<T> clazz;

    public SabresQuery(Class<T> clazz) {
        this.clazz = clazz;
        name = clazz.getSimpleName();
    }

    public static <T extends SabresObject> SabresQuery<T> getQuery(Class<T> clazz) {
        return new SabresQuery<>(clazz);
    }

    public Task<T> getInBackground(final long objectId) {
        return Task.callInBackground(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return get(objectId);
            }
        });
    }

    public void getInBackground(long objectId, final GetCallback<T> callback) {
        getInBackground(objectId).continueWith(new Continuation<T, Object>() {
            @Override
            public Object then(Task<T> task) throws Exception {
                callback.done(task.getResult(), SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    public T get(long objectId) throws SabresException {
        Sabres sabres = Sabres.self();
        sabres.open();
        if (!Sabres.tableExists(sabres, name)) {
            throw new SabresException(SabresException.OBJECT_NOT_FOUND,
                    String.format("table %s does not exist", name));
        }
            Schema schema = SchemaTable.select(sabres, name);
            Cursor c = sabres.select(name, Where.equalTo(SabresObject.OBJECT_ID_KEY, objectId));
            try {
                if (c == null || c.getCount() == 0) {
                    throw new SabresException(SabresException.OBJECT_NOT_FOUND,
                            String.format("table %s has no object with key %s", name, objectId));
                }

                c.moveToFirst();

                T instance;
                try {
                    instance = clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Failed to instantiate class %s",
                            clazz.getSimpleName()), e);
                }

                instance.populate(c, schema);
                return instance;

        } finally {
            if (c != null) {
                c.close();
            }
            sabres.close();
        }
    }
}
