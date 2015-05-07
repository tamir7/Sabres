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

import com.example.sabres.model.Director;
import com.example.sabres.model.Movie;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

// Uses Bolts Tasks API
public class FightClubController {
    private static final String TITLE = "Fight Club";
    private static final String DIRECTOR_NAME = "David Fincher";
    private static final Double RATING = 8.9;
    private static final Byte META_SCORE = 66;
    private static final Short YEAR = 1999;
    private static final Integer BUDGET = 63000000;
    private static final Long GROSS = 37023395l;
    private static final Boolean HAS_BRAD_PITT = true;
    static final Date DATE_OF_BIRTH;
    static {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(1962, 8, 28);
        DATE_OF_BIRTH = c.getTime();
    }

    private static final String TAG = FightClubController.class.getSimpleName();

    public void createMovie() {
        Movie.findWithTitleInBackground(TITLE).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                        return task.makeVoid();
                }

                if (task.getResult().isEmpty()) {
                    Movie movie = new Movie();
                    movie.setTitle(TITLE);
                    movie.setRating(RATING);
                    return movie.saveInBackground();
                }

                // already exists
                return Task.forResult(null);
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to create Fight Club Movie object", task.getError());
                } else {
                    Log.i(TAG, "Fight Club Movie object successfully created");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void modifyMovie() {
        Movie.findWithTitleInBackground(TITLE).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                } else {
                    Movie movie = task.getResult().get(0);
                    movie.setYear(YEAR);
                    movie.setMetaScore(META_SCORE);
                    movie.setBudget(BUDGET);
                    movie.setGross(GROSS);
                    movie.setHasBradPitt(HAS_BRAD_PITT);
                    return movie.saveInBackground();
                }
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to modify Fight Club movie", task.getError());
                } else {
                    Log.i(TAG, "Modified Fight Club movie successfully");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void deleteMovie() {
        Movie.findWithTitleInBackground(TITLE).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                } else {
                    Movie movie = task.getResult().get(0);
                    return movie.deleteInBackground();
                }
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to delete Fight Club movie", task.getError());
                } else {
                    Log.i(TAG, "Deleted Fight Club movie successfully");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void setDirector() {
        final Capture<Movie> movieCapture = new Capture<>();
        Movie.findWithTitleInBackground(TITLE).
                continueWithTask(new Continuation<List<Movie>, Task<List<Director>>>() {
                    @Override
                    public Task<List<Director>> then(Task<List<Movie>> task) throws Exception {
                        if (task.isFaulted()) {
                            return Task.forError(task.getError());
                        } else if (task.getResult().isEmpty()) {
                            return Task.forError(new IllegalStateException("Fight Club object does not exist."));
                        } else {
                            movieCapture.set(task.getResult().get(0));
                            return Director.findWithNameInBackground(DIRECTOR_NAME);
                    }
                }
    }).continueWithTask(new Continuation<List<Director>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Director>> task) throws Exception {
                if (task.isFaulted()) {
                    return Task.forError(task.getError());
                }
                Movie movie = movieCapture.get();
                Director director;

                if (task.getResult().isEmpty()) {
                    director = new Director();
                    director.setName(DIRECTOR_NAME);
                    director.setDateOfBirth(DATE_OF_BIRTH);
                } else {
                    director = task.getResult().get(0);
                }

                movie.setDirector(director);
                return movie.saveInBackground();
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to set Director to Fight Club Movie",
                            task.getError());
                } else {
                    Log.i(TAG, "Successfully set Director to Fight Club Movie");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }
}
