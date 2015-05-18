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

import com.example.sabres.model.Movie;
import com.sabres.DeleteCallback;
import com.sabres.GetCallback;
import com.sabres.Sabres;
import com.sabres.SabresException;
import com.sabres.SabresQuery;
import com.sabres.SaveCallback;

import junit.framework.Assert;

import java.util.Date;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

public class BasicTestsController {
    private static final String TAG = BasicTestsController.class.getSimpleName();

    public static void beginWithTasks() {
        Sabres.deleteDatabase().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return checkDataConsistency();
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Basic Tests failed", task.getError());
                } else {
                    Log.i(TAG, "Basic Tests passed");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private static Task<Void> checkDataConsistency() {
        final Capture<Date> createdAtCapture = new Capture<>();
        final Capture<Date> updatedAtCapture = new Capture<>();
        final Capture<Movie> fightClubCapture = new Capture<>(MovieController.createFightClub());

        return Sabres.deleteDatabase().onSuccessTask(new Continuation<Void, Task<Void>>() {

            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return fightClubCapture.get().saveInBackground();
            }
        }).onSuccessTask(new Continuation<Void, Task<Movie>>() {
            @Override
            public Task<Movie> then(Task<Void> task) throws Exception {
                Movie fightClub = fightClubCapture.get();
                createdAtCapture.set(fightClub.getCreatedAt());
                updatedAtCapture.set(fightClub.getUpdatedAt());
                SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
                return q.getInBackground(fightClub.getObjectId());
            }
        }).onSuccessTask(new Continuation<Movie, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Movie> task) throws Exception {
                checkFightClubMovieObject(task.getResult(), createdAtCapture.get(),
                    updatedAtCapture.get());
                return Task.forResult(null);
            }
        });
    }

    public static void beginWithCallbacks() {

        Sabres.deleteDatabase(new DeleteCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    Log.e(TAG, "Basic Tests failed: delete database failed", e);
                } else {
                    final Movie fightClub = MovieController.createFightClub();
                    fightClub.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(SabresException e) {
                            if (e != null) {
                                Log.e(TAG, "Basic Tests failed: Failed to save movie object");
                            } else {
                                SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
                                q.getInBackground(fightClub.getObjectId(),
                                    new GetCallback<Movie>() {
                                        @Override
                                        public void done(Movie object, SabresException e) {
                                            if (e != null) {
                                                Log.e(TAG, "Basic Tests failed: Failed to get " +
                                                    "movie object");
                                            } else {
                                                try {
                                                    checkFightClubMovieObject(object,
                                                        fightClub.getCreatedAt(),
                                                        fightClub.getUpdatedAt());
                                                } catch (Exception ex) {
                                                    Log.e(TAG, "Basic Tests failed", ex);
                                                    return;
                                                }

                                                Log.i(TAG, "Basic Tests passed");
                                            }
                                        }
                                    });
                            }
                        }
                    });
                }
            }
        });

    }

    private static void checkFightClubMovieObject(Movie movie, Date createdAt, Date updatedAt) {
        Assert.assertEquals(Movie.FightClub.TITLE, movie.getTitle());
        Assert.assertEquals(Movie.FightClub.RATING, movie.getRating());
        Assert.assertEquals(Movie.FightClub.META_SCORE, movie.getMetaScore());
        Assert.assertEquals(Movie.FightClub.NOMINATIONS, movie.getNominations());
        Assert.assertEquals(Movie.FightClub.YEAR, movie.getYear());
        Assert.assertEquals(Movie.FightClub.BUDGET, movie.getBudget());
        Assert.assertEquals(Movie.FightClub.GROSS, movie.getGross());
        Assert.assertEquals(Movie.FightClub.NOMINATED_FOR_OSCAR, movie.isNominatedForOscar());
        Assert.assertEquals(createdAt, movie.getCreatedAt());
        Assert.assertEquals(updatedAt, movie.getUpdatedAt());
    }
}
