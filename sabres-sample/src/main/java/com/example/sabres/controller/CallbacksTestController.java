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
import com.sabres.CountCallback;
import com.sabres.DeleteCallback;
import com.sabres.FetchCallback;
import com.sabres.FindCallback;
import com.sabres.GetCallback;
import com.sabres.Sabres;
import com.sabres.SabresException;
import com.sabres.SabresObject;
import com.sabres.SabresQuery;
import com.sabres.SaveCallback;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallbacksTestController extends AbstractTestController {
    private static final String TAG = CallbacksTestController.class.getSimpleName();

    public static void begin() {
        checkNonDatabaseApi();
        checkDataConsistency(new TestCallback() {
            @Override
            public void done(Exception e) {
                if (e == null) {
                    checkBulkOperations(new TestCallback() {
                        @Override
                        public void done(Exception e) {
                            if (e == null) {
                                Log.i(TAG, "Tests passed");
                            } else {
                                Log.e(TAG, "Tests Failed", e);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Tests Failed", e);
                }

            }
        });
    }

    private static void checkDataConsistency(final TestCallback callback) {
        Log.i(TAG, "checkDataConsistency start");
        Sabres.deleteDatabase(new DeleteCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkDataConsistency: deleteDatabase successful");
                    createAndSaveFightClubMovie(callback);
                }
            }
        });

    }

    private static void checkBulkOperations(final TestCallback callback) {
        Log.i(TAG, "checkBulkOperations start");
        Sabres.deleteDatabase(new DeleteCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkBulkOperations: deleteDatabase successful");
                    saveActors(callback);
                }
            }
        });
    }

    private static void saveActors(final TestCallback callback) {
        final List<Actor> actors = createActors();
        SabresObject.saveAllInBackground(actors, new SaveCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkBulkOperations: saveAllInBackground successful");
                    checkActors(actors, callback);
                }
            }
        });


    }

    private static void checkActors(final List<Actor> actors, final TestCallback callback) {
        SabresQuery.getQuery(Actor.class).findInBackground(new FindCallback<Actor>() {
            @Override
            public void done(List<Actor> objects, SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkBulkOperations: findInBackground successful");
                    Assert.assertEquals(actors.size(), objects.size());
                    Assert.assertEquals(true, containsBenicio(objects));
                    fetchActors(actors, callback);
                }
            }
        });
    }

    private static void fetchActors(final List<Actor> actors, final TestCallback callback) {
        final List<Actor> actorsToFetch = new ArrayList<>(actors.size());
        for (Actor actor : actors) {
            Actor actorToFetch = SabresObject.createWithoutData(Actor.class, actor.getObjectId());
            actorsToFetch.add(actorToFetch);
        }

        SabresObject.fetchAllIfNeededInBackground(actorsToFetch, new FetchCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
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
                    deleteActors(actors, callback);
                }
            }
        });
    }

    private static void deleteActors(List<Actor> actors, final TestCallback callback) {
        SabresObject.deleteAllInBackground(actors, new DeleteCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkBulkOperations: deleteAllInBackground successful");
                    countActors(callback);
                }
            }
        });
    }

    private static void countActors(final TestCallback callback) {
        SabresQuery.getQuery(Actor.class).countInBackground(new CountCallback() {
            @Override
            public void done(Long count, SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkBulkOperations: countInBackground successful");
                    Assert.assertEquals((Long)0L, count);
                    Log.i(TAG, "checkBulkOperations successful");
                    callback.done(null);
                }
            }
        });
    }

    private static void createAndSaveFightClubMovie(final TestCallback callback) {
        final Movie fightClub = MovieController.createFightClub();
        fightClub.saveInBackground(new SaveCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkDataConsistency: saveInBackground successful");
                    queryFightClubMovie(callback, fightClub.getObjectId(), fightClub.getCreatedAt(),
                        fightClub.getUpdatedAt());
                }
            }
        });
    }

    private static void queryFightClubMovie(final TestCallback callback, long objectId,
        final Date createdAt, final Date updatedAt) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.getInBackground(objectId, new GetCallback<Movie>() {
            @Override
            public void done(Movie object, SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkDataConsistency: getInBackground successful");
                    try {
                        checkFightClubMovieObject(object, createdAt, updatedAt);
                    } catch (Exception ex) {
                        callback.done(ex);
                        return;
                    }

                    deleteFightClubMovie(callback, object);
                }
            }
        });
    }

    private static void deleteFightClubMovie(final TestCallback callback, Movie movie) {
        movie.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    callback.done(e);
                } else {
                    Log.i(TAG, "checkDataConsistency successful");
                    callback.done(null);
                }
            }
        });
    }

    private interface TestCallback {
        void done(Exception e);
    }
}
