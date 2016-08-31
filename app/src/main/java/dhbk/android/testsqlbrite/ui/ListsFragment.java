package dhbk.android.testsqlbrite.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import dhbk.android.testsqlbrite.R;
import dhbk.android.testsqlbrite.TodoApp;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT;

public class ListsFragment extends Fragment {
    // inject the brite db
    @Inject
    BriteDatabase db;

    @BindView(android.R.id.list)
    ListView listView;
    @BindView(android.R.id.empty)
    View emptyView;


    private Listener listener;
    private ListsAdapter adapter;

    /**
     * declare field subcription, so you can unsubcribe it later
     */
    private Subscription subscription;

    public ListsFragment() {
    }

    public static ListsFragment newInstance() {
        ListsFragment fragment = new ListsFragment();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }

        super.onAttach(activity);

        // how to inject the dagger to this class
        TodoApp.getComponent(activity).inject(this);
        setHasOptionsMenu(true);

        listener = (Listener) activity;
        adapter = new ListsAdapter(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.add(R.string.new_list)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        listener.onNewListClicked();
                        return true;
                    }
                });
        MenuItemCompat.setShowAsAction(item, SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lists, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);
    }

    @OnItemClick(android.R.id.list)
    void listClicked(long listId) {
        listener.onListClicked(listId);
    }

    /**
     * subscribe
     */
    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle("To-Do");

        /**
         * query the table
         * mapToList: translate  each row in cursor to a list of object
         * note that when use this method -  if the db is very large, it's make this method is very comsume time because translate to each object.
         *
         *  remember to turn on the UI thread
         */
        subscription = db.createQuery(ListsItem.TABLES, ListsItem.QUERY)
                .mapToList(ListsItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter);
        // subscribe to an action
    }

    // unsubcribe
    @Override
    public void onPause() {
        super.onPause();
        subscription.unsubscribe();
    }

    interface Listener {
        void onListClicked(long id);

        void onNewListClicked();
    }


}
