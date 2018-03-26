package it.communikein.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.ui.RecipeDetailActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link RecipeIngredientsWidgetConfigureActivity RecipeIngredientsWidgetConfigureActivity}
 */
public class RecipeIngredientsWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Recipe recipe = RecipeIngredientsWidgetConfigureActivity.loadRecipePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_ingredients_widget);

        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.KEY_RECIPE, recipe);
        // In widget we are not allowing to use intents as usually. We have to use PendingIntent instead of 'startActivity'
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Here the basic operations the remote view can do.
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        if (recipe != null) {
            views.setTextViewText(R.id.widget_name, recipe.getName());

            Intent listIntent = new Intent(context, ListWidgetService.class);
            listIntent.putExtra(ListWidgetService.KEY_INGREDIENTS, appWidgetId);
            views.setRemoteAdapter(R.id.widget_list, listIntent);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetUpdateService.startUpdateListWidgets(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            RecipeIngredientsWidgetConfigureActivity.deleteRecipePref(context, appWidgetId);
        }
    }
}

