package dhbk.android.testsqlbrite;

import android.app.Application;
import android.content.Context;

/**
 * Created by huynhducthanhphong on 8/31/16.
 */

/**
 * make application component
 */
public class TodoApp extends Application {
    private TodoComponent mainComponent;

    // declare component
    @Override
    public void onCreate() {
        super.onCreate();

        mainComponent = DaggerTodoComponent.builder().todoModule(new TodoModule(this)).build();
    }

    // get the component
    public static TodoComponent getComponent(Context context) {
        return ((TodoApp) context.getApplicationContext()).mainComponent;
    }
}
