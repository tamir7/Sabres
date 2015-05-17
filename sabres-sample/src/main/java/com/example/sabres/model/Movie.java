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
    private static final String RATING_KEY = "rating";
    private static final String YEAR_KEY = "year";
    private static final String META_SCORE_KEY = "metaScore";
    private static final String BUDGET_KEY = "budget";
    private static final String GROSS_KEY = "gross";
    private static final String HAS_BRAD_PITT_KEY = "hasBradPitt";
    private static final String DIRECTOR_KEY = "director";
    private static final String STARRING_KEY = "starring";

    public String getTitle() {
        return getString(TITLE_KEY);
    }

    public Double getIMDBRating() {
        return getDouble(RATING_KEY);
    }

    public Short getYear() {
        return getShort(YEAR_KEY);
    }

    public void setYear(Short year) {
        put(YEAR_KEY, year);
    }

    public Byte getMetaScore() {
        return getByte(META_SCORE_KEY);
    }

    public Integer getBudget() {
        return getInt(BUDGET_KEY);
    }

    public void setMetaScore(Byte metaScore) {
        put(META_SCORE_KEY, metaScore);
    }

    public void setBudget(Integer budget) {
        put(BUDGET_KEY, budget);
    }

    public void setGross(Long gross) {
        put(GROSS_KEY, gross);
    }

    public Long getGross() {
        return getLong(GROSS_KEY);
    }

    public Boolean hasBradPitt() {
        return getBoolean(HAS_BRAD_PITT_KEY);
    }

    public void setHasBradPitt(Boolean hasBradPitt) {
        put(HAS_BRAD_PITT_KEY, hasBradPitt);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
    }

    public void setDirector(Director director) {
        put(DIRECTOR_KEY, director);
    }

    public Director getDirector() {
        return (Director)getSabresObject(DIRECTOR_KEY);
    }

    public void setRating(double rating) {
        put(RATING_KEY, rating);
    }

    public void setStarring(List<String> starring) {
        addAll(STARRING_KEY, starring);
    }

    public List<String> getStarring() {
        return getList(STARRING_KEY);
    }

    public static Task<List<Movie>> findWithTitleInBackground(String title) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.whereEqualTo(TITLE_KEY, title);
        return q.findInBackground();
    }

    public static Task<List<Movie>> findWithTitleInBackgroundIncludeDirector(String title) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.include(DIRECTOR_KEY);
        q.whereEqualTo(TITLE_KEY, title);
        return q.findInBackground();
    }

    public static void findWithTitleInBackground(String title, final FindCallback<Movie> callback) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.whereEqualTo(TITLE_KEY, title);
        q.findInBackground(callback);
    }

    public static Task<List<Movie>> findWithActorsInBackground(List<String> actors) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.whereContainsAll(STARRING_KEY, actors);
        return q.findInBackground();
    }
}
