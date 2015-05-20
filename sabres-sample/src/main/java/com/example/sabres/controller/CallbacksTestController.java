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

import java.util.Date;

public class CallbacksTestController extends AbstractTestController {
    private static final String TAG = CallbacksTestController.class.getSimpleName();

    public static void begin() {
        Sabres.deleteDatabase(new DeleteCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    Log.e(TAG, "Basic Tests failed: delete database failed", e);
                } else {
                    createAndSaveFightClubMovie();
                }
            }
        });
    }

    private static void createAndSaveFightClubMovie() {
        final Movie fightClub = MovieController.createFightClub();
        fightClub.saveInBackground(new SaveCallback() {
            @Override
            public void done(SabresException e) {
                if (e != null) {
                    Log.e(TAG, "Basic Tests failed: Failed to save movie object");
                } else {
                    queryFightClubMovie(fightClub.getObjectId(), fightClub.getCreatedAt(),
                        fightClub.getUpdatedAt());
                }
            }
        });
    }

    private static void queryFightClubMovie(long objectId, final Date createdAt,
        final Date updatedAt) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.getInBackground(objectId, new GetCallback<Movie>() {
            @Override
            public void done(Movie object, SabresException e) {
                if (e != null) {
                    Log.e(TAG, "Basic Tests failed: Failed to get " +
                        "movie object");
                } else {
                    try {
                        checkFightClubMovieObject(object,
                            createdAt,
                            updatedAt);
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
