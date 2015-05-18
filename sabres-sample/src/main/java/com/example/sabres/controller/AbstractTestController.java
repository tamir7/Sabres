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

import com.example.sabres.model.Movie;

import junit.framework.Assert;

import java.util.Date;

abstract class AbstractTestController {

    protected static void checkFightClubMovieObject(Movie movie, Date createdAt, Date updatedAt) {
        Assert.assertEquals(Movie.FightClub.TITLE, movie.getTitle());
        Assert.assertEquals(Movie.FightClub.RATING, movie.getRating());
        Assert.assertEquals(Movie.FightClub.META_SCORE, movie.getMetaScore());
        Assert.assertEquals(Movie.FightClub.NOMINATIONS, movie.getNominations());
        Assert.assertEquals(Movie.FightClub.YEAR, movie.getYear());
        Assert.assertEquals(Movie.FightClub.BUDGET, movie.getBudget());
        Assert.assertEquals(Movie.FightClub.GROSS, movie.getGross());
        Assert.assertEquals(Movie.FightClub.NOMINATED_FOR_OSCAR, movie.isNominatedForOscar());
        Assert.assertEquals(createdAt, movie.getCreatedAt());
        Assert.assertEquals(updatedAt, movie.getUpdatedAt());
    }
}
