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
import com.sabres.Sabres;

import java.util.List;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

public class QueryController {
    private static final String TAG = QueryController.class.getSimpleName();

    public void queryFightClubFetchDirector() {
        final Capture<Director> directorCapture = new Capture<>();
        Movie.findWithTitleInBackground(FightClubController.TITLE).continueWithTask(new Continuation<List<Movie>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Movie>> task) throws Exception {
                if (task.isFaulted()) {
                    return task.makeVoid();
                } else if (task.getResult().isEmpty()) {
                    return Task.forError(new IllegalStateException("Fight Club movie does not exist"));
                } else {
                    Director director = task.getResult().get(0).getDirector();
                    directorCapture.set(director);
                    return director.fetchInBackground();
                }
            }
        }).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "failed to get director from Fight Club Object");
                } else {
                    Log.i(TAG, String.format("Director of Fight Club movie is %s", directorCapture.get().getName()));
                }

                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void queryFightClubIncludeDirector() {
        Sabres.testFunction();
    }
}
