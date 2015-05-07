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

import com.sabres.SabresObject;
import com.sabres.SabresQuery;

import java.util.Date;
import java.util.List;

import bolts.Task;

public class Director extends SabresObject {
    private static final String NAME_KEY = "name";
    private static final String DATE_OF_BIRTH_KEY = "dateOfBirth";

    public void setName(String name) {
        put(NAME_KEY, name);
    }

    public void setDateOfBirth(Date dateOfBirth) {
        put(DATE_OF_BIRTH_KEY, dateOfBirth);
    }

    public String getName() {
        return getString(NAME_KEY);
    }

    public Date getDateOfBirth() {
        return getDate(DATE_OF_BIRTH_KEY);
    }

    public static Task<List<Director>> findWithNameInBackground(String name) {
        SabresQuery<Director> q = SabresQuery.getQuery(Director.class);
        q.whereEqualTo(NAME_KEY, name);
        return q.findInBackground();
    }
}
