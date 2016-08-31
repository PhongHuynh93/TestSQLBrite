package dhbk.android.testsqlbrite.ui;


import android.app.Activity;
import android.database.Cursor;
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

import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dhbk.android.testsqlbrite.R;
import dhbk.android.testsqlbrite.TodoApp;
import dhbk.android.testsqlbrite.db.Db;
import dhbk.android.testsqlbrite.db.TodoItem;
import dhbk.android.testsqlbrite.db.TodoList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemsFragment extends Fragment {
    private static final String KEY_LIST_ID = "list_id";
    private static final String LIST_QUERY = "SELECT * FROM "
            + TodoItem.TABLE
            + " WHERE "
            + TodoItem.LIST_ID
            + " = ? ORDER BY "
            + TodoItem.COMPLETE
            + " ASC";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM "
            + TodoItem.TABLE
            + " WHERE "
            + TodoItem.COMPLETE
            + " = "
            + Db.BOOLEAN_FALSE
            + " AND "
            + TodoItem.LIST_ID
            + " = ?";
    private static final String TITLE_QUERY =
            "SELECT " + TodoList.NAME + " FROM " + TodoList.TABLE + " WHERE " + TodoList.ID + " = ?";

    public interface Listener {
        void onNewItemClicked(long listId);
    }

    public static ItemsFragment newInstance(long listId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_LIST_ID, listId);

        ItemsFragment fragment = new ItemsFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    public ItemsFragment() {
        // Required empty public constructor
    }


    @Inject
    BriteDatabase db;

    @BindView(android.R.id.list)
    ListView listView;
    @BindView(android.R.id.empty)
    View emptyView;

    private Listener listener;
    private ItemsAdapter adapter;
    private CompositeSubscription subscriptions;

    private long getListId() {
        return getArguments().getLong(KEY_LIST_ID);
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }

        super.onAttach(activity);
        TodoApp.getComponent(activity).inject(this);
        setHasOptionsMenu(true);

        listener = (Listener) activity;
        adapter = new ItemsAdapter(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.add(R.string.new_item)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        listener.onNewItemClicked(getListId());
                        return true;
                    }
                });
        MenuItemCompat.setShowAsAction(item, SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_items, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);

        RxAdapterView.itemClickEvents(listView) //
                .observeOn(Schedulers.io())
                .subscribe(new Action1<AdapterViewItemClickEvent>() {
                    @Override
                    public void call(AdapterViewItemClickEvent event) {
                        boolean newValue = !adapter.getItem(event.position()).complete();
                        db.update(TodoItem.TABLE, new TodoItem.Builder().complete(newValue).build(),
                                TodoItem.ID + " = ?", String.valueOf(event.id()));
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        String listId = String.valueOf(getListId());

        subscriptions = new CompositeSubscription();

        Observable<Integer> itemCount = db.createQuery(TodoItem.TABLE, COUNT_QUERY, listId) //
                .map(new Func1<SqlBrite.Query, Integer>() {
                    @Override
                    public Integer call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        try {
                            if (!cursor.moveToNext()) {
                                throw new AssertionError("No rows");
                            }
                            return cursor.getInt(0);
                        } finally {
                            cursor.close();
                        }
                    }
                });
        Observable<String> listName =
                db.createQuery(TodoList.TABLE, TITLE_QUERY, listId).map(new Func1<SqlBrite.Query, String>() {
                    @Override
                    public String call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        try {
                            if (!cursor.moveToNext()) {
                                throw new AssertionError("No rows");
                            }
                            return cursor.getString(0);
                        } finally {
                            cursor.close();
                        }
                    }
                });
        subscriptions.add(
                Observable.combineLatest(listName, itemCount, new Func2<String, Integer, String>() {
                    @Override
                    public String call(String listName, Integer itemCount) {
                        return listName + " (" + itemCount + ")";
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String title) {
                                getActivity().setTitle(title);
                            }
                        }));

        subscriptions.add(db.createQuery(TodoItem.TABLE, LIST_QUERY, listId)
                .mapToList(TodoItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter));
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }

}
