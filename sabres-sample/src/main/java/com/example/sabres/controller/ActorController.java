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

import bolts.Continuation;
import bolts.Task;

public class ActorController {
    private static Task<Actor> create(final Actor actor) {
        return actor.saveInBackground().continueWithTask(new Continuation<Void, Task<Actor>>() {
            @Override
            public Task<Actor> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    return Task.forError(task.getError());
                }

                return Task.forResult(actor);
            }
        });
    }

    public static Task<Actor> createBradPitt() {
        Actor actor = new Actor();
        actor.setName(Actor.BradPitt.NAME);
        actor.setDateOfBirth(Actor.BradPitt.DATE_OF_BIRTH);
        return create(actor);
    }

    public static Task<Actor> createEdwardNorton() {
        Actor actor = new Actor();
        actor.setName(Actor.EdwardNorton.NAME);
        actor.setDateOfBirth(Actor.EdwardNorton.DATE_OF_BIRTH);
        return create(actor);
    }

    public static Task<Actor> createHelenaBonhamCarter() {
        Actor actor = new Actor();
        actor.setName(Actor.HelenaBonhamCarter.NAME);
        actor.setDateOfBirth(Actor.HelenaBonhamCarter.DATE_OF_BIRTH);
        return create(actor);
    }

    public static Task<Actor> createHarveyKeitel() {
        Actor actor = new Actor();
        actor.setName(Actor.HarveyKeitel.NAME);
        actor.setDateOfBirth(Actor.HarveyKeitel.DATE_OF_BIRTH);
        return create(actor);
    }

    public static Task<Actor> createTimRoth() {
        Actor actor = new Actor();
        actor.setName(Actor.TimRoth.NAME);
        actor.setDateOfBirth(Actor.TimRoth.DATE_OF_BIRTH);
        return create(actor);
    }

    public static Task<Actor> createMichaelMadsen() {
        Actor actor = new Actor();
        actor.setName(Actor.MichaelMadsen.NAME);
        actor.setDateOfBirth(Actor.MichaelMadsen.DATE_OF_BIRTH);
        return create(actor);
    }

    public static Task<Actor> createJasonStatham() {
        Actor actor = new Actor();
        actor.setName(Actor.JasonStatham.NAME);
        actor.setDateOfBirth(Actor.JasonStatham.DATE_OF_BIRTH);
        return create(actor);
    }

    public static Task<Actor> createBenicioDelToro() {
        Actor actor = new Actor();
        actor.setName(Actor.BenicioDelToro.NAME);
        actor.setDateOfBirth(Actor.BenicioDelToro.DATE_OF_BIRTH);
        return create(actor);
    }
}
