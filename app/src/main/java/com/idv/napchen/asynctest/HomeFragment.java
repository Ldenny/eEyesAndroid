package com.idv.napchen.asynctest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        final View coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
//        View btAdd = view.findViewById(R.id.btAdd);
//        btAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Having a CoordinatorLayout in your view hierarchy
//                // allows Snackbar to enable certain features,
//                // such as swipe-to-dismiss and
//                // automatically moving of widgets like FloatingActionButton.
//                Snackbar.make(
//                        coordinatorLayout,
//                        "Add button clicked",
//                        Snackbar.LENGTH_SHORT)
//                        .show();
//            }
//        });
        return view;
    }
}
