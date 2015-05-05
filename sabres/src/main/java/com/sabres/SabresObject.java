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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

abstract public class SabresObject {
    private static final String TAG = SabresObject.class.getSimpleName();
    private static final String UNDEFINED = "(undefined)";
    static final String OBJECT_ID_KEY = "objectId";
    private static final String CREATED_AT_KEY = "createdAt";
    private static final String UPDATED_AT_KEY = "updatedAt";
    private final ContentValues values = new ContentValues();
    private final ContentValues dirtyValues = new ContentValues();
    private final Schema schema = new Schema();
    private final String name;
    private long id = 0;

    protected SabresObject() {
        name = getClass().getSimpleName();
    }

    public void put(String key, Object value) {
        if (value instanceof String) {
            schema.put(key, JavaType.String);
            values.put(key, (String)value);
            dirtyValues.put(key, (String)value);
        } else if (value instanceof Integer) {
            schema.put(key, JavaType.Integer);
            values.put(key, (Integer)value);
            dirtyValues.put(key, (Integer)value);
        } else if (value instanceof Date) {
            schema.put(key, JavaType.Date);
            values.put(key, ((Date)value).getTime());
            dirtyValues.put(key, ((Date)value).getTime());
        } else if (value instanceof Boolean) {
            schema.put(key, JavaType.Boolean);
            values.put(key, (Boolean)value);
            dirtyValues.put(key, (Boolean)value);
        } else if (value instanceof Long) {
            schema.put(key, JavaType.Long);
            values.put(key, (Long)value);
            dirtyValues.put(key, (Long)value);
        } else if (value instanceof Short) {
            schema.put(key, JavaType.Short);
            values.put(key, (Short)value);
            dirtyValues.put(key, (Short)value);
        } else if (value instanceof Byte) {
            schema.put(key, JavaType.Byte);
            values.put(key, (Byte) value);
            dirtyValues.put(key, (Byte) value);
        } else if (value instanceof Float) {
            schema.put(key, JavaType.Float);
            values.put(key, (Float) value);
            dirtyValues.put(key, (Float) value);
        } else if (value instanceof Double){
            schema.put(key, JavaType.Double);
            values.put(key, (Double) value);
            dirtyValues.put(key, (Double) value);
        } else {
            throw new IllegalArgumentException(String.format("Class %s is not supported",
                    value.getClass().getSimpleName()));
        }
    }

    public long getObjectId() {
        return id;
    }

    public String getString(String key) {
        return values.getAsString(key);
    }

    public Boolean getBoolean(String key)  {
        return values.getAsBoolean(key);
    }

    public Integer getInt(String key) {
        return values.getAsInteger(key);
    }

    public Byte getByte(String key) {
        return values.getAsByte(key);
    }

    public Short getShort(String key) {
        return values.getAsShort(key);
    }

    public Long getLong(String key) {
        return values.getAsLong(key);
    }

    public Float getFloat(String key) {
        return values.getAsFloat(key);
    }

    public Double getDouble(String key) {
        return values.getAsDouble(key);
    }

    public Date getDate(String key) {
        Long time = values.getAsLong(key);
        return time != null ? new Date(time) : null;
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

    // TODO: Build indices automatically.
    public void save() throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        sabres.beginTransaction();

        try {
            SchemaTable.create(sabres);
            Schema currentSchema = SchemaTable.select(sabres, name);
            if (currentSchema.isEmpty()) {
                put(CREATED_AT_KEY, new Date());
                put(UPDATED_AT_KEY, new Date());
                SchemaTable.insert(sabres, name, schema);
                createTable(sabres, schema);
            } else {
                put(UPDATED_AT_KEY, new Date());
                Schema newSchema = currentSchema.update(schema);
                if (!newSchema.isEmpty()) {
                    SchemaTable.insert(sabres, name, newSchema);
                    alterTable(sabres, newSchema);
                }
            }

            if (id == 0) {
                id = insert(sabres);
            } else {
                update(sabres);
            }

            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
            sabres.close();
        }
    }

    private void createTable(Sabres sabres, Schema schema) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(name).ifNotExists();
        createCommand.withColumn(new Column(OBJECT_ID_KEY, SqlType.Integer).primaryKey().notNull());
        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
            createCommand.withColumn(new Column(entry.getKey(), entry.getValue().toSqlType()));
        }

        sabres.execSQL(createCommand.toString());
    }

    private void alterTable(Sabres sabres, Schema schema) throws SabresException {
        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
            sabres.execSQL(new AlterTableCommand(name, new Column(entry.getKey(),
                    entry.getValue().toSqlType())).toString());
        }
    }

    private long insert(Sabres sabres) throws SabresException {
        long id = sabres.insert(name, dirtyValues);
        dirtyValues.clear();
        return id;
    }

    private void update(Sabres sabres) {
        sabres.update(name, dirtyValues, Where.equalTo(OBJECT_ID_KEY, id));
        dirtyValues.clear();
    }

    void populate(Cursor c, Schema schema) {
        id = CursorHelper.getLong(c, OBJECT_ID_KEY);
        this.schema.putAll(schema);
        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
            if (!c.isNull(c.getColumnIndex(entry.getKey()))) {
                switch (entry.getValue()) {
                    case Integer:
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
                    case Byte:
                        values.put(entry.getKey(), CursorHelper.getInt(c, entry.getKey()));
                        break;
                    case Short:
                        values.put(entry.getKey(), CursorHelper.getShort(c, entry.getKey()));
                        break;
                    case Long:
                        values.put(entry.getKey(), CursorHelper.getLong(c, entry.getKey()));
                        break;
                    case Boolean:
                        values.put(entry.getKey(), CursorHelper.getInt(c, entry.getKey()));
                        break;
                    case Date:
                        values.put(entry.getKey(), CursorHelper.getLong(c, entry.getKey()));
                        break;
                }
            }
        }
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

        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
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
