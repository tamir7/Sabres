package com.example.sabres.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sabres.R;
import com.example.sabres.model.Movie;
import com.sabres.Sabres;
import com.sabres.SabresQuery;

import bolts.Continuation;
import bolts.Task;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SabresFragment extends Fragment {

    public SabresFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_sabres, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @OnClick(R.id.button_action)
    public void onClickActionButton() {
        Movie movie = new Movie();
        movie.setTitle("Fight Club");
        movie.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(getClass().getSimpleName(), "Failed to saveInBackground",
                            task.getError());
                } else {
                    Log.e(getClass().getSimpleName(), "saveInBackground success");
                }
                return null;
            }
        });
    }

    @OnClick(R.id.button_print)
    public void onClickPrintButton() {
        SabresQuery<Movie> q = SabresQuery.getQuery(Movie.class);

        q.getInBackground(1).continueWith(new Continuation<Movie, Void>() {
            @Override
            public Void then(Task task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(getClass().getSimpleName(), "getInBackground failed", task.getError());
                } else {
                    Log.e(getClass().getSimpleName(), String.format("%s\n%s",
                            Movie.class.getSimpleName(), task.getResult()));
            }

            return null;
        }
    }, Task.UI_THREAD_EXECUTOR);

        Sabres.printTables();
    }
}
