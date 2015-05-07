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

public class QuentinController {
    private static final String TAG = QuentinController.class.getSimpleName();
    private static final String NAME = "Quentin Tarantino";
    private static final Date INCORRECT_DATE_OF_BIRTH;
    static final Date CORRECT_DATE_OF_BIRTH;
     static {
         Calendar c = Calendar.getInstance();
         c.clear();
         c.set(1963, 3, 27);
         CORRECT_DATE_OF_BIRTH = c.getTime();
         c.clear();
         c.set(1954, 2, 12);
         INCORRECT_DATE_OF_BIRTH = c.getTime();
    }


    public void createDirector() {
        Director.findWithNameInBackground(NAME).continueWithTask(new Continuation<List<Director>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Director>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                }

                if (task.getResult().isEmpty()) {
                    Director director = new Director();
                    director.setName(NAME);
                    director.setDateOfBirth(INCORRECT_DATE_OF_BIRTH);
                    return director.saveInBackground();
                }

                // already exists
                return Task.forResult(null);
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to create Quentin Director object", task.getError());
                } else {
                    Log.i(TAG, "Quentin Director object successfully created");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

    }

    public void deleteDirector() {
        Director.findWithNameInBackground(NAME).continueWithTask(new Continuation<List<Director>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Director>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                } else {
                    Director director = task.getResult().get(0);
                    return director.deleteInBackground();
                }
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to delete Quentin Director object", task.getError());
                } else {
                    Log.i(TAG, "Deleted Quentin Director successfully");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

    }

    public void setToMovie() {
        final Capture<Movie> movieCapture = new Capture<>();
        Movie.findWithTitleInBackground(ReservoirDogsController.TITLE).
                continueWithTask(new Continuation<List<Movie>, Task<List<Director>>>() {
                    @Override
                    public Task<List<Director>> then(Task<List<Movie>> task) throws Exception {
                        if (task.isFaulted()) {
                            return Task.forError(task.getError());
                        } else {
                            movieCapture.set(task.getResult().get(0));
                            return Director.findWithNameInBackground(NAME);
                        }
                    }
                }).continueWithTask(new Continuation<List<Director>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Director>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                } else {
                    Movie movie = movieCapture.get();
                    Director director = task.getResult().get(0);
                    director.setDateOfBirth(CORRECT_DATE_OF_BIRTH);
                    movie.setDirector(director);
                    return movie.saveInBackground();
                }
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to attach Quentin Director to Reservoir Dogs Movie",
                            task.getError());
                } else {
                    Log.i(TAG, "Successfully attached Quentin Director to Reservoir Dogs Movie");
                }
                return null;
            }
        });
    }
}
