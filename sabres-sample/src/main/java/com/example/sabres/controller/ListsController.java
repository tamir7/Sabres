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

import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class ListsController {
    private static final String TAG = ListsController.class.getSimpleName();

    public void setStaringToFightClub() {
        Movie.findWithTitleInBackground(FightClubController.TITLE).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                } else if (task.getResult().isEmpty()) {
                    return Task.forError(new RuntimeException("Fight Club movie does not exist"));
                } else {
                    Movie movie = task.getResult().get(0);
                    movie.setStarring(Arrays.asList("Brad Pitt", "Edward Norton",
                            "Helena Bonham Carter"));
                    return movie.saveInBackground();
                }
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to save starring to Fight Club movie", task.getError());
                } else {
                    Log.i(TAG, "Set starring to Fight Club successfully");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

    }

    public void getStarringFromFightClub() {
        Movie.findWithTitleInBackground(FightClubController.TITLE).continueWith(new Continuation<List<Movie>, Void>() {
            @Override
            public Void then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to get Fight Club movie", task.getError());
                } else if (task.getResult().isEmpty()) {
                    Log.w(TAG, "Fight Club movie does not exist");
                } else {
                    List<String> stars = task.getResult().get(0).getStarring();
                    Log.i(TAG, "Stars of Fight Club: " + Arrays.toString(stars.toArray()));
                }

                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void findByActors() {
        Movie.findWithActorsInBackground(Arrays.asList("Brad Pitt", "Edward Norton"))
            .continueWith(new Continuation<List<Movie>, Void>() {
                @Override
                public Void then(Task<List<Movie>> task) throws Exception {
                    if (task.isFaulted()) {
                        Log.e(TAG, "Error while searching for movies with actors", task.getError());
                    } else if (task.getResult().isEmpty()) {
                        Log.w(TAG, "Failed to find movie with actors");
                    } else {
                        for (Movie movie : task.getResult()) {
                            Log.i(TAG, String.format("Found movie: %s", movie.getTitle()));
                        }
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
    }
}
