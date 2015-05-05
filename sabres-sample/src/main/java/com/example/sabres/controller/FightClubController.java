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

import java.util.List;

import bolts.Continuation;
import bolts.Task;

// Uses Bolts Tasks API
public class FightClubController {
    private static final String NAME = "Fight Club";
    private static final Double RATING = 8.9;
    private static final Byte META_SCORE = 66;
    private static final Short YEAR = 1999;
    private static final Integer BUDGET = 63000000;
    private static final Long GROSS = 37023395l;
    private static final Boolean HAS_BRAD_PITT = true;

    private static final String TAG = FightClubController.class.getSimpleName();

    public void createMovie() {
        Movie.findWithTitleInBackground(NAME).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                        return task.makeVoid();
                }

                if (task.getResult().isEmpty()) {
                    Movie movie = new Movie();
                    movie.setTitle(NAME);
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
        Movie.findWithTitleInBackground(NAME).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
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
        Movie.findWithTitleInBackground(NAME).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
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
}
