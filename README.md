# Sabres

Sabres is an ORM library that exposes a [Parse]-like API.

## Features

+ **Schemaless**: No need to define a schema for your objects. `Sabres` will dynamicly update schema changes.   

+ **Auto indexing**: No need to define indices. `Sabres` will handle that for you.

+ **No Migration**: As schemas are dynamic, there are no "database versions".

+ **No Sqlite**: the API fully abstracts all Sqlite interfaces, while still giving you most of the functionality.

+ **Simple and powerfull API**: Well... everyone says that about their libraries, but don't take my word for it, just scroll down a bit.

## Quick Start

Create your model by extending [SabresObject].

```java
public class Movie extends SabresObject {
    private static final String TITLE_KEY = "title";
    private static final String YEAR_KEY = "year";

    public String getTitle() {
        return getString(TITLE_KEY);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
    }

    public Short getYear() {
        return getShort(YEAR_KEY);
    }

    public void setYear(Short year) {
        put(YEAR_KEY, year);
    }
```

Initialize Sabres and register your model classes. 

```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SabresObject.registerSubclass(Movie.class);
        Sabres.initialize(this);
    }
```

Create a new Movie object and save it to Sabres:

```java
Movie movie = new Movie();
movie.setTitle("Fight Club");
movie.setYear(1999);
movie.saveInBackground(new SaveCallback() {
    @Override
    public void done(SabresException e) {
        if (e == null) {
            // save was successful
        } else {
            // Save failed 
        }
      }
});
```

Query the Sabres Database with the [SabresQuery] object:

```java
  SabresQuery<Movie> query = SabresQuery.getQuery(Movie.class);
  query.whereEqualTo(TITLE_KEY, title);
  query.findInBackground(new FindCallback<Movie>() {
          @Override
          public void done(List<Movie> objects, SabresException e) {
          if (e == null) {
              if (objects.isEmpty()) {
                  // did not find any objects matching query
              } else {
                  // found some objects
              }
          } else {
              // query failed.
          }
});
```

Please see [Wiki] for full documentation or [Javadoc] for... well.. for Javadoc.

## Installation

Right now, the jar is hosted on my Bintray account. I'll upload it to jcenter when I deem it stable enough.

```java
repositories {
    maven { url 'http://dl.bintray.com/tamir7/maven' }
}

 compile 'com.sabres:sabres:0.9.3@aar'
```

## License

    Copyright 2015 Tamir Shomer

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[Javadoc]: http://tamir7.github.io/Sabres/
[Wiki]: https://github.com/tamir7/Sabres/wiki
[Parse]: http://www.parse.com
[SabresObject]: https://github.com/tamir7/Sabres/blob/master/sabres/src/main/java/com/sabres/SabresObject.java
[SabresQuery]: https://github.com/tamir7/Sabres/blob/master/sabres/src/main/java/com/sabres/SabresQuery.java
