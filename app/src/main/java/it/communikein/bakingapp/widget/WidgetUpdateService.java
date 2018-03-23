package it.communikein.bakingapp.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import it.communikein.bakingapp.R;

public class WidgetUpdateService extends IntentService {

    public static final String ACTION_UPDATE_LIST_VIEW = "it.communikein.bakingapp.widgetupdateservice.update_app_widget_list";

    public WidgetUpdateService() {
        super(WidgetUpdateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_UPDATE_LIST_VIEW.equals(action)){
                handleActionUpdateListView();
            }
        }
    }

    private void handleActionUpdateListView() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, RecipeIngredientsWidget.class));

        RecipeIngredientsWidget.updateAllAppWidget(this, appWidgetManager, appWidgetIds);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }

    public static void startUpdateListWidgets(Context context) {
        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_UPDATE_LIST_VIEW);
        context.startService(intent);
    }

}
