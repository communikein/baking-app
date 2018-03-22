package it.communikein.bakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.databinding.RecipeIngredientsWidgetConfigureBinding;

/**
 * The configuration screen for the {@link RecipeIngredientsWidget RecipeIngredientsWidget} AppWidget.
 */
public class RecipeIngredientsWidgetConfigureActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<Recipe>> {

    private static final int LOADER_RECIPES = 123;

    private static final String PREFS_NAME = "it.communikein.bakingapp.widget.RecipeIngredientsWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    RecipeIngredientsWidgetConfigureBinding mBinding;

    // Write the prefix to the SharedPreferences object for this widget
    static void saveRecipePref(Context context, int appWidgetId, Recipe recipe) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, recipe.toJsonString());
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static Recipe loadRecipePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String recipeString = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (recipeString != null) {
            return Recipe.fromJsonString(recipeString);
        } else {
            return null;
        }
    }

    static void deleteRecipePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        mBinding = DataBindingUtil.setContentView(this, R.layout.recipe_ingredients_widget_configure);
        mBinding.setButton.setOnClickListener(v -> setRecipe());

        getSupportLoaderManager()
                .restartLoader(LOADER_RECIPES, null, this)
                .forceLoad();

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }

    private void setRecipe() {
        final Context context = RecipeIngredientsWidgetConfigureActivity.this;

        // When the button is clicked, store the string locally
        Recipe selected = (Recipe) mBinding.recipesSpinner.getSelectedItem();
        saveRecipePref(context, mAppWidgetId, selected);

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RecipeIngredientsWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @NonNull
    @Override
    public Loader<ArrayList<Recipe>> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_RECIPES:
                return new RecipesLoader(this);

            default:
                throw new RuntimeException("Loader " + String.valueOf(id) + ", not recognised.");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<Recipe>> loader, ArrayList<Recipe> data) {
        switch (loader.getId()) {
            case LOADER_RECIPES:
                ArrayAdapter<Recipe> recipesAdapter = new ArrayAdapter<>(this,
                        R.layout.list_item_ingredient, data.toArray(new Recipe[data.size()]));
                mBinding.recipesSpinner.setAdapter(recipesAdapter);

                break;

            default:
                throw new RuntimeException("Loader not found");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<Recipe>> loader) {

    }
}

