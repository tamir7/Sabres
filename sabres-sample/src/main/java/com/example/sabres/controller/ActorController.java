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

import com.example.sabres.model.Actor;

public class ActorController {

    public static Actor createBradPitt() {
        Actor actor = new Actor();
        actor.setName(Actor.BradPitt.NAME);
        actor.setDateOfBirth(Actor.BradPitt.DATE_OF_BIRTH);
        return actor;
    }

    public static Actor createEdwardNorton() {
        Actor actor = new Actor();
        actor.setName(Actor.EdwardNorton.NAME);
        actor.setDateOfBirth(Actor.EdwardNorton.DATE_OF_BIRTH);
        return actor;
    }

    public static Actor createHelenaBonhamCarter() {
        Actor actor = new Actor();
        actor.setName(Actor.HelenaBonhamCarter.NAME);
        actor.setDateOfBirth(Actor.HelenaBonhamCarter.DATE_OF_BIRTH);
        return actor;
    }

    public static Actor createHarveyKeitel() {
        Actor actor = new Actor();
        actor.setName(Actor.HarveyKeitel.NAME);
        actor.setDateOfBirth(Actor.HarveyKeitel.DATE_OF_BIRTH);
        return actor;
    }

    public static Actor createTimRoth() {
        Actor actor = new Actor();
        actor.setName(Actor.TimRoth.NAME);
        actor.setDateOfBirth(Actor.TimRoth.DATE_OF_BIRTH);
        return actor;
    }

    public static Actor createMichaelMadsen() {
        Actor actor = new Actor();
        actor.setName(Actor.MichaelMadsen.NAME);
        actor.setDateOfBirth(Actor.MichaelMadsen.DATE_OF_BIRTH);
        return actor;
    }

    public static Actor createJasonStatham() {
        Actor actor = new Actor();
        actor.setName(Actor.JasonStatham.NAME);
        actor.setDateOfBirth(Actor.JasonStatham.DATE_OF_BIRTH);
        return actor;
    }

    public static Actor createBenicioDelToro() {
        Actor actor = new Actor();
        actor.setName(Actor.BenicioDelToro.NAME);
        actor.setDateOfBirth(Actor.BenicioDelToro.DATE_OF_BIRTH);
        return actor;
    }
}
