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

public class BoltsSabresController extends AbstractSabresController {
    private static final String TAG = BoltsSabresController.class.getSimpleName();

    @Override
    public void createFightClubMovie() {
        Movie.findWithTitleInBackground("Fight Club").continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                        return task.makeVoid();
                }

                if (task.getResult().isEmpty()) {
                    Movie movie = new Movie();
                    movie.setTitle("Fight Club");
                    movie.setImdbRating(8.9);
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

    @Override
    public void modifyFightClubMovie() {
        Movie.findWithTitleInBackground("Fight Club").continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                } else {
                    Movie movie = task.getResult().get(0);
                    movie.setYear(Short.valueOf("1999"));
                    movie.setMetaScore(Byte.valueOf("66"));
                    movie.setBudget(63000000);
                    movie.setGross(Long.valueOf("37023395"));
                    movie.setHasBradPitt(true);
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

    @Override
    public void deleteFightClubMovie() {
        Movie.findWithTitleInBackground("Fight Club").continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
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
