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

public class MovieController {

    public static Movie createFightClub() {
        Movie movie = new Movie();
        movie.setTitle(Movie.FightClub.TITLE);
        movie.setRating(Movie.FightClub.RATING);
        movie.setMetaScore(Movie.FightClub.META_SCORE);
        movie.setNominations(Movie.FightClub.NOMINATIONS);
        movie.setYear(Movie.FightClub.YEAR);
        movie.setBudget(Movie.FightClub.BUDGET);
        movie.setGross(Movie.FightClub.GROSS);
        movie.setNominatedForOscar(Movie.FightClub.NOMINATED_FOR_OSCAR);
        return movie;
    }

    public static Movie createReservoirDogs() {
        Movie movie = new Movie();
        movie.setTitle(Movie.ReservoirDogs.TITLE);
        movie.setRating(Movie.ReservoirDogs.RATING);
        movie.setMetaScore(Movie.ReservoirDogs.META_SCORE);
        movie.setNominations(Movie.ReservoirDogs.NOMINATIONS);
        movie.setYear(Movie.ReservoirDogs.YEAR);
        movie.setBudget(Movie.ReservoirDogs.BUDGET);
        movie.setGross(Movie.ReservoirDogs.GROSS);
        movie.setNominatedForOscar(Movie.ReservoirDogs.NOMINATED_FOR_OSCAR);
        return movie;
    }

    public static Movie createSnatch() {
        Movie movie = new Movie();
        movie.setTitle(Movie.Snatch.TITLE);
        movie.setRating(Movie.Snatch.RATING);
        movie.setMetaScore(Movie.Snatch.META_SCORE);
        movie.setNominations(Movie.Snatch.NOMINATIONS);
        movie.setYear(Movie.Snatch.YEAR);
        movie.setBudget(Movie.Snatch.BUDGET);
        movie.setGross(Movie.Snatch.GROSS);
        movie.setNominatedForOscar(Movie.Snatch.NOMINATED_FOR_OSCAR);
        return movie;
    }
}
