package dhbk.android.testsqlbrite;

import android.app.Application;
import android.content.Context;

/**
 * Created by huynhducthanhphong on 8/31/16.
 */
public class TodoApp extends Application {
    private TodoComponent mainComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mainComponent = DaggerTodoComponent.builder().todoModule(new TodoModule(this)).build();
    }

    public static TodoComponent getComponent(Context context) {
        return ((TodoApp) context.getApplicationContext()).mainComponent;
    }
}
