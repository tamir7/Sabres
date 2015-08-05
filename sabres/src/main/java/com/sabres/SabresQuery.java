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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * The SabresQuery class defines a query that is used to fetch SabresObjects.
 * The most common use case is finding all objects that match a query through the
 * {@link #findInBackground(FindCallback)} method, using a FindCallback. For example,
 * this sample code fetches all objects of class "MyClass".
 * <pre>
 * {@code
 * SabresQuery<MyClass> query = SabresQuery.getQuery(MyClass.class);
 * query.findInBackground(new FindCallback<MyClass>() {
 *     public void done(List<MyClass> objects, SabresException e) {
 *         if (e == null) {
 *             objectsWereRetrievedSuccessfully(objects);
 *         } else {
 *             objectRetrievalFailed();
 *         }
 *    }
 * });
 * }
 * </pre>
 * A SabresQuery can also be used to retrieve a single object whose id is known,
 * through the {@link #getInBackground(long)} method, using a GetCallback.
 * For example, this sample code fetches an object of class "MyClass" for id myId.
 * <pre>
 * {@code
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
 * A SabresQuery can also be used to count the number of objects that match the query without
 * retrieving all of those objects. For example,
 * this sample code counts the number of objects of the class "MyClass".
 * <pre>
 * {@code
 * SabresQuery<MyClass> query = ParseQuery.getQuery(MyClass.class);
 * query.countInBackground(new CountCallback() {
 *     public void done(int count, SabresException e) {
 *         if (e == null) {
 *             objectsWereCounted(count);
 *         } else {
 *             objectCountFailed();
 *         }
 *     }
 * });
 * }
 * </pre>
 * Using the callback methods is usually preferred because the database operation will not block
 * the calling thread. However, in some cases it may be easier to use the
 * {@link #find()}, {@link #get(long)} or {@link #count()} calls, which do block the calling thread.
 * For example, if your application has already spawned a background task to perform work,
 * that background task could use the blocking calls and avoid the code complexity of callbacks.
 */
public class SabresQuery<T extends SabresObject> {
    private static final String TAG = SabresQuery.class.getSimpleName();
    private final String name;
    private final Class<T> clazz;
    private final List<String> keyIndices = new ArrayList<>();
    private final List<String> includes = new ArrayList<>();
    private final List<OrderBy> orderByList = new ArrayList<>();
    private final List<SabresQuery<T>> innerQueries;
    private List<String> selectKeys;
    private Where where;
    private Integer limit;
    private Integer skip;
    private SelectCommand innerSelect;
    private boolean illegalQuery = false;

    /**
     * Constructs a query for a SabresObject subclass type.
     * A default query with no further parameters will retrieve all SabresObjects of the
     * provided class.
     *
     * @param clazz The SabresObject subclass type to retrieve.
     */
    public SabresQuery(Class<T> clazz) {
        this.clazz = clazz;
        name = clazz.getSimpleName();
        selectKeys = Schema.getKeys(name);
        innerQueries = null;
    }

    private SabresQuery(List<SabresQuery<T>> queries) {
        if (queries.isEmpty()) {
            throw new IllegalArgumentException("The list of queries is empty");
        }

        SabresQuery<T> q = queries.get(0);
        this.clazz = q.clazz;
        this.name = q.name;
        this.innerQueries = queries;
    }

    /**
     * Creates a new query for the given SabresObject subclass type.
     * A default query with no further parameters will retrieve all SabresObjects of the
     * provided class.
     *
     * @param clazz The name of the class to retrieve SabresObjects for.
     */
    public static <T extends SabresObject> SabresQuery<T> getQuery(Class<T> clazz) {
        return new SabresQuery<>(clazz);
    }

    /**
     * Constructs a query that is the or of the given queries.
     * Previous calls to setLimit, setSkip, addAscendingOrder, addDescendingOrder or selectKeys
     * are disregarded for the inner queries. Calling those functions on the returned query will
     * have the desired effect.
     * Calling where* functions on the return query is not permitted.
     *
     * @param queries The list of ParseQuerys to 'or' together.
     * @return A ParseQuery that is the 'or' of the passed in queries.
     */
    public static <T extends SabresObject> SabresQuery<T> or(List<SabresQuery<T>> queries) {
        return new SabresQuery<>(queries);
    }

    private void createIndices(Sabres sabres)
        throws SabresException {
        if (innerQueries == null) {
            if (!keyIndices.isEmpty()) {
                CreateIndexCommand createIndexCommand =
                    new CreateIndexCommand(name, keyIndices).ifNotExists();
                sabres.execSQL(createIndexCommand.toString());
            }
        } else {
            for (SabresQuery q : innerQueries) {
                q.createIndices(sabres);
            }
        }
    }

    /**
     * Sorts the results in ascending order by the given key.
     * Multiple calls with different keys can be made to this and
     * {@link #addDescendingOrder(String)} functions for a single query.
     *
     * @param key The key to order by
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> addAscendingOrder(String key) {
        orderByList.add(new OrderBy(key, OrderBy.Direction.Ascending));
        return this;
    }

    /**
     * Restrict the fields of returned SabresObjects to only include the provided keys.
     *
     * @param selectKeys The set of keys to include in the result.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> selectKeys(List<String> selectKeys) {
        this.selectKeys.clear();
        this.selectKeys.add(SabresObject.getObjectIdKey());
        this.selectKeys.addAll(selectKeys);
        return this;
    }

    /**
     * Sorts the results in descending order by the given key.
     * Multiple calls with different keys can be made to this and
     * {@link #addAscendingOrder(String)} functions for a single query.
     *
     * @param key The key to order by
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> addDescendingOrder(String key) {
        orderByList.add(new OrderBy(key, OrderBy.Direction.Descending));
        return this;
    }

    /**
     * Constructs a SabresObject whose id is already known by fetching data from the database in
     * a background thread.
     *
     * @param objectId Object id of the SabresObject to fetch.
     * @return A Task that is resolved when the fetch completes.
     */
    public Task<T> getInBackground(final long objectId) {
        return Task.callInBackground(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return get(objectId);
            }
        });
    }

    /**
     * Constructs a SabresObject whose id is already known by fetching data from the database in
     * a background thread.
     *
     * @param objectId Object id of the SabresObject to fetch.
     * @param callback callback.done(object, e) will be called when the fetch completes.
     */
    public void getInBackground(long objectId, final GetCallback<T> callback) {
        getInBackground(objectId).continueWith(new Continuation<T, Object>() {
            @Override
            public Object then(Task<T> task) throws Exception {
                callback.done(task.getResult(), SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    /**
     * returns the maximum returned results for this query.
     *
     * @return the number of maximum returned results. null if was not set(all results.)
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * Controls the maximum number of results that are returned.
     * There is no default limit. If not specified, all objects that match the query are returned.
     *
     * @param limit The new limit.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Returns the number of results to skip before returning any results for the current query.
     *
     * @return The number of results to skip. null if was not set (don't skip).
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Controls the number of results to skip before returning any results.
     * This is useful for pagination. Default is to skip zero results.
     *
     * @param skip The new skip.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> setSkip(int skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Include nested SabresObjects for the provided key.
     *
     * @param key The key that should be included.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> include(String key) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            throw new IllegalArgumentException(String.format("Unrecognized key %s in Object %s",
                key, name));
        }

        if (descriptor.getType().equals(SabresDescriptor.Type.Pointer)) {
            includes.add(key);
        } else {
            Log.w(TAG, String.format("keys of type %s are always included in query results",
                descriptor.getType().toString()));
        }

        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to be equal to the
     * provided value.
     *
     * @param key   The key to check.
     * @param value The value that the SabresObject must contain.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereEqualTo(String key, Object value) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }

        SabresValue sabresValue = SabresValue.create(value);
        if (descriptor.getType().equals(SabresDescriptor.Type.List)) {
            innerSelect = new SelectCommand(SabresList.getTableName(name, key),
                Collections.singletonList(SabresList.getParentIdKey()));
            innerSelect.as(SabresList.getParentIdKey(), SabresObject.getObjectIdKey());
            innerSelect.where(Where.equalTo(SabresList.getValueKey(), sabresValue));
            innerSelect.withoutSemicolon();
        } else {
            addWhere(key, Where.equalTo(key, sabresValue));
        }
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to be not equal to
     * the provided value.
     *
     * @param key   The key to check.
     * @param value The value that must not be equalled.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereNotEqualTo(String key, Object value) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            // if key does not exist, then we're not equal to it.
            return this;
        }
        addWhere(key, Where.notEqualTo(key, SabresValue.create(value)));
        return this;
    }

    /**
     * Add a constraint for finding objects that contain the given key.
     *
     * @param key The key that should exist.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereExists(String key) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.isNot(key, new StringValue("NULL")));
        return this;
    }

    /**
     * Add a constraint for finding objects that do not contain a given key.
     *
     * @param key The key that should not exist.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereDoesNotExist(String key) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            return this;
        }
        addWhere(key, Where.is(key, new StringValue("NULL")));
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to be less than
     * the provided value.
     *
     * @param key   The key to check.
     * @param value The value that provides an upper bound.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereLessThan(String key, Object value) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.lessThan(key, SabresValue.create(value)));
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to be less than or
     * equal to the provided value.
     *
     * @param key   The key to check.
     * @param value The value that provides an upper bound.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereLessThanOrEqual(String key, Object value) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.lessThanOrEqual(key, SabresValue.create(value)));
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to be greater than
     * the provided value.
     *
     * @param key   The key to check.
     * @param value The value that provides an lower bound.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereGraterThan(String key, Object value) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.greaterThan(key, SabresValue.create(value)));
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to be greater than
     * or equal to the provided value.
     *
     * @param key   key The key to check.
     * @param value The value that provides an lower bound.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereGreaterThanOrEqual(String key, Object value) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.greaterThanOrEqual(key, SabresValue.create(value)));
        return this;
    }

    /**
     * Add a constraint for finding string values that start with a provided string.
     *
     * @param key    The key that the string to match is stored in.
     * @param prefix The substring that the value must start with.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereStartsWith(String key, String prefix) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.startsWith(key, prefix));
        return this;
    }

    /**
     * Add a constraint for finding string values that end with a provided string.
     *
     * @param key    The key that the string to match is stored in.
     * @param suffix The substring that the value must end with.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereEndsWith(String key, String suffix) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.endsWith(key, suffix));
        return this;
    }

    /**
     * @param key       The key that the string to match is stored in.
     * @param substring The substring that the value must contain.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereContains(String key, String substring) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        addWhere(key, Where.contains(key, substring));
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to be contained in
     * the provided list of values.
     *
     * @param key    The key to check.
     * @param values The values that will match.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereContainedIn(String key, List<?> values) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        List<String> stingValues = new ArrayList<>(values.size());
        for (Object o : values) {
            stingValues.add(SabresValue.create(o).toSql());
        }

        addWhere(key, Where.in(key, stingValues));
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value not be contained in
     * the provided list of values.
     *
     * @param key    The key to check.
     * @param values The values that will not match.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereNotContainedIn(String key, List<?> values) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            return this;
        }
        List<String> stingValues = new ArrayList<>(values.size());
        for (Object o : values) {
            stingValues.add(SabresValue.create(o).toSql());
        }

        addWhere(key, Where.notIn(key, stingValues));
        return this;
    }

    /**
     * Add a constraint to the query that requires a particular key's value to contain every one
     * of the provided list of values.
     *
     * @param key    The key to check. This key's value must be an array.
     * @param values The values that will match.
     * @return this, so you can chain this call.
     */
    public SabresQuery<T> whereContainsAll(String key, List<?> values) {
        SabresDescriptor descriptor = Schema.getDescriptor(name, key);
        if (descriptor == null) {
            illegalQuery = true;
            return this;
        }
        if (!descriptor.getType().equals(SabresDescriptor.Type.List)) {
            throw new IllegalArgumentException(String.format("Key %s in object %s is not a list",
                key, name));
        }

        innerSelect = new SelectCommand(SabresList.getTableName(name, key),
            Collections.singletonList(SabresList.getParentIdKey()));
        innerSelect.as(SabresList.getParentIdKey(), SabresObject.getObjectIdKey());
        Where where = null;
        for (Object o : values) {
            if (where == null) {
                where = Where.equalTo(SabresList.getValueKey(), SabresValue.create(o));
            } else {
                where.or(Where.equalTo(SabresList.getValueKey(), SabresValue.create(o)));
            }
        }
        innerSelect.where(where);
        innerSelect.groupBy(SabresList.getParentIdKey());
        innerSelect.having(Where.equalTo(new SqlFunction(SabresList.getParentIdKey(),
            SqlFunction.Function.Count).toSql(), new IntValue(values.size())));
        innerSelect.withoutSemicolon();
        return this;
    }

    private void addWhere(String key, Where where) {
        if (innerQueries != null) {
            throw new IllegalStateException("Cannot call Where* functions on a compound query.");
        }

        if (this.where == null) {
            this.where = where;
        } else {
            this.where = this.where.and(where);
        }

        keyIndices.add(key);
    }

    /**
     * Retrieves at most one SabresObject that satisfies this query.
     *
     * @return A SabresObject obeying the conditions set in this query.
     * @throws SabresException throws an exception if no object was found or if there was a
     *                         problem with the query.
     * @see SabresException#OBJECT_NOT_FOUND
     */
    public T getFirst() throws SabresException {
        limit = 1;
        List<T> results = find();
        if (results.isEmpty()) {
            throw new SabresException(SabresException.OBJECT_NOT_FOUND,
                String.format("failed to get first object for query %s", where.toString()));
        }

        return results.get(0);
    }

    /**
     * Retrieves at most one SabresObject that satisfies this query in a background thread.
     * This is preferable to using {@link #getFirst()}, unless your code is already
     * running in a background thread.
     *
     * @return A Task that will be resolved when the get has completed.
     */
    public Task<T> getFirstInBackground() {
        return Task.callInBackground(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return getFirst();
            }
        });
    }

    /**
     * Retrieves at most one SabresObject that satisfies this query in a background thread.
     * This is preferable to using {@link #getFirst()}, unless your code is already
     *
     * @param callback callback.done(object, e) is called when the find completes.
     */
    public void getFirstInBackground(final GetCallback<T> callback) {
        getFirstInBackground().continueWith(new Continuation<T, Void>() {
            @Override
            public Void then(Task<T> task) throws Exception {
                callback.done(task.getResult(), SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Retrieves a list of SabresObjects that satisfy this query from the database in a background
     * thread.
     *
     * @return A Task that will be resolved when the find has completed.
     */
    public Task<List<T>> findInBackground() {
        return Task.callInBackground(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return find();
            }
        });
    }

    /**
     * Retrieves a list of SabresObjects that satisfy this query from the database in a background
     * thread.
     *
     * @param callback callback.done(objectList, e) is called when the find completes.
     */
    public void findInBackground(final FindCallback<T> callback) {
        findInBackground().continueWith(new Continuation<List<T>, Void>() {
            @Override
            public Void then(Task<List<T>> task) throws Exception {
                callback.done(task.getResult(), SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Counts the number of objects that match this query.
     *
     * @return The number of object that match the query.
     * @throws SabresException Throws an exception when the query is invalid.
     */
    public long count() throws SabresException {
        if (illegalQuery) {
            return 0;
        }

        Sabres sabres = Sabres.self();
        sabres.open();
        try {
            CountCommand command = new CountCommand(name);
            if (where != null) {
                command.where(where);
            }

            if (SqliteMaster.tableExists(sabres, name)) {
                return sabres.count(command.toSql());
            }

            return 0;
        } finally {
            sabres.close();
        }
    }

    /**
     * Counts the number of objects that match this query in a background thread.
     *
     * @param callback callback.done(count, e) will be called when the count completes.
     */
    public void countInBackground(final CountCallback callback) {
        countInBackground().continueWith(new Continuation<Long, Void>() {
            @Override
            public Void then(Task<Long> task) throws Exception {
                callback.done(task.getResult(), SabresException.construct(task.getError()));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Counts the number of objects that match this query in a background thread.
     *
     * @return A Task that will be resolved when the count has completed.
     */
    public Task<Long> countInBackground() {
        return Task.callInBackground(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return count();
            }
        });
    }

    /**
     * Retrieves a list of SabresObjects that satisfy this query.
     *
     * @return A list of all SabresObjects obeying the conditions set in this query.
     * @throws SabresException Throws a SabresException if there was an error with the query.
     */
    public List<T> find() throws SabresException {
        List<T> objects = new ArrayList<>();

        if (illegalQuery) {
            return objects;
        }

        Sabres sabres = Sabres.self();
        sabres.open();
        Cursor c = null;
        try {
            if (SqliteMaster.tableExists(sabres, name)) {
                createIndices(sabres);
                SelectCommand command = new SelectCommand(name, selectKeys);
                for (String include : includes) {
                    SabresDescriptor descriptor = Schema.getDescriptor(name, include);
                    if (descriptor != null &&
                        descriptor.getType().equals(SabresDescriptor.Type.Pointer)) {
                        command.join(descriptor.getName(), include,
                            Schema.getKeys(descriptor.getName()));
                    }
                }

                for (OrderBy orderBy : orderByList) {
                    command.orderBy(orderBy);
                }

                if (limit != null) {
                    command.withLimit(limit);
                }

                if (skip != null) {
                    command.withSkip(skip);
                }

                if (innerQueries != null) {
                    for (SabresQuery q : innerQueries) {
                        if (where == null) {
                            where = q.where;
                        } else {
                            where.or(q.where);
                        }
                    }
                }

                if (innerSelect != null) {
                    command.inInnerSelect(innerSelect, SabresObject.getObjectIdKey());
                }

                c = sabres.select(command.where(where).toSql());
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    T object = createObjectInstance();
                    object.populate(sabres, c);
                    for (String include : includes) {
                        object.populateChild(sabres, c, include);
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

    /**
     * Constructs a SabresObject whose id is already known by fetching data from the database.
     *
     * @param objectId The id of the object to get.
     * @return Object id of the ParseObject to fetch.
     * @throws SabresException Throws an exception when there is no such object or if there's a
     *                         database error.
     * @see SabresException#OBJECT_NOT_FOUND
     */
    public T get(long objectId) throws SabresException {
        whereEqualTo(SabresObject.getObjectIdKey(), objectId);
        return getFirst();
    }
}
