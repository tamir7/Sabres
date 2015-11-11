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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import bolts.Continuation;
import bolts.Task;

/**
 * The SabresObject is a base class that allows custom objects to be saved and retrieved with the
 * Sabres library.
 * <p>
 * To start working with SabresObject and the Sabres library, first a model class needs to be
 * created. Getters ans Setters for custom data can also be provided.
 * <pre>
 * {@code
 * public class MyObject extends SabresObject {
 *     private static final String MY_INT_KEY = "myInt";
 *     private static final String MY_STRING_KEY = "myString";
 *
 *     public void setMyInt(int myInt) {
 *         put(MY_INT_KEY, myInt);
 *     }
 *
 *     public void setMyString(String myString) {
 *         put(MY_STRING_KEY, myString);
 *     }
 *
 *     public Integer getMyInt() {
 *         return getInt(MY_INT_KEY);
 *     }
 *
 *     public String getMyString() {
 *         return getString(MY_STRING_KEY);
 *     }
 * }
 * }
 * </pre>
 * <p>
 * Before the object can be used, it needs to be registered with the SabresLibrary.
 * See {@link #registerSubclass(Class)}
 * <p>
 * Now, MyObject can be created, and saved to the database.
 * <p>
 * <pre>
 * {@code
 * MyObject myObject = new MyObject();
 * myObject.setMyInt(7);
 * myObject.setMyString("someFunString");
 * myObject.saveInBackground();
 * }
 * </pre>
 * <p>
 * To get objects from the database, the {@link SabresQuery} object is used.
 */
abstract public class SabresObject {
    private static final String TAG = SabresObject.class.getSimpleName();
    private static final String UNDEFINED = "(undefined)";
    private static final Map<String, Class<? extends SabresObject>> subClasses = new HashMap<>();
    private static final Map<String, Object> locks = new HashMap<>();
    private static final String OBJECT_ID_KEY = "objectId";
    private static final String CREATED_AT_KEY = "createdAt";
    private static final String UPDATED_AT_KEY = "updatedAt";
    private final Map<String, SabresValue> values = new HashMap<>();
    private static final Map<String, Map<String, SabresDescriptor>> schemaChanges =
        new ConcurrentHashMap<>();
    private final Set<String> dirtyKeys = new HashSet<>();
    private final String name;
    private boolean dataAvailable = false;
    private long id = 0;

    protected SabresObject() {
        name = getClass().getSimpleName();
    }

    /**
     * Creates a reference to an existing SabresObject for use in creating associations between
     * SabresObjects. Calling {@link #isDataAvailable()} on this object will return false until
     * {@link #fetch()} has been called.
     *
     * @param clazz The SabresObject subclass to create.
     * @param id    The object id for the referenced object.
     * @return A SabresObject without data.
     */
    public static <T extends SabresObject> T createWithoutData(Class<? extends SabresObject> clazz,
        long id) {
        T object = createObjectInstance(clazz);
        object.setObjectId(id);
        return object;
    }

    /**
     * Creates a new SabresObject based upon a subclass type.
     *
     * @param clazz The class of object to create.
     * @return A new SabresObject based upon the class name of the given subclass type.
     */
    public static <T extends SabresObject> T create(Class<T> clazz) {
        return createObjectInstance(clazz);
    }

    static Set<String> getSubClassNames() {
        return subClasses.keySet();
    }

    static <T extends SabresObject> T createWithoutData(String className, long id) {
        return createWithoutData(subClasses.get(className), id);
    }

    /**
     * Registers a custom subclass type with Sabres library.
     * <p>
     * Needs to be called before {@link Sabres#initialize}.
     * <p>
     * <pre>
     * {@code
     * public class MyApplication extends Application {
     *     public void onCreate() {
     *         super.onCreate();
     *         SabresObject.registerSubclass(MyObject1.class);
     *         SabresObject.registerSubclass(MyObject2.class);
     *         Sabres.initialize(this);
     *     }
     * }
     * }
     * </pre>
     *
     * @param subClass The subclass type to register.
     */
    public static void registerSubclass(Class<? extends SabresObject> subClass) {
        subClasses.put(subClass.getSimpleName(), subClass);
        locks.put(subClass.getSimpleName(), new Object());
    }

    public static String getObjectIdKey() {
        return OBJECT_ID_KEY;
    }

    private static String toString(String name, List<SabresObject> objects) {
        Map<String, SabresDescriptor> schema = Schema.getSchema(name);
        String[] headers = new String[schema.size() + 1];
        String[][] data = new String[objects.size()][schema.size() + 1];
        int i = 0;
        int j = 1;
        headers[0] = "objectId(String)";
        for (SabresObject o : objects) {
            data[i++][0] = String.valueOf(o.getObjectId());
        }

        for (Map.Entry<String, SabresDescriptor> entry : schema.entrySet()) {
            headers[j] = String.format("%s(%s)", entry.getKey(), entry.getValue().toString());
            i = 0;
            for (SabresObject o : objects) {
                data[i++][j] = o.stringify(entry.getKey());
            }
            j++;
        }
        return FlipTable.of(headers, data);
    }

    private static <T extends SabresObject> T createObjectInstance(Class<? extends SabresObject>
        clazz) {
        try {
            //noinspection unchecked
            return (T)clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate class %s",
                clazz.getSimpleName()), e);
        }
    }

    /**
     * Prints all objects of a specific class
     *
     * @param clazz the class to print.
     */
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
                            object.populate(sabres, c);
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

    /**
     * Gets the createdAt key used to save the creation time of an object.
     *
     * @return The key used to save the creation time of an object.
     */
    public static String getCreatedAtKey() {
        return CREATED_AT_KEY;
    }

    /**
     * Gets the updatedAt key used to save the last update time of an object.
     *
     * @return The key used to save the last update time of an object.
     */
    public static String getUpdatedAtKey() {
        return UPDATED_AT_KEY;
    }

    /**
     * Saves each object in the provided list.
     *
     * @param objects The objects to save.
     * @throws SabresException Throws a SabresException in case of error in one of the save
     *                         operations.
     */
    public static <T extends SabresObject> void saveAll(List<T> objects) throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        sabres.beginTransaction();
        try {
            for (SabresObject o : objects) {
                o.saveInTransaction(sabres);
            }
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
            sabres.close();
        }
    }

    /**
     * Saves each object in the provided list to the database in a background thread.
     * This is preferable to using saveAll, unless your code is already running from a background
     * thread.
     *
     * @param objects The objects to save.
     * @return A Task that is resolved when saveAll completes.
     */
    public static <T extends SabresObject> Task<Void> saveAllInBackground(final List<T> objects) {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                saveAll(objects);
                return null;
            }
        });
    }

    /**
     * Saves each object in the provided list to the database in a background thread.
     * This is preferable to using saveAll, unless your code is already running from a background
     * thread.
     *
     * @param objects  The objects to save.
     * @param callback callback.done(e) is called when the save completes.
     */
    public static <T extends SabresObject> void saveAllInBackground(final List<T> objects,
        final SaveCallback callback) {
        saveAllInBackground(objects).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Fetches all the objects that don't have data in the provided list.
     *
     * @param objects The list of objects to fetch.
     * @throws SabresException Throws an exception if there was a problem with the operation.
     */
    public static <T extends SabresObject> void fetchAllIfNeeded(List<T> objects)
        throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        try {
            for (T o : objects) {
                if (!o.isDataAvailable()) {
                    o.fetch(sabres);
                }
            }
        } finally {
            sabres.close();
        }
    }

    /**
     * Fetches all the objects that don't have data in the provided list in a background thread.
     *
     * @param objects The list of objects to fetch.
     * @return A Task that is resolved when fetch completes.
     */
    public static <T extends SabresObject> Task<Void> fetchAllIfNeededInBackground(
        final List<T> objects) {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                fetchAllIfNeeded(objects);
                return null;
            }
        });
    }

    /**
     * Fetches all the objects that don't have data in the provided list in a background thread.
     *
     * @param objects  The list of objects to fetch.
     * @param callback callback.done(e) is called when the fetch completes.
     */
    public static <T extends SabresObject> void fetchAllIfNeededInBackground(List<T> objects,
        final FetchCallback callback) {
        fetchAllIfNeededInBackground(objects).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Fetches all the objects in the provided list.
     *
     * @param objects The list of objects to fetch.
     * @throws SabresException Throws an exception if there was a problem with the operation.
     */
    public static <T extends SabresObject> void fetchAll(List<T> objects) throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        try {
            for (T o : objects) {
                o.fetch(sabres);
            }
        } finally {
            sabres.close();
        }
    }

    /**
     * Fetches all the objects in the provided list in a background thread.
     *
     * @param objects The list of objects to fetch.
     * @return A Task that is resolved when fetch completes.
     */
    public static <T extends SabresObject> Task<Void> fetchAllInBackground(final List<T> objects) {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                fetchAll(objects);
                return null;
            }
        });
    }

    /**
     * Fetches all the objects in the provided list in a background thread.
     *
     * @param objects  The list of objects to fetch.
     * @param callback callback.done(e) is called when the fetch completes.
     */
    public static <T extends SabresObject> void fetchAllInBackground(List<T> objects,
        final FetchCallback callback) {
        fetchAllInBackground(objects).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Deletes all the objects in the table.
     *
     * @throws SabresException Throws an exception if one of the deletes fails.
     */
    public static <T extends SabresObject> void deleteAll(Class<T> clazz) throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        sabres.beginTransaction();
        try {
            String table = clazz.getSimpleName();
            dropTable(sabres, table);
            createTable(sabres, Schema.getSchema(table), table);
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
            sabres.close();
        }
    }

    /**
     * Deletes each object in the provided list.
     *
     * @param objects The objects to delete.
     * @throws SabresException Throws an exception if one of the deletes fails.
     */
    public static <T extends SabresObject> void deleteAll(List<T> objects) throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        sabres.beginTransaction();
        try {
            for (T o : objects) {
                o.deleteInTransaction(sabres);
            }
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
            sabres.close();
        }
    }

    /**
     * Deletes all objects in the table in a background thread.
     *
     * @return A Task that is resolved when deleteAll completes.
     */
    public static <T extends SabresObject> Task<Void> deleteAllInBackground(final Class<T> clazz) {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deleteAll(clazz);
                return null;
            }
        });
    }

    /**
     * Deletes all objects in the table in a background thread.
     *
     * @param callback The callback method to execute when completed.
     */
    public static <T extends SabresObject> void deleteAllInBackground(Class<T> clazz,
                                                                      final DeleteCallback callback) {
        deleteAllInBackground(clazz).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Deletes each object in the provided list in a background thread.
     *
     * @param objects The objects to delete.
     * @return A Task that is resolved when deleteAll completes.
     */
    public static <T extends SabresObject> Task<Void> deleteAllInBackground(final List<T> objects) {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deleteAll(objects);
                return null;
            }
        });
    }

    /**
     * Deletes each object in the provided list in a background thread.
     *
     * @param objects  The objects to delete.
     * @param callback The callback method to execute when completed.
     */
    public static <T extends SabresObject> void deleteAllInBackground(final List<T> objects,
        final DeleteCallback callback) {
        deleteAllInBackground(objects).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Adds a value to a list with the given key.
     *
     * @param key   Key of list object.
     * @param value Value to add. Can be of Type Byte, Short, Integer, Long,
     *              Float, Double, Date or an extension of SabresObject.
     */
    public void add(String key, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        put(key, Collections.singletonList(value));
    }

    /**
     * Adds objects to a list with the given key.
     *
     * @param key     Key of list object.
     * @param objects List of Objects to add. List can be of Type Byte, Short, Integer, Long,
     *                Float, Double, Date or an extension of SabresObject.
     */
    public void addAll(String key, List<?> objects) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        if (values.containsKey(key)) {
            SabresValue sabresValue = values.get(key);
            if (!(sabresValue instanceof ListValue)) {
                throw new IllegalArgumentException(
                    "Add operations are only permitted on list values");
            }

            ListValue listValue = (ListValue)sabresValue;
            for (Object o : objects) {
                listValue.add(o);
            }
        } else {
            put(key, objects);
        }
    }

    /**
     * Adds a key-value pair to the object.
     *
     * @param key   Key in object.
     * @param value Can be of type Boolean, Byte, Short, Integer, Long,
     *              Float, Double, Date or an extension of SabresObject.
     *              Putting value to null will clear the key (same as {@link #remove(String)}).
     */
    public void put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        Map<String, SabresDescriptor> schema = Schema.getSchema(name);
        if (value == null && (schema == null || !schema.containsKey(key))) {
            // clearing a value that does not exist..
            return;
        }

        SabresValue sabresValue = SabresValue.create(value);

        // type checks and schema changes are not relevant on null object.
        if (!sabresValue.getDescriptor().getType().equals(SabresDescriptor.Type.Null)) {
            if (schema != null && schema.containsKey(key)) {
                if (!schema.get(key).equals(sabresValue.getDescriptor())) {
                    throw new IllegalArgumentException(
                        String.format("Cannot set key %s to type %s. " +
                                "Already set to type %s", key,
                            sabresValue.getDescriptor().toString(),
                            schema.get(key).toString()));
                }
            } else {
                Map<String, SabresDescriptor> currentSchema = schemaChanges.get(name);
                if (currentSchema == null) {
                    currentSchema = new ConcurrentHashMap<>();
                }
                currentSchema.put(key, sabresValue.getDescriptor());
                schemaChanges.put(name, currentSchema);
            }
        }

        values.put(key, sabresValue);
        dirtyKeys.add(key);
    }

    /**
     * Removes a key from this object's data if it exists.
     *
     * @param key The key to remove.
     */
    public void remove(String key) {
        put(key, null);
    }

    /**
     * Removes all instances of the objects contained in a List from the array associated with
     * a given key. To remove one value you can call
     * SabresObject.removeAll(key, Arrays.asList(value)).
     *
     * @param key     The key.
     * @param objects The objects to remove.
     */
    public void removeAll(String key, List<?> objects) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        if (values.containsKey(key)) {
            SabresValue sabresValue = values.get(key);
            if (!(sabresValue instanceof ListValue)) {
                throw new IllegalArgumentException(
                    "removeAll operation is only permitted on list values");
            }

            ListValue listValue = (ListValue)sabresValue;
            for (Object o : objects) {
                listValue.remove(o);
            }
        }
    }

    /**
     * Gets the unique id of the object. Assigned when object is first saved into the database.
     *
     * @return objectId.
     */
    public long getObjectId() {
        return id;
    }

    /**
     * Setter for the object id.
     * In general you do not need to use this.
     * However, in some cases this can be convenient.
     * For example, if you are serializing a SabresObject yourself and wish to recreate it,
     * you can use this to recreate the SabresObject exactly.
     *
     * @param objectId The unique id of the object.
     */
    public void setObjectId(long objectId) {
        this.id = objectId;
    }

    /**
     * Checks if the object has been fetched from the database.
     *
     * @return true if the object has been fetched from the database. false otherwise.
     */
    public boolean isDataAvailable() {
        return dataAvailable;
    }

    private void checkDataAvailable() {
        if (id != 0 && !dataAvailable) {
            throw new IllegalStateException("No data associated with object," +
                "call fetch to populate data from database");
        }
    }

    /**
     * Checks if the object has a specific key.
     *
     * @param key The key to check.
     * @return true if the object has data paired with the given key. false otherwise.
     */
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    /**
     * Gets a String value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a String.
     */
    public String getString(String key) {
        checkDataAvailable();
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof StringValue) {
                return ((StringValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a Boolean value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a Boolean.
     */
    public Boolean getBoolean(String key) {
        checkDataAvailable();
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof BooleanValue) {
                return ((BooleanValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets an Integer value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not an Integer.
     */
    public Integer getInt(String key) {
        checkDataAvailable();
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof IntValue) {
                return ((IntValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a Byte value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a Byte.
     */
    public Byte getByte(String key) {
        checkDataAvailable();
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof ByteValue) {
                return ((ByteValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a Short value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a Short.
     */
    public Short getShort(String key) {
        checkDataAvailable();
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof ShortValue) {
                return ((ShortValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a Long value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a Long.
     */
    public Long getLong(String key) {
        checkDataAvailable();
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof LongValue) {
                return ((LongValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a Float value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a Float.
     */
    public Float getFloat(String key) {
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof FloatValue) {
                return ((FloatValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a Double value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a Double.
     */
    public Double getDouble(String key) {
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof DoubleValue) {
                return ((DoubleValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a Date value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's not a Date.
     */
    public Date getDate(String key) {
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof DateValue) {
                return ((DateValue)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a List value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's cannot be converted to a list.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof ListValue) {
                return ((ListValue<T>)value).getValue();
            }
        }

        return null;
    }

    /**
     * Gets a custom subclass of SabresObject value for a given key.
     *
     * @param key The key to access value for.
     * @return null is there is no such value or if it's cannot be converted to a
     * SabresObject.
     */
    @SuppressWarnings("unchecked")
    public <T extends SabresObject> T getSabresObject(String key) {
        if (values.containsKey(key)) {
            SabresValue value = values.get(key);
            if (value instanceof ObjectValue) {
                return ((ObjectValue<T>)value).getValue();
            }
        }

        return null;
    }

    /**
     * Saves this object to the database in a background thread.
     * <p>
     * This is preferable to using {@link #save()} ,
     * unless your code is already running from a background thread.
     *
     * @return A Task that is resolved when the save completes.
     */
    public Task<Void> saveInBackground() {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                save();
                return null;
            }
        });
    }

    /**
     * Saves this object to the database in a background thread.
     * <p>
     * This is preferable to using {@link #save()} ,
     * unless your code is already running from a background thread.
     *
     * @param callback callback.done(e) is called when the save completes.
     */
    public void saveInBackground(final SaveCallback callback) {
        saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    private void saveIfNeededInTransaction(Sabres sabres)
        throws SabresException {
        if (id == 0 || !dirtyKeys.isEmpty()) {
            saveInTransaction(sabres);
        }
    }

    private void saveInTransaction(Sabres sabres) throws SabresException {
        put(UPDATED_AT_KEY, new Date());
        if (id == 0) {
            put(CREATED_AT_KEY, new Date());
        }

        synchronized (locks.get(name)) {
            Map<String, SabresDescriptor> schema = schemaChanges.get(name);

            if (schema != null && !schema.isEmpty()) {
                Schema.update(sabres, name, schema);
                updateTable(sabres, schema);
                schemaChanges.remove(name);
            }
        }

        updateChildren(sabres);

        if (id == 0) {
            id = insert(sabres);
        } else {
            update(sabres);
        }

        updateLists(sabres);

        dirtyKeys.clear();
        dataAvailable = true;
    }

    /**
     * Saves this object to the database.
     * <p>
     * Typically, you should use {@link #saveInBackground()} instead of this,
     * unless you are managing your own threading.
     *
     * @throws SabresException in case of an error with the save operation.
     */
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

    public void updateTable(Sabres sabres, Map<String, SabresDescriptor> schema) throws
        SabresException {
        if (SqliteMaster.tableExists(sabres, name)) {
            alterTable(sabres, schema);
        } else {
            createTable(sabres, schema);
        }
    }

    private static void createTable(Sabres sabres, Map<String, SabresDescriptor> schema,
                                    String table) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(table).ifNotExists();
        createCommand.withColumn(new Column(OBJECT_ID_KEY, SqlType.Integer).primaryKey().notNull());
        for (Map.Entry<String, SabresDescriptor> entry : schema.entrySet()) {
            Column column = new Column(entry.getKey(), entry.getValue().toSqlType());
            if (entry.getValue().getType().equals(SabresDescriptor.Type.Pointer)) {
                column.foreignKeyIn(entry.getValue().getName());
            }

            createCommand.withColumn(column);
        }

        sabres.execSQL(createCommand.toSql());
    }

    private void createTable(Sabres sabres, Map<String, SabresDescriptor> schema) throws
        SabresException {
        SabresObject.createTable(sabres, schema, name);
    }

    private void alterTable(Sabres sabres, Map<String, SabresDescriptor> schema) throws SabresException {
        for (Map.Entry<String, SabresDescriptor> entry : schema.entrySet()) {
            sabres.execSQL(new AlterTableCommand(name, new Column(entry.getKey(),
                    entry.getValue().toSqlType())).toSql());
        }
    }

    private static void dropTable(Sabres sabres, String table) throws SabresException {
        DropTableCommand dropTableCommand = new DropTableCommand(table).ifExists();
        sabres.execSQL(dropTableCommand.toSql());
    }

    private void updateChildren(Sabres sabres) throws SabresException {
        for (Map.Entry<String, SabresValue> entry : values.entrySet()) {
            if (entry.getValue() instanceof ObjectValue) {
                SabresObject o = ((ObjectValue<?>)entry.getValue()).getValue();
                o.saveIfNeededInTransaction(sabres);
            }
        }
    }

    private void updateLists(Sabres sabres) throws SabresException {
        for (Map.Entry<String, SabresValue> entry : values.entrySet()) {
            if (entry.getValue() instanceof ListValue) {
                if (entry.getValue() instanceof ObjectListValue) {
                    for (SabresObject o : ((ObjectListValue)entry.getValue()).getValue()) {
                        o.saveIfNeededInTransaction(sabres);
                    }
                }
                SabresList list = SabresList.get(sabres, name, entry.getKey());
                list.insert(sabres, id, ((ListValue<?>)entry.getValue()).getValue());
            }
        }
    }

    private long insert(Sabres sabres) throws SabresException {
        return sabres.insert(new InsertCommand(name, values).toSql());
    }

    private void update(Sabres sabres) throws SabresException {
        final Map<String, SabresValue> dirtyValues = new HashMap<>(dirtyKeys.size());
        for (String key : dirtyKeys) {
            dirtyValues.put(key, values.get(key));
        }

        UpdateCommand command = new UpdateCommand(name, dirtyValues);
        command.where(Where.equalTo(OBJECT_ID_KEY, new LongValue(id)));
        sabres.execSQL(command.toSql());
    }

    void fetch(Sabres sabres) throws SabresException {
        Cursor c = null;
        try {
            SelectCommand command = new SelectCommand(name, Schema.getKeys(name)).
                where(Where.equalTo(OBJECT_ID_KEY, new LongValue(id)));
            c = sabres.select(command.toSql());
            if (!c.moveToFirst()) {
                throw new SabresException(SabresException.OBJECT_NOT_FOUND,
                    String.format("table %s has no object with key %s", name, id));
            }
            populate(sabres, c);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Fetches the data of this object from the database.
     * <p>
     * Typically, you should use {@link #fetchInBackground()} instead of this,
     * unless you are managing your own threading.
     *
     * @throws SabresException in case of an error with the fetch operation.
     */
    public void fetch() throws SabresException {
        Sabres sabres = Sabres.self();
        sabres.open();
        try {
            fetch(sabres);
        } finally {
            sabres.close();
        }
    }

    /**
     * If this SabresObject has not been fetched ({@link #isDataAvailable()} returns false),
     * fetches the data of this object from the database.
     *
     * @throws SabresException Throws an exception if there was a problem with the operation.
     */
    public void fetchIfNeeded() throws SabresException {
        if (!isDataAvailable()) {
            fetch();
        }
    }

    /**
     * If this SabresObject has not been fetched ({@link #isDataAvailable()} returns false),
     * fetches the data of this object from the database in a background thread.
     *
     * @return A Task that is resolved when fetch completes.
     */
    public Task<Void> fetchIfNeededInBackground() {
        if (isDataAvailable()) {
            return Task.forResult(null);
        }

        return fetchInBackground();
    }

    /**
     * If this SabresObject has not been fetched ({@link #isDataAvailable()} returns false),
     * fetches the data of this object from the database in a background thread.
     *
     * @param callback callback.done(e) is called when the fetch completes.
     */
    public void fetchIfNeededInBackground(final FetchCallback callback) {
        fetchIfNeededInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Fetches this object from the database in a background thread.
     * <p>
     * This is preferable to using {@link #fetch()},
     * unless your code is already running from a background thread.
     *
     * @return A Task that is resolved when the fetch completes.
     */
    public Task<Void> fetchInBackground() {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                fetch();
                return null;
            }
        });
    }

    /**
     * Checks if the passed SabresObject has the same Id as this.
     *
     * @param other The other SabresObject
     * @return true ig the id's of the objects are the same, false otherwise.
     */
    public boolean hasSameId(SabresObject other) {
        return id == other.id;
    }

    /**
     * Fetches this object from the database in a background thread.
     * <p>
     * This is preferable to using {@link #fetch()} ,
     * unless your code is already running from a background thread.
     *
     * @param callback callback.done(e) is called when the fetch completes.
     */
    public void fetchInBackground(final FetchCallback callback) {
        fetchInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    /**
     * Checks if the Object has data that has not yet been saved to the Database.
     *
     * @return true if it has data that has not been saved, false otherwise.
     */
    public boolean isDirty() {
        return !dirtyKeys.isEmpty();
    }

    /**
     * Checks if a specific key was changed and not yet saved to the database.
     *
     * @param key The key to check.
     * @return true if the value of the passed key was not saved, false otherwise.
     */
    public boolean isDirty(String key) {
        return dirtyKeys.contains(key);
    }

    /**
     * Returns a Set of all the keys in this object.
     *
     * @return a Set of all the keys in this object.
     */
    public Set<String> keySet() {
        return Schema.getSchema(name).keySet();
    }

    void populate(Sabres sabres, Cursor c) throws SabresException {
        populate(sabres, c, null);
    }

    private String getCursorKey(String prefix, String key) {
        if (prefix == null) {
            return key;
        }

        return String.format("%s_%s", prefix, key);
    }

    /**
     * Increments the given key by 1.
     *
     * @param key The key to increment.
     */
    public void increment(String key) {
        increment(key, 1);
    }

    /**
     * Increments the given key by amount.
     *
     * @param key    The key to increment.
     * @param amount The amount to increment the current value of the given key.
     */
    public void increment(String key, Number amount) {
        if (values.containsKey(key)) {
            SabresValue sabresValue = values.get(key);
            if (sabresValue instanceof NumberValue) {
                ((NumberValue)sabresValue).increment(amount);
                dirtyKeys.add(key);
            } else {
                throw new IllegalArgumentException(
                    String.format("Key %s is not a number. Cannot increment", key));
            }
        } else {
            throw new IllegalArgumentException(
                String.format("Key %s does not exist. Cannot increment", key));
        }
    }

    void populate(Sabres sabres, Cursor c, String prefix) throws SabresException {
        id = CursorHelper.getLong(c, OBJECT_ID_KEY);
        Map<String, SabresDescriptor> schema = Schema.getSchema(name);

        for (Map.Entry<String, SabresDescriptor> entry : schema.entrySet()) {
            if (c.getColumnIndex(getCursorKey(prefix, entry.getKey())) != -1 &&
                !c.isNull(c.getColumnIndex(getCursorKey(prefix, entry.getKey())))) {
                SabresValue value = null;
                switch (entry.getValue().getType()) {
                    case Integer:
                        value = new IntValue(CursorHelper.getInt(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case Boolean:
                        value = new BooleanValue(CursorHelper.getBoolean(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case Byte:
                        value = new ByteValue(CursorHelper.getByte(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case Double:
                        value = new DoubleValue(CursorHelper.getDouble(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case Float:
                        value = new FloatValue(CursorHelper.getFloat(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case String:
                        value = new StringValue(CursorHelper.getString(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case Short:
                        value = new ShortValue(CursorHelper.getShort(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case Long:
                        value = new LongValue(CursorHelper.getLong(c,
                            getCursorKey(prefix, entry.getKey())));
                        break;
                    case Date:
                        value = new DateValue(CursorHelper.getDate(c, getCursorKey(prefix,
                            entry.getKey())));
                        break;
                    case Pointer:
                        SabresObject object =
                            createWithoutData(subClasses.get(entry.getValue().getName()),
                                CursorHelper.getLong(c, getCursorKey(prefix,
                                    entry.getKey())));
                        value = new ObjectValue<>(object);
                        break;
                    case List:
                        List<?> list = SabresList.get(sabres, name, entry.getKey()).
                            select(sabres, id, entry.getValue());
                        value = SabresValue.create(list);
                        break;
                }

                if (value != null) {
                    values.put(entry.getKey(), value);
                }
            }
        }

        dataAvailable = true;
    }

    void populateChild(Sabres sabres, Cursor c, String key) throws SabresException {
        SabresValue value = values.get(key);
        if (value == null) {
            throw new IllegalStateException(String.format("Child with key %s does not exist", key));
        }

        if (!(value instanceof ObjectValue)) {
            throw new IllegalArgumentException(
                String.format("value of key %s in not a SabresObject", key));
        }

        ((ObjectValue<?>)value).getValue().populate(sabres, c, key);
    }

    private String stringify(String key) {
        if (!values.containsKey(key)) {
            return UNDEFINED;
        }

        return values.get(key).toString();
    }

    @Override
    public String toString() {
        return SabresObject.toString(name, Collections.singletonList(this));
    }

    /**
     * Deletes this object from the database. This does not delete or destroy the object locally.
     *
     * @throws SabresException Throws an error if the object does not exist or if the operation
     *                         fails.
     */
    public void delete() throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        try {
            deleteInTransaction(sabres);
        } finally {
            sabres.close();
        }
    }

    void deleteInTransaction(Sabres sabres) throws SabresException {
        sabres.execSQL(new DeleteCommand(name).where(Where.equalTo(OBJECT_ID_KEY,
            new LongValue(id))).toSql());
    }

    /**
     * Deletes this object on the server in a background thread.
     * <p>
     * This is preferable to using {@link #delete()}, unless your code is already running from a
     * background thread.
     *
     * @return A Task that is resolved when delete completes.
     */
    public Task<Void> deleteInBackground() {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                delete();
                return null;
            }
        });
    }

    /**
     * Deletes this object on the server in a background thread.
     * <p>
     * This is preferable to using {@link #delete()}, unless your code is already running from a
     * background thread.
     *
     * @param callback callback.done(e) is called when the delete operation completes.
     */
    public void deleteInBackground(final DeleteCallback callback) {
        deleteInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    /**
     * Gets the time that this object was created in the Database.
     *
     * @return the time this object was created in the Database.
     */
    public Date getCreatedAt() {
        return getDate(CREATED_AT_KEY);
    }

    /**
     * Gets the time that this object was last saved in the Database.
     *
     * @return the time this object was last saved in the Database.
     */
    public Date getUpdatedAt() {
        return getDate(UPDATED_AT_KEY);
    }
}
