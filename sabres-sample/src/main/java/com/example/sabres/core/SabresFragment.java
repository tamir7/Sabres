package com.example.sabres.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sabres.R;
import com.example.sabres.controller.BasicTestsController;
import com.example.sabres.model.Actor;
import com.example.sabres.model.Director;
import com.example.sabres.model.Movie;
import com.sabres.Sabres;
import com.sabres.SabresObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SabresFragment extends Fragment {
    private final static String TAG = SabresFragment.class.getSimpleName();

    public SabresFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_sabres, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @OnClick(R.id.button_print_tables)
    public void onClickPrintTables() {
        Sabres.printTables();
    }

    @OnClick(R.id.button_print_indices)
    public void onClickPrintIndices() {
        Sabres.printIndices();
    }

    @OnClick(R.id.button_delete_database)
    public void onClickDeleteDatabase() {
        Sabres.deleteDatabase().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "Failed to delete database");
                } else {
                    Log.i(TAG, "Database deleted");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    @OnClick(R.id.button_print_movie_schema)
    public void onClickPrintMovieSchema() {
        Sabres.printSchema(Movie.class);
    }

    @OnClick(R.id.button_print_director_schema)
    public void onClickPrintDirectorSchema() {
        Sabres.printSchema(Director.class);
    }

    @OnClick(R.id.button_print_actor_schema)
    public void onClickPrintActorSchema() {
        Sabres.printSchema(Actor.class);
    }

    @OnClick(R.id.button_print_movies)
    public void onClickPrintMovies() {
        SabresObject.printAll(Movie.class);
    }

    @OnClick(R.id.button_print_directors)
    public void onClickPrintDirectors() {
        SabresObject.printAll(Director.class);
    }

    @OnClick(R.id.button_print_actors)
    public void onClickPrintActors() {
        SabresObject.printAll(Actor.class);
    }

    @OnClick(R.id.button_basic_tests)
    public void onClickBasicTests() {
        BasicTestsController.begin();
    }
}
