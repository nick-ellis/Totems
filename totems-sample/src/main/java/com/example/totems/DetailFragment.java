package com.example.totems;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class DetailFragment extends Fragment {

    public static final String ARG_TITLE = "DETAIL_TITLE";

    public interface Listener {
        void close();
    }

    private Listener listener;

    TextView vTitle;
    Button vClose;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener)context;
        } else {
            throw new ClassCastException(context.toString() + " must implement listener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_detail, container, false);

        vTitle = (TextView)v.findViewById(R.id.title);
        vTitle.setText(getArguments().getCharSequence(ARG_TITLE, "?"));

        vClose = (Button)v.findViewById(R.id.close);
        vClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.close();
            }
        });

        return v;
    }

    public CharSequence getTitle() {
        return vTitle.getText();
    }
}
