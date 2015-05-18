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
import com.sabres.Sabres;
import com.sabres.SabresQuery;

import junit.framework.Assert;

import bolts.Continuation;
import bolts.Task;

public class BasicTestsController {
    private static final String TAG = BasicTestsController.class.getSimpleName();

    public static void begin() {
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

    private static void checkFightClubMovieObject(Movie movie) {
        Assert.assertEquals(Movie.FightClub.TITLE, movie.getTitle());
        Assert.assertEquals(Movie.FightClub.RATING, movie.getRating());
        Assert.assertEquals(Movie.FightClub.META_SCORE, movie.getMetaScore());
        Assert.assertEquals(Movie.FightClub.NOMINATIONS, movie.getNominations());
        Assert.assertEquals(Movie.FightClub.YEAR, movie.getYear());
        Assert.assertEquals(Movie.FightClub.BUDGET, movie.getBudget());
        Assert.assertEquals(Movie.FightClub.GROSS, movie.getGross());
        Assert.assertEquals(Movie.FightClub.NOMINATED_FOR_OSCAR, movie.isNominatedForOscar());
    }

    private static Task<Void> checkDataConsistency() {
        return Sabres.deleteDatabase().onSuccessTask(new Continuation<Void, Task<Movie>>() {
            @Override
            public Task<Movie> then(Task<Void> task) throws Exception {
                return MovieController.createFightClub();
            }
        }).onSuccessTask(new Continuation<Movie, Task<Movie>>() {
            @Override
            public Task<Movie> then(Task<Movie> task) throws Exception {
                SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
                return q.getInBackground(task.getResult().getObjectId());
            }
        }).onSuccessTask(new Continuation<Movie, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Movie> task) throws Exception {
                checkFightClubMovieObject(task.getResult());
                return Task.forResult(null);
            }
        });
    }
}
