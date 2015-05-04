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

package com.example.sabres.model;

import com.sabres.FindCallback;
import com.sabres.SabresObject;
import com.sabres.SabresQuery;

import java.util.List;

import bolts.Task;

public class Movie extends SabresObject {
    private static final String TITLE_KEY = "title";
    private static final String IMDB_RATING_KEY = "imdb_rating";

    public String getTitle() {
        return getString(TITLE_KEY);
    }

    public Double getImdbRating() {
        return getDouble(IMDB_RATING_KEY);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
    }

    public void setImdbRating(double imdbRating) {
        put(IMDB_RATING_KEY, imdbRating);
    }

    public static Task<List<Movie>> findWithTitleInBackground(String title) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.whereEqualTo(TITLE_KEY, title);
        return q.findInBackground();
    }

    public static void findWithTitleInBackground(String title, final FindCallback<Movie> callback) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.whereEqualTo(TITLE_KEY, title);
        q.findInBackground(callback);
    }
}
