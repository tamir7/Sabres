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

package com.example.sabres.controller;

import android.util.Log;

import com.example.sabres.model.Actor;
import com.example.sabres.model.Movie;
import com.sabres.Sabres;
import com.sabres.SabresObject;
import com.sabres.SabresQuery;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

public class TasksTestController extends AbstractTestController {
    private static final String TAG = TasksTestController.class.getSimpleName();

    public static void begin() {
        checkNonDatabaseApi();
        checkDataConsistency().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return checkBulkOperations();
            }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return checkQueries();
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Tests failed", task.getError());
                } else {
                    Log.i(TAG, "Tests passed");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private static Task<Void> checkQueries() {
        Log.i(TAG, "checkQueries start");
        final List<Actor> actors = new ArrayList<>();
        return Sabres.deleteDatabase().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkQueries: deleteDatabase successful");
                actors.addAll(createActors());
                return SabresObject.saveAllInBackground(actors);
            }
        }).onSuccessTask(new Continuation<Void, Task<List<Actor>>>() {
            @Override
            public Task<List<Actor>> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkQueries: saveAllInBackground successful");
                return SabresQuery.getQuery(Actor.class).
                    addAscendingOrder(SabresObject.getCreatedAtKey()).
                    selectKeys(Collections.singletonList(SabresObject.getCreatedAtKey())).
                    findInBackground();
            }
        }).onSuccessTask(new Continuation<List<Actor>, Task<List<Actor>>>() {
            @Override
            public Task<List<Actor>> then(Task<List<Actor>> task) throws Exception {
                Log.i(TAG, "checkQueries: findInBackground ascending order successful");
                Actor previousActor = null;
                Assert.assertEquals(actors.size(), task.getResult().size());
                for (Actor actor : task.getResult()) {
                    if (previousActor != null) {
                        Assert.assertTrue(previousActor.getCreatedAt().before(actor.getCreatedAt()));
                    }
                    previousActor = actor;
                }
                return SabresQuery.getQuery(Actor.class).
                    addDescendingOrder(SabresObject.getUpdatedAtKey()).
                    selectKeys(Collections.singletonList(SabresObject.getUpdatedAtKey())).
                    findInBackground();
            }
        }).onSuccessTask(new Continuation<List<Actor>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Actor>> task) throws Exception {
                Actor previousActor = null;
                Assert.assertEquals(actors.size(), task.getResult().size());
                for (Actor actor : task.getResult()) {
                    if (previousActor != null) {
                        Assert.assertTrue(previousActor.getUpdatedAt().after(actor.getUpdatedAt()));
                    }
                    previousActor = actor;
                }
                return null;
            }
        });
    }

    private static Task<Void> checkBulkOperations() {
        final Capture<Integer> actorCountCapture = new Capture<>();
        final List<Actor> actors = new ArrayList<>();
        final List<Actor> actorsToFetch = new ArrayList<>();
        Log.i(TAG, "checkBulkOperations start");
        return Sabres.deleteDatabase().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkBulkOperations: deleteDatabase successful");
                actors.addAll(createActors());
                actorCountCapture.set(actors.size());
                return SabresObject.saveAllInBackground(actors);
            }
        }).onSuccessTask(new Continuation<Void, Task<List<Actor>>>() {
            @Override
            public Task<List<Actor>> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkBulkOperations: saveAllInBackground successful");
                return SabresQuery.getQuery(Actor.class).findInBackground();
            }
        }).onSuccessTask(new Continuation<List<Actor>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Actor>> task) throws Exception {
                Log.i(TAG, "checkBulkOperations: findInBackground successful");
                Assert.assertEquals(actorCountCapture.get(), Integer.valueOf(
                    task.getResult().size()));
                Assert.assertEquals(true, containsBenicio(task.getResult()));
                for (Actor actor : actors) {
                    Actor actorToFetch = SabresObject.createWithoutData(Actor.class,
                        actor.getObjectId());
                    actorsToFetch.add(actorToFetch);
                }
                return SabresObject.fetchAllIfNeededInBackground(actorsToFetch);
            }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkBulkOperations: fetchAllIfNeededInBackground successful");
                int foundActors = 0;
                for (Actor actor : actors) {
                    for (Actor fetchedActor : actorsToFetch) {
                        if (actor.hasSameId(fetchedActor)) {
                            foundActors++;
                            Assert.assertEquals(actor.getName(), fetchedActor.getName());
                            break;
                        }
                    }
                }
                Assert.assertEquals(actors.size(), foundActors);
                return SabresObject.deleteAllInBackground(actors);
            }
        }).onSuccessTask(new Continuation<Void, Task<Long>>() {
            @Override
            public Task<Long> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkBulkOperations: deleteAllInBackground successful");
                return SabresQuery.getQuery(Actor.class).countInBackground();
            }
        }).onSuccess(new Continuation<Long, Void>() {
            @Override
            public Void then(Task<Long> task) throws Exception {
                Log.i(TAG, "checkBulkOperations: countInBackground successful");
                Assert.assertEquals((Long)0L, task.getResult());
                Log.i(TAG, "checkBulkOperations successful");
                return null;
            }
        });
    }

    private static Task<Void> checkDataConsistency() {
        final Capture<Date> createdAtCapture = new Capture<>();
        final Capture<Date> updatedAtCapture = new Capture<>();
        final Capture<Movie> fightClubCapture = new Capture<>(MovieController.createFightClub());

        Log.i(TAG, "checkDataConsistency start");

        return Sabres.deleteDatabase().onSuccessTask(new Continuation<Void, Task<Void>>() {

            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkDataConsistency: deleteDatabase successful");
                return fightClubCapture.get().saveInBackground();
            }
        }).onSuccessTask(new Continuation<Void, Task<Movie>>() {
            @Override
            public Task<Movie> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkDataConsistency: saveInBackground successful");
                Movie fightClub = fightClubCapture.get();
                createdAtCapture.set(fightClub.getCreatedAt());
                updatedAtCapture.set(fightClub.getUpdatedAt());
                SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
                return q.getInBackground(fightClub.getObjectId());
            }
        }).onSuccessTask(new Continuation<Movie, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Movie> task) throws Exception {
                Log.i(TAG, "checkDataConsistency: getInBackground successful");
                checkFightClubMovieObject(task.getResult(), createdAtCapture.get(),
                    updatedAtCapture.get());
                Log.i(TAG, "checkDataConsistency successful");
                fightClubCapture.get().watch();
                return fightClubCapture.get().saveInBackground();
            }
        }).onSuccessTask(new Continuation<Void, Task<Movie>>() {
            @Override
            public Task<Movie> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkDataConsistency: saveInBackground after watch successful");
                return SabresQuery.getQuery(Movie.class).whereEqualTo(SabresObject.getObjectIdKey(),
                    fightClubCapture.get().getObjectId()).getFirstInBackground();
            }
        }).onSuccessTask(new Continuation<Movie, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Movie> task) throws Exception {
                Log.i(TAG, "checkDataConsistency: getFirstInBackground successful");
                Assert.assertEquals(Integer.valueOf(1), task.getResult().getTimesWatched());
                task.getResult().watch(-5);
                return task.getResult().saveInBackground();
            }
        }).onSuccessTask(new Continuation<Void, Task<Movie>>() {
            @Override
            public Task<Movie> then(Task<Void> task) throws Exception {
                Log.i(TAG, "checkDataConsistency: saveInBackground after negative watch successful");
                return SabresQuery.getQuery(Movie.class).
                    getInBackground(fightClubCapture.get().getObjectId());
            }
        }).onSuccessTask(new Continuation<Movie, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Movie> task) throws Exception {
                Log.i(TAG, "checkDataConsistency: getInBackground after watch increment successful");
                Assert.assertEquals(Integer.valueOf(-4), task.getResult().getTimesWatched());
                return task.getResult().deleteInBackground();
            }
        });
    }
}
