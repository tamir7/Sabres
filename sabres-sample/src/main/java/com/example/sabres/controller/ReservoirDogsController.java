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
import com.sabres.FindCallback;
import com.sabres.SabresException;
import com.sabres.SaveCallback;

import java.util.List;

// Uses callbacks API
public class ReservoirDogsController {
    private static final String TAG = ReservoirDogsController.class.getSimpleName();
    static final String TITLE = "Reservoir Dogs";
    private static final Double RATING = 8.4;
    private static final Byte META_SCORE = 78;
    private static final Short YEAR = 1992;
    private static final Integer BUDGET = 1200000;
    private static final Long GROSS = 2812029l;
    private static final Boolean HAS_BRAD_PITT = false;

    public void createMovie() {
        Movie.findWithTitleInBackground(TITLE, new FindCallback<Movie>() {
            @Override
            public void done(List<Movie> objects, SabresException e) {
                if (e != null) {
                    Log.e(TAG, "Failed to create Reservoir Dogs Movie object", e);
                } else if (!objects.isEmpty()) {
                    Log.w(TAG, "Reservoir Dogs Movie object already exists");
                } else {
                    Movie movie = new Movie();
                    movie.setTitle(TITLE);
                    movie.setRating(RATING);
                    movie.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(SabresException e) {
                            if (e != null) {
                                Log.e(TAG, "Failed to create Reservoir Dogs Movie object", e);
                            } else {
                                Log.i(TAG, "Reservoir Dogs Movie object created successfully");
                            }
                        }
                    });
                }
            }
        });
    }

    public void modifyMovie() {
        Movie.findWithTitleInBackground(TITLE, new FindCallback<Movie>() {
            @Override
            public void done(List<Movie> objects, SabresException e) {
                if (e != null) {
                    Log.e(TAG, "Failed to modify Reservoir Dogs Movie object", e);
                } else if (objects.isEmpty()) {
                    Log.w(TAG, "Reservoir Dogs Movie object does not exist exists");
                } else {
                    Movie movie = objects.get(0);
                    movie.setYear(YEAR);
                    movie.setMetaScore(META_SCORE);
                    movie.setBudget(BUDGET);
                    movie.setGross(GROSS);
                    movie.setHasBradPitt(HAS_BRAD_PITT);
                    movie.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(SabresException e) {
                            if (e != null) {
                                Log.e(TAG, "Failed to update Reservoir Dogs Movie object", e);
                            } else {
                                Log.i(TAG, "Reservoir Dogs Movie object updated successfully");
                            }
                        }
                    });
                }
            }
        });
    }

    public void deleteMovie() {
        Movie.findWithTitleInBackground(TITLE, new FindCallback<Movie>() {
            @Override
            public void done(List<Movie> objects, SabresException e) {
                if (e != null) {
                    Log.e(TAG, "Failed to delete Reservoir Dogs Movie object", e);
                } else if (objects.isEmpty()) {
                    Log.w(TAG, "Reservoir Dogs Movie object does not exist exists");
                } else {
                    Movie movie = objects.get(0);
                    movie.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(SabresException e) {
                            if (e != null) {
                                Log.e(TAG, "Failed to delete Reservoir Dogs Movie object", e);
                            } else {
                                Log.i(TAG, "Reservoir Dogs Movie object deleted successfully");
                            }
                        }
                    });
                }
            }
        });
    }
}
