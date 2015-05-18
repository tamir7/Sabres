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

import java.util.Calendar;
import java.util.Date;

public class Actor extends SabresObject {
    private static final String NAME_KEY = "name";
    private static final String DATE_OF_BIRTH_KEY = "dateOfBirth";

    public String getName() {
        return getString(NAME_KEY);
    }

    public void setName(String name) {
        put(NAME_KEY, name);
    }

    public Date getDateOfBirth() {
        return getDate(DATE_OF_BIRTH_KEY);
    }

    public void setDateOfBirth(Date dateOfBirth) {
        put(DATE_OF_BIRTH_KEY, dateOfBirth);
    }

    public static class BradPitt {
        public static final String NAME = "Brad Pitt";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1963, 10, 18);
            DATE_OF_BIRTH = c.getTime();
        }

        private BradPitt() {
        }
    }

    public static class EdwardNorton {
        public static final String NAME = "Edward Norton";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1969, 8, 18);
            DATE_OF_BIRTH = c.getTime();
        }

        private EdwardNorton() {
        }
    }

    public static class HelenaBonhamCarter {
        public static final String NAME = "Helena Bonham Carter";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1966, 5, 26);
            DATE_OF_BIRTH = c.getTime();
        }

        private HelenaBonhamCarter() {
        }
    }

    public static class HarveyKeitel {
        public static final String NAME = "Harvey Keitel";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1939, 5, 13);
            DATE_OF_BIRTH = c.getTime();
        }

        private HarveyKeitel() {
        }
    }

    public static class TimRoth {
        public static final String NAME = "Tim Roth";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1961, 5, 14);
            DATE_OF_BIRTH = c.getTime();
        }

        private TimRoth() {
        }
    }

    public static class MichaelMadsen {
        public static final String NAME = "Michael Madsen";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1958, 9, 25);
            DATE_OF_BIRTH = c.getTime();
        }

        private MichaelMadsen() {
        }
    }

    public static class JasonStatham {
        public static final String NAME = "Jason Statham";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1967, 7, 26);
            DATE_OF_BIRTH = c.getTime();
        }

        private JasonStatham() {
        }
    }

    public static class BenicioDelToro {
        public static final String NAME = "Benicio Del Toro";
        public static final Date DATE_OF_BIRTH;

        static {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(1967, 2, 19);
            DATE_OF_BIRTH = c.getTime();
        }

        private BenicioDelToro() {
        }
    }
}
