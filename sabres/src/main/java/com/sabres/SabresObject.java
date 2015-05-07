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

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.jakewharton.fliptables.FlipTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

abstract public class SabresObject {
    private static final String TAG = SabresObject.class.getSimpleName();
    private static final String UNDEFINED = "(undefined)";
    private static final Map<String, Class<? extends SabresObject>> subClasses = new HashMap<>();
    static final String OBJECT_ID_KEY = "objectId";
    private static final String CREATED_AT_KEY = "createdAt";
    private static final String UPDATED_AT_KEY = "updatedAt";
    private final ContentValues values = new ContentValues();
    private final ContentValues dirtyValues = new ContentValues();
    private final Schema schema = new Schema();
    private final Map<String, SabresObject> children = new HashMap<>();
    private final Map<String, SabresObject> dirtyChildren = new HashMap<>();
    private boolean dataAvailable = false;
    private final String name;
    protected long id = 0;

    protected SabresObject() {
        name = getClass().getSimpleName();
    }

    private static <T extends SabresObject> T createWithoutData(Class<T> clazz, long id) {
        T object = createObjectInstance(clazz);
        object.id = id;
        return object;
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

        if (value instanceof String) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.String));
            dirtyValues.put(key, (String)value);
        } else if (value instanceof Integer) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Integer));
            dirtyValues.put(key, (Integer)value);
        } else if (value instanceof Date) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Date));
            dirtyValues.put(key, ((Date)value).getTime());
        } else if (value instanceof Boolean) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Boolean));
            dirtyValues.put(key, (Boolean)value);
        } else if (value instanceof Long) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Long));
            dirtyValues.put(key, (Long)value);
        } else if (value instanceof Short) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Short));
            dirtyValues.put(key, (Short)value);
        } else if (value instanceof Byte) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Byte));
            dirtyValues.put(key, (Byte) value);
        } else if (value instanceof Float) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Float));
            dirtyValues.put(key, (Float) value);
        } else if (value instanceof Double){
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Double));
            dirtyValues.put(key, (Double) value);
        } else if (value instanceof SabresObject) {
            schema.put(key, new ObjectDescriptor(ObjectDescriptor.Type.Pointer,
                    value.getClass().getSimpleName()));
            dirtyChildren.put(key, (SabresObject) value);
        } else {
            throw new IllegalArgumentException(String.format("Class %s is not supported",
                    value.getClass().getSimpleName()));
        }
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

    public String getString(String key) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsString(key);
        }

        checkDataAvailable();
        return values.getAsString(key);
    }


    public Boolean getBoolean(String key)  {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsBoolean(key);
        }

        checkDataAvailable();
        return values.getAsBoolean(key);
    }

    public Integer getInt(String key) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsInteger(key);
        }

        checkDataAvailable();
        return values.getAsInteger(key);
    }

    public Byte getByte(String key) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsByte(key);
        }

        checkDataAvailable();
        return values.getAsByte(key);
    }

    public Short getShort(String key) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsShort(key);
        }

        checkDataAvailable();
        return values.getAsShort(key);
    }

    public Long getLong(String key) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsLong(key);
        }

        checkDataAvailable();
        return values.getAsLong(key);
    }

    public Float getFloat(String key) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsFloat(key);
        }

        checkDataAvailable();
        return values.getAsFloat(key);
    }

    public Double getDouble(String key) {
        if (dirtyValues.containsKey(key)) {
            return dirtyValues.getAsDouble(key);
        }

        checkDataAvailable();
        return values.getAsDouble(key);
    }

    public Date getDate(String key) {
        if (dirtyValues.containsKey(key)) {
            return new Date(dirtyValues.getAsLong(key));
        }

        checkDataAvailable();
        return values.containsKey(key) ? new Date(values.getAsLong(key)) : null;
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
        SchemaTable.create(sabres);
        Schema currentSchema = SchemaTable.select(sabres, name);
        if (currentSchema.isEmpty()) {
            SchemaTable.insert(sabres, name, schema);
            createTable(sabres, schema);
        } else {
            Schema newSchema = currentSchema.createDiffSchema(schema);
            if (!newSchema.isEmpty()) {
                SchemaTable.insert(sabres, name, newSchema);
                alterTable(sabres, newSchema);
            }
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

    private void createTable(Sabres sabres, Schema schema) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(name).ifNotExists();
        createCommand.withColumn(new Column(OBJECT_ID_KEY, SqlType.Integer).primaryKey().notNull());
        for (Map.Entry<String, ObjectDescriptor> entry: schema.getObjectDescriptors().entrySet()) {
            createCommand.withColumn(new Column(entry.getKey(), entry.getValue().toSqlType()));
        }

        sabres.execSQL(createCommand.toString());
    }

    private void alterTable(Sabres sabres, Schema schema) throws SabresException {
        for (Map.Entry<String, ObjectDescriptor> entry: schema.getObjectDescriptors().entrySet()) {
            sabres.execSQL(new AlterTableCommand(name, new Column(entry.getKey(),
                    entry.getValue().toSqlType())).toString());
        }
    }

    private void updateChildren(Sabres sabres) throws SabresException {
        for (Map.Entry<String, SabresObject> entry: dirtyChildren.entrySet()) {
            entry.getValue().saveInTransaction(sabres);
            dirtyValues.put(entry.getKey(), entry.getValue().getObjectId());
        }
    }

    private long insert(Sabres sabres) throws SabresException {
        updateChildren(sabres);
        long id = sabres.insert(name, dirtyValues);
        values.putAll(dirtyValues);
        children.putAll(dirtyChildren);
        dirtyValues.clear();
        dirtyChildren.clear();
        return id;
    }

    private void update(Sabres sabres) throws SabresException {
        updateChildren(sabres);
        sabres.update(name, dirtyValues, Where.equalTo(OBJECT_ID_KEY, id));
        values.putAll(dirtyValues);
        children.putAll(dirtyChildren);
        dirtyValues.clear();
        dirtyChildren.clear();
    }

    <T extends SabresObject> void populate(Cursor c, Schema schema) {
        id = CursorHelper.getLong(c, OBJECT_ID_KEY);
        this.schema.putAll(schema);
        for (Map.Entry<String, ObjectDescriptor> entry: schema.getObjectDescriptors().entrySet()) {
            if (!c.isNull(c.getColumnIndex(entry.getKey()))) {
                switch (entry.getValue().getType()) {
                    case Integer:
                    case Boolean:
                    case Byte:
                        values.put(entry.getKey(), CursorHelper.getInt(c, entry.getKey()));
                        break;
                    case Double:
                        values.put(entry.getKey(), CursorHelper.getDouble(c, entry.getKey()));
                        break;
                    case Float:
                        values.put(entry.getKey(), CursorHelper.getFloat(c, entry.getKey()));
                        break;
                    case String:
                        values.put(entry.getKey(), CursorHelper.getString(c, entry.getKey()));
                        break;
                    case Short:
                        values.put(entry.getKey(), CursorHelper.getShort(c, entry.getKey()));
                        break;
                    case Long:
                    case Date:
                        values.put(entry.getKey(), CursorHelper.getLong(c, entry.getKey()));
                        break;
                    case Pointer:
                        children.put(entry.getKey(),
                                createWithoutData(subClasses.get(entry.getValue().getName()),
                                CursorHelper.getLong(c, entry.getKey())));
                        break;
                }
            }
        }

        dataAvailable = true;
    }

   private String stringify(String key) {
       if (!values.containsKey(key)) {
           return UNDEFINED;
       }

       switch (schema.getType(key)) {
           case Integer:
           case Double:
           case Float:
           case String:
           case Byte:
           case Short:
           case Long:
           case Pointer:
               return values.get(key).toString();
           case Boolean:
               return values.getAsInteger(key) == 0 ? String.valueOf(false) : String.valueOf(true);
           case Date:
               return new Date(values.getAsLong(key)).toString();
       }

       throw new IllegalStateException(String.format("No rule to stringify %s object",
               schema.getType(key)));
   }

    private static String toString(Schema schema, List<SabresObject> objects) {
        String[] headers = new String[schema.size() + 1];
        String[][] data = new String[objects.size()][schema.size() + 1];
        int i = 0;
        int j = 1;
        headers[0] = "objectId(String)";
        for (SabresObject o: objects) {
            data[i++][0] = String.valueOf(o.getObjectId());
        }

        for (Map.Entry<String, ObjectDescriptor> entry: schema.getObjectDescriptors().entrySet()) {
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
        return SabresObject.toString(schema, Collections.singletonList(this));
    }

    private static <T extends SabresObject> T createObjectInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
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
                    if (Sabres.tableExists(sabres, clazz.getSimpleName())) {
                        c = sabres.select(clazz.getSimpleName(), null);
                        Schema schema = SchemaTable.select(sabres, clazz.getSimpleName());
                        List<SabresObject> objects = new ArrayList<>();
                        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                            T object = SabresObject.createObjectInstance(clazz);
                            object.populate(c, schema);
                            objects.add(object);
                        }

                        return SabresObject.toString(schema, objects);
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
        sabres.delete(name, Where.equalTo(OBJECT_ID_KEY, id));
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
