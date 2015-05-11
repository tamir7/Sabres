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

import com.jakewharton.fliptables.FlipTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

abstract public class SabresObject {
    private static final String TAG = SabresObject.class.getSimpleName();
    private static final String UNDEFINED = "(undefined)";
    private static final Map<String, Class<? extends SabresObject>> subClasses = new HashMap<>();
    private static final String OBJECT_ID_KEY = "objectId";
    private static final String CREATED_AT_KEY = "createdAt";
    private static final String UPDATED_AT_KEY = "updatedAt";
    private final Map<String, ObjectValue> values = new HashMap<>();
    private final Map<String, ObjectValue> dirtyValues = new HashMap<>();
    private final Map<String, ObjectDescriptor> schemaChanges = new HashMap<>();
    private final Map<String, SabresObject> children = new HashMap<>();
    private final Map<String, SabresObject> dirtyChildren = new HashMap<>();
    private boolean dataAvailable = false;
    private final String name;
    private long id = 0;

    protected SabresObject() {
        name = getClass().getSimpleName();
    }

    private static <T extends SabresObject> T createWithoutData(Class<? extends SabresObject> clazz, long id) {
        T object = createObjectInstance(clazz);
        object.setObjectId(id);
        return object;
    }

    void setObjectId(long objectId) {
        this.id = objectId;
    }

    static Set<String> getSubClassNames() {
        return subClasses.keySet();
    }

    static <T extends SabresObject> T createWithoutData(String className, long id) {
        return createWithoutData(subClasses.get(className), id);
    }

    public static void registerSubclass(Class<? extends SabresObject> subClass) {
        subClasses.put(subClass.getSimpleName(), subClass);
    }

    public void put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        ObjectDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            descriptor = ObjectDescriptor.fromObject(value);
            schemaChanges.put(key, descriptor);
        }

        if (descriptor.getType().equals(ObjectDescriptor.Type.Pointer)) {
            dirtyChildren.put(key, (SabresObject)value);
        } else {
            dirtyValues.put(key, new ObjectValue(value, descriptor));

        }
    }

    static String getObjectIdKey() {
        return OBJECT_ID_KEY;
    }

    private boolean isDirty() {
        return dirtyValues.size() != 0 || !dirtyChildren.isEmpty();
    }

    public long getObjectId() {
        return id;
    }

    private void checkDataAvailable() {
        if (!dataAvailable) {
            throw new IllegalStateException("No data associated with object," +
                    "call fetch to populate data from database");
        }
    }

    private <T> T get(String key, Class<T> clazz) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.get(key).get(clazz);
        }

        checkDataAvailable();
        if (values.containsKey(key)) {
            return values.get(key).get(clazz);
        }

        return null;
    }

    public String getString(String key) {
        return get(key, String.class);
    }


    public Boolean getBoolean(String key)  {
        return get(key, Boolean.class);
    }

    public Integer getInt(String key) {
        return get(key, Integer.class);
    }

    public Byte getByte(String key) {
        return get(key, Byte.class);
    }

    public Short getShort(String key) {
        return get(key, Short.class);
    }

    public Long getLong(String key) {
        return get(key, Long.class);
    }

    public Float getFloat(String key) {
        return get(key, Float.class);
    }

    public Double getDouble(String key) {
        return get(key, Double.class);
    }

    public Date getDate(String key) {
        return get(key, Date.class);
    }

    public SabresObject getSabresObject(String key) {
        if (dirtyChildren.containsKey(key)) {
            return dirtyChildren.get(key);
        }

        checkDataAvailable();
        return children.get(key);
    }

    public Task<Void> saveInBackground() {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                save();
                return null;
            }
        });
    }

    public void saveInBackground(final SaveCallback callback) {
        saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    private void saveIfNeededInTransaction(Sabres sabres) throws SabresException {
        if (id == 0 || isDirty()) {
            saveInTransaction(sabres);
        }
    }

    private void saveInTransaction(Sabres sabres) throws SabresException {
        if (id == 0) {
            createObject(sabres);
        } else {
            updateObject(sabres);
        }
        dataAvailable = true;
    }

    public void save() throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        sabres.beginTransaction();

        try {
            saveInTransaction(sabres);
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
            sabres.close();
        }
    }

    private void updateSchema(Sabres sabres) throws SabresException {
        Map<String, ObjectDescriptor> schema = Schema.getSchema(name);
        if (schema == null) {
            Schema.update(sabres, name, schemaChanges);
            createTable(sabres);
        } else {
            Schema.update(sabres, name, schemaChanges);
            alterTable(sabres);

        }
    }

    private void createObject(Sabres sabres) throws SabresException {
        put(CREATED_AT_KEY, new Date());
        put(UPDATED_AT_KEY, new Date());
        updateSchema(sabres);
        id = insert(sabres);
    }

    private void updateObject(Sabres sabres) throws SabresException {
        put(UPDATED_AT_KEY, new Date());
        updateSchema(sabres);
        update(sabres);
    }

    private void createTable(Sabres sabres) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(name).ifNotExists();
        createCommand.withColumn(new Column(OBJECT_ID_KEY, SqlType.Integer).primaryKey().notNull());
        for (Map.Entry<String, ObjectDescriptor> entry: schemaChanges.entrySet()) {
            Column column = new Column(entry.getKey(), entry.getValue().toSqlType());
            if (entry.getValue().getType().equals(ObjectDescriptor.Type.Pointer)) {
                column.foreignKeyIn(entry.getValue().getName());
            }

            createCommand.withColumn(column);
        }

        sabres.execSQL(createCommand.toString());
    }

    private void alterTable(Sabres sabres) throws SabresException {
        for (Map.Entry<String, ObjectDescriptor> entry: schemaChanges.entrySet()) {
            sabres.execSQL(new AlterTableCommand(name, new Column(entry.getKey(),
                    entry.getValue().toSqlType())).toString());
        }
    }

    private void updateChildren(Sabres sabres) throws SabresException {
        for (Map.Entry<String, SabresObject> entry: dirtyChildren.entrySet()) {
            entry.getValue().saveIfNeededInTransaction(sabres);
            dirtyValues.put(entry.getKey(), new ObjectValue(entry.getValue().getObjectId(),
                    new ObjectDescriptor(ObjectDescriptor.Type.Pointer,
                            entry.getValue().getClass().getSimpleName())));
        }
    }

    private long insert(Sabres sabres) throws SabresException {
        updateChildren(sabres);
        long id = sabres.insert(new InsertCommand(name, dirtyValues).toSql());
        values.putAll(dirtyValues);
        children.putAll(dirtyChildren);
        dirtyValues.clear();
        dirtyChildren.clear();
        return id;
    }

    private void update(Sabres sabres) throws SabresException {
        updateChildren(sabres);
        UpdateCommand command = new UpdateCommand(name, dirtyValues);
        command.where(Where.equalTo(OBJECT_ID_KEY, id));
        sabres.execSQL(command.toSql());
        values.putAll(dirtyValues);
        children.putAll(dirtyChildren);
        dirtyValues.clear();
        dirtyChildren.clear();
    }

    void fetch(Sabres sabres) throws SabresException {
        Cursor c = null;
        try {
            SelectCommand command = new SelectCommand(name, Schema.getKeys(name)).
                    where(Where.equalTo(OBJECT_ID_KEY, id));
            c = sabres.select(command.toSql());
            if (!c.moveToFirst()) {
                throw new SabresException(SabresException.OBJECT_NOT_FOUND,
                        String.format("table %s has no object with key %s", name, id));
            }
            populate(c);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public void fetch() throws SabresException {
        Sabres sabres = Sabres.self();
        sabres.open();
        try {
            fetch(sabres);
        } finally {
            sabres.close();
        }
    }

    public Task<Void> fetchInBackground() {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                fetch();
                return null;
            }
        });
    }

    public void fetchInBackground(final FetchCallback callback)  {
        fetchInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    void populate(Cursor c) {
        id = CursorHelper.getLong(c, OBJECT_ID_KEY);
        Map<String, ObjectDescriptor> schema = Schema.getSchema(name);

        for (Map.Entry<String, ObjectDescriptor> entry: schema.entrySet()) {
            if (!c.isNull(c.getColumnIndex(entry.getKey()))) {
                Object value = null;
                switch (entry.getValue().getType()) {
                    case Integer:
                        value = CursorHelper.getInt(c, entry.getKey());
                        break;
                    case Boolean:
                        value = CursorHelper.getBoolean(c, entry.getKey());
                        break;
                    case Byte:
                        value = CursorHelper.getByte(c, entry.getKey());
                        break;
                    case Double:
                        value = CursorHelper.getDouble(c, entry.getKey());
                        break;
                    case Float:
                        value = CursorHelper.getFloat(c, entry.getKey());
                        break;
                    case String:
                        value = CursorHelper.getString(c, entry.getKey());
                        break;
                    case Short:
                        value = CursorHelper.getShort(c, entry.getKey());
                        break;
                    case Long:
                        value = CursorHelper.getLong(c, entry.getKey());
                        break;
                    case Date:
                        value = CursorHelper.getDate(c, entry.getKey());
                        break;
                    case Pointer:
                        value = CursorHelper.getLong(c, entry.getKey());
                        children.put(entry.getKey(),
                                createWithoutData(subClasses.get(entry.getValue().getName()),
                                        CursorHelper.getLong(c, entry.getKey())));
                        break;
                }

                if (value != null) {
                    values.put(entry.getKey(), new ObjectValue(value, entry.getValue()));
                }
            }
        }

        dataAvailable = true;
    }

   private String stringify(String key) {
       if (!values.containsKey(key)) {
           return UNDEFINED;
       }

       return values.get(key).toString();
   }

    private static String toString(String name, List<SabresObject> objects) {
        Map<String, ObjectDescriptor> schema = Schema.getSchema(name);
        String[] headers = new String[schema.size() + 1];
        String[][] data = new String[objects.size()][schema.size() + 1];
        int i = 0;
        int j = 1;
        headers[0] = "objectId(String)";
        for (SabresObject o: objects) {
            data[i++][0] = String.valueOf(o.getObjectId());
        }

        for (Map.Entry<String, ObjectDescriptor> entry: schema.entrySet()) {
            headers[j] = String.format("%s(%s)", entry.getKey(), entry.getValue().toString());
            i = 0;
            for (SabresObject o: objects) {
                data[i++][j] = o.stringify(entry.getKey());
            }
            j++;
        }
        return FlipTable.of(headers, data);
    }

    @Override
    public String toString() {
        return SabresObject.toString(name, Collections.singletonList(this));
    }

    private static <T extends SabresObject> T createObjectInstance(Class<? extends SabresObject> clazz) {
        try {
            //noinspection unchecked
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate class %s",
                    clazz.getSimpleName()), e);
        }
    }

    public static <T extends SabresObject> void printAll(final Class<T> clazz) {
        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Sabres sabres = Sabres.self();
                sabres.open();
                Cursor c = null;
                try {
                    if (SqliteMaster.tableExists(sabres, clazz.getSimpleName())) {
                        c = sabres.select(new SelectCommand(clazz.getSimpleName(),
                                Schema.getKeys(clazz.getSimpleName())).toSql());
                        List<SabresObject> objects = new ArrayList<>();
                        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                            T object = SabresObject.createObjectInstance(clazz);
                            object.populate(c);
                            objects.add(object);
                        }

                        return SabresObject.toString(clazz.getSimpleName(), objects);
                    } else {
                        return String.format("Class %s does not exist", clazz.getSimpleName());
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                    sabres.close();
                }
            }
        }).continueWith(new Continuation<String, Void>() {
            @Override
            public Void then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, String.format("Failed to print table %s", clazz.getSimpleName()),
                            task.getError());
                } else {
                    Log.i(TAG, String.format("%s:\n%s", clazz.getSimpleName(), task.getResult()));
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void delete() throws SabresException {
        Sabres sabres = Sabres.self();
        sabres.open();
        sabres.execSQL(new DeleteCommand(name).where(Where.equalTo(OBJECT_ID_KEY, id)).toSql());
        sabres.close();
    }

    public Task<Void> deleteInBackground() {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                delete();
                return null;
            }
        });
    }

    public void deleteInBackground(final DeleteCallback callback) {
        deleteInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        });
    }
}
