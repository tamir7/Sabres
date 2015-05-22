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

import com.example.sabres.model.Actor;
import com.example.sabres.model.Director;
import com.example.sabres.model.Movie;
import com.sabres.SabresObject;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

abstract class AbstractTestController {
    private static final String TAG = AbstractTestController.class.getSimpleName();

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

    protected static boolean containsBenicio(List<Actor> actors) {
        for (Actor actor : actors) {
            if (Actor.BenicioDelToro.NAME.equals(actor.getName())) {
                return true;
            }
        }

        return false;
    }

    protected static void checkNonDatabaseApi() {
        Log.i(TAG, "checkNonDatabaseApi start");
        Director director = SabresObject.create(Director.class);
        Assert.assertEquals(false, director.isDataAvailable());
        Assert.assertEquals(false, director.containsKey(Director.getNameKey()));
        Assert.assertEquals(false, director.isDirty());
        Assert.assertEquals(false, director.isDirty(Director.getNameKey()));
        director.setName(Director.GuyRitchie.NAME);
        Assert.assertEquals(true, director.containsKey(Director.getNameKey()));
        Assert.assertEquals(true, director.isDirty());
        Assert.assertEquals(false, director.isDirty(Director.getDateOfBirthKey()));
        Log.i(TAG, "checkNonDatabaseApi successful");
    }

    protected static List<Actor> createActors() {
        List<Actor> actors = new ArrayList<>();
        actors.add(ActorController.createBenicioDelToro());
        actors.add(ActorController.createBradPitt());
        actors.add(ActorController.createEdwardNorton());
        actors.add(ActorController.createHarveyKeitel());
        actors.add(ActorController.createHelenaBonhamCarter());
        actors.add(ActorController.createJasonStatham());
        actors.add(ActorController.createMichaelMadsen());
        actors.add(ActorController.createTimRoth());
        return actors;
    }
}
