package com.example.sabres.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sabres.R;
import com.example.sabres.controller.FightClubController;
import com.example.sabres.controller.ReservoirDogsController;
import com.example.sabres.controller.SabresController;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SabresFragment extends Fragment {
    private final SabresController sabresController = new SabresController();
    private final FightClubController fightClubController = new FightClubController();
    private final ReservoirDogsController reservoirDogsController = new ReservoirDogsController();


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
        sabresController.printTables();
    }

    @OnClick(R.id.button_print_indices)
    public void onClickPrintIndices() {
        sabresController.printIndices();
    }

    @OnClick(R.id.button_print_schema)
    public void onClickPrintSchema() {
        sabresController.printSchema();
    }

    @OnClick(R.id.button_print_movies)
    public void onClickPrintMovies() {
        sabresController.printMovies();
    }

    @OnClick(R.id.button_create_fight_club_movie)
    public void onClickCreateFightClubMovie() {
        fightClubController.createMovie();
    }

    @OnClick(R.id.button_modify_fight_club_movie)
    public void onClickModifyFightClubMovie() {
        fightClubController.modifyMovie();
    }

    @OnClick(R.id.button_delete_fight_club_movie)
    public void onClickDeleteFightClubMovie() {
        fightClubController.deleteMovie();
    }

    @OnClick(R.id.button_create_reservoir_dogs_movie)
    public void onClickCreateReservoirDogsMovie() {
        reservoirDogsController.createMovie();
    }

    @OnClick(R.id.button_modify_reservoir_dogs_movie)
    public void onClickModifyReservoirDogsMovie() {
        reservoirDogsController.modifyMovie();
    }

    @OnClick(R.id.button_delete_reservoir_dogs_movie)
    public void onClickDeleteReservoirDogsMovie() {
        reservoirDogsController.deleteMovie();
    }
}
