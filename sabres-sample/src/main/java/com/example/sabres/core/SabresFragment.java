package com.example.sabres.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sabres.R;
import com.example.sabres.controller.AbstractSabresController;
import com.example.sabres.controller.BoltsSabresController;
import com.example.sabres.controller.CallbacksSabresController;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SabresFragment extends Fragment {
    private boolean useBoltsController = true;
    private final AbstractSabresController controller =
            useBoltsController ? new BoltsSabresController() : new CallbacksSabresController();

    public SabresFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_sabres, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @OnClick(R.id.button_print_tables)
    public void onClickPrintTables() {
        controller.printTables();
    }

    @OnClick(R.id.button_print_schema)
    public void onClickPrintSchema() {
        controller.printSchema();
    }

    @OnClick(R.id.button_print_movies)
    public void onClickPrintMovies() {
        controller.printMovies();
    }

    @OnClick(R.id.button_create_fight_club_movie)
    public void onClickCreateFightClubMovie() {
        controller.createFightClubMovie();
    }

    @OnClick(R.id.button_modify_fight_club_movie)
    public void onClickModifyFightClubMovie() {
        controller.modifyFightClubMovie();
    }

    @OnClick(R.id.button_delete_fight_club_movie)
    public void onClickDeleteFightClubMovie() {
        controller.deleteFightClubMovie();
    }
}
