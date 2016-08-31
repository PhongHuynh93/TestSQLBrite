package dhbk.android.testsqlbrite.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dhbk.android.testsqlbrite.R;

public class NewListFragment extends Fragment {

    public NewListFragment() {
    }


    public static NewListFragment newInstance() {
        NewListFragment fragment = new NewListFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_list, container, false);
    }
}
