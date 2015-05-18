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
    private static final String NOMINATED_FOR_OSCAR_KEY = "nominatedForOscar";
    private static final String DIRECTOR_KEY = "director";
    private static final String STARRING_KEY = "starring";
    private static final String NOMINATIONS_KEY = "nominations";

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

    public static Task<List<Movie>> findWithActorInBackground(String actor) {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);
        q.whereEqualTo(STARRING_KEY, actor);
        return q.findInBackground();
    }

    public String getTitle() {
        return getString(TITLE_KEY);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
    }

    public Float getRating() {
        return getFloat(RATING_KEY);
    }

    public void setRating(Float rating) {
        put(RATING_KEY, rating);
    }

    public Byte getNominations() {
        return getByte(NOMINATIONS_KEY);
    }

    public void setNominations(Byte nomination) {
        put(NOMINATIONS_KEY, nomination);
    }

    public Short getYear() {
        return getShort(YEAR_KEY);
    }

    public void setYear(Short year) {
        put(YEAR_KEY, year);
    }

    public Double getMetaScore() {
        return getDouble(META_SCORE_KEY);
    }

    public void setMetaScore(Double metaScore) {
        put(META_SCORE_KEY, metaScore);
    }

    public Integer getBudget() {
        return getInt(BUDGET_KEY);
    }

    public void setBudget(Integer budget) {
        put(BUDGET_KEY, budget);
    }

    public Long getGross() {
        return getLong(GROSS_KEY);
    }

    public void setGross(Long gross) {
        put(GROSS_KEY, gross);
    }

    public Boolean isNominatedForOscar() {
        return getBoolean(NOMINATED_FOR_OSCAR_KEY);
    }

    public void setNominatedForOscar(Boolean hasOscarNominations) {
        put(NOMINATED_FOR_OSCAR_KEY, hasOscarNominations);
    }

    public Director getDirector() {
        return (Director)getSabresObject(DIRECTOR_KEY);
    }

    public void setDirector(Director director) {
        put(DIRECTOR_KEY, director);
    }

    public List<String> getStarring() {
        return getList(STARRING_KEY);
    }

    public void setStarring(List<String> starring) {
        addAll(STARRING_KEY, starring);
    }

    public static class FightClub {
        public static final String TITLE = "Fight Club";
        public static final Float RATING = 8.9F;
        public static final Double META_SCORE = 0.66;
        public static final Byte NOMINATIONS = 23;
        public static final Short YEAR = 1999;
        public static final Integer BUDGET = 63000000;
        public static final Long GROSS = 37023395L;
        public static final Boolean NOMINATED_FOR_OSCAR = true;

        private FightClub() {
        }
    }

    public static class ReservoirDogs {
        public static final String TITLE = "Reservoir Dogs";
        public static final Float RATING = 8.4F;
        public static final Double META_SCORE = 0.78;
        public static final Byte NOMINATIONS = 10;
        public static final Short YEAR = 1992;
        public static final Integer BUDGET = 1200000;
        public static final Long GROSS = 2812029L;
        public static final Boolean NOMINATED_FOR_OSCAR = true;

        private ReservoirDogs() {
        }
    }

    public static class Snatch {
        public static final String TITLE = "Snatch";
        public static final Float RATING = 8.3F;
        public static final Double META_SCORE = 0.55;
        public static final Byte NOMINATIONS = 5;
        public static final Short YEAR = 2000;
        public static final Integer BUDGET = 9390000;
        public static final Long GROSS = 30093107L;
        public static final Boolean NOMINATED_FOR_OSCAR = false;

        private Snatch() {
        }
    }
}
