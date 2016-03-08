package com.example.totems;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

import totems.Totems;

public class MainActivity extends AppCompatActivity
        implements Totems.Listener, MasterFragment.Listener, DetailFragment.Listener {

    private static final String TAG_PLANETS = "MAIN_PLANETS";
    private ArrayList<CharSequence> planets;
    private Totems totems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totems = Totems.newTotems(this, getSupportFragmentManager(), R.id.left_pane, R.id.right_pane);

        if (savedInstanceState == null) {
            planets = new ArrayList<>();
            totems.push(Totems.LEFT_TOTEM, new MasterFragment());
        } else {
            planets = savedInstanceState.getCharSequenceArrayList(TAG_PLANETS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequenceArrayList(TAG_PLANETS, planets);
    }

    @Override
    public void totemEmpty(int totem) {
        switch (totem) {
            case Totems.LEFT_TOTEM:
                break;
            case Totems.RIGHT_TOTEM:
                break;
        }
    }

    @Override
    public void totemNoLongerEmpty(int totem) {
        switch (totem) {
            case Totems.LEFT_TOTEM:
                break;
            case Totems.RIGHT_TOTEM:
                break;
        }
    }

    @Override
    public void totemNewFragment(int totem, Fragment fragment) {
        switch (totem) {
            case Totems.LEFT_TOTEM:
                break;
            case Totems.RIGHT_TOTEM:
                break;
        }
    }

    @Override
    public void planetPicked(CharSequence planet) {
        if (planets.contains(planet)) {
            Toast.makeText(this, "Planet already added", Toast.LENGTH_SHORT).show();
            return;
        }

        planets.add(planet);
        DetailFragment fragment = new DetailFragment();

        Bundle args = new Bundle();
        args.putCharSequence(DetailFragment.ARG_TITLE, planet);
        fragment.setArguments(args);

        totems.push(Totems.RIGHT_TOTEM, fragment);
    }

    @Override
    public void close() {
        DetailFragment fragment = (DetailFragment)totems.peek(Totems.RIGHT_TOTEM);
        planets.remove(fragment.getTitle());
        totems.pop(Totems.RIGHT_TOTEM);
    }
}
