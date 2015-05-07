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

import com.example.sabres.model.Director;
import com.example.sabres.model.Movie;
import com.sabres.Sabres;
import com.sabres.SabresObject;

public class SabresController {
    public void printTables() {
        Sabres.printTables();
    }

    public void printIndices() {
        Sabres.printIndices();
    }

    public void printSchema() {
        Sabres.printSchemaTable();
    }

    public void printMovies() {
        SabresObject.printAll(Movie.class);
    }

    public void printDirectors() {
        SabresObject.printAll(Director.class);
    }
}
