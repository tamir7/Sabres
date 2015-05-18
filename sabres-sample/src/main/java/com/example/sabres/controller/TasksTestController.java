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

import java.util.Date;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

public class TasksTestController extends AbstractTestController {
    private static final String TAG = TasksTestController.class.getSimpleName();

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
}
