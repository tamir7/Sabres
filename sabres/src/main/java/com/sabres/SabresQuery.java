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
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class SabresQuery<T extends SabresObject> {
    private static final String TAG = SabresQuery.class.getSimpleName();
    private final String name;
    private final Class<T> clazz;
    private Where where;
    private final List<String> keyIndices = new ArrayList<>();
    private final List<String> includes = new ArrayList<>();

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

    public SabresQuery include(String key) {
        ObjectDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            throw new IllegalArgumentException(String.format("Unrecognized key %s in Object %s",
                    key, name));
        }

        if (descriptor.getType().equals(ObjectDescriptor.Type.Pointer)) {
            includes.add(key);
        } else {
            Log.w(TAG, String.format("keys of type %s are always included in query results",
                    descriptor.getType().toString()));
        }

        return this;
    }

    private String stringifyObject(Object object)  {
        if (object instanceof Number) {
            return String.valueOf(object);
        }

        if (object instanceof String) {
            return (String)object;
        }

        if (object instanceof Boolean) {
            return (Boolean)object ? "1" : "0";
        }

        if (object instanceof Date) {
            return String.valueOf(((Date) object).getTime());
        }

        if (object instanceof SabresObject) {
            return String.valueOf(((SabresObject)object).getObjectId());
        }

        throw new IllegalArgumentException(String.format("No rule to stringify Object of class %s",
                object.getClass().getSimpleName()));
    }

    public void whereEqualTo(String key, Object object) {
        keyIndices.add(key);
        addWhere(Where.equalTo(key, stringifyObject(object)));
    }

    private void addWhere(Where where) {
        if (this.where == null) {
            this.where = where;
        } else {
            this.where = this.where.and(where);
        }
    }

    private void checkTableExists(Sabres sabres) throws SabresException {
        if (!SqliteMaster.tableExists(sabres, name)){
            throw new SabresException(SabresException.OBJECT_NOT_FOUND,
                    String.format("table %s does not exist", name));
        }
    }

    public Task<List<T>> findInBackground() {
        return Task.callInBackground(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return find();
            }
        });
    }

    public void findInBackground(final FindCallback<T> callback) {
        findInBackground().continueWith(new Continuation<List<T>, Void>() {
            @Override
            public Void then(Task<List<T>> task) throws Exception {
                callback.done(task.getResult(), SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    static void createIndices(Sabres sabres, String name, List<String> keys)
            throws SabresException {
        CreateIndexCommand createIndexCommand =  new CreateIndexCommand(name, keys).ifNotExists();
        sabres.execSQL(createIndexCommand.toString());
    }

    public List<T> find() throws SabresException {
        Sabres sabres = Sabres.self();
        List<T> objects = new ArrayList<>();
        sabres.open();
        Cursor c = null;
        try {
            if (SqliteMaster.tableExists(sabres, name)) {
                createIndices(sabres, name, keyIndices);
                SelectCommand command = new SelectCommand(name, Schema.getKeys(name));
                for (String include: includes) {
                    ObjectDescriptor descriptor = Schema.getDescriptor(name, include);
                    if (descriptor != null &&
                            descriptor.getType().equals(ObjectDescriptor.Type.Pointer)) {
                        command.join(descriptor.getName(), include,
                                Schema.getKeys(descriptor.getName()));
                    }
                }

                c = sabres.select(command.where(where).toSql());
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    T object = createObjectInstance();
                    object.populate(c);
                    for (String include: includes) {
                        object.populateChild(c, include);
                    }

                    for (String key: Schema.getListsKeys(name)) {
                        object.populateList(key, SabresCollection.get(sabres, name, key).
                                select(sabres, object.getObjectId(),
                                        Schema.getDescriptor(name, key)));
                    }

                    objects.add(object);
                }
            }

            return objects;
        } finally {
            if (c != null) {
                c.close();
            }

            sabres.close();
        }
    }

    private T createObjectInstance() {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate class %s",
                    clazz.getSimpleName()), e);
        }
    }

    public T get(long objectId) throws SabresException {
        Sabres sabres = Sabres.self();
        sabres.open();
        try {
            checkTableExists(sabres);
            T instance = SabresObject.createWithoutData(name, objectId);
            instance.fetch(sabres);
            return instance;
        } finally {
            sabres.close();
        }
    }
}
