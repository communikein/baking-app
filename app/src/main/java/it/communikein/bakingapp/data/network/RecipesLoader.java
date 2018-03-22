package it.communikein.bakingapp.data.network;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import it.communikein.bakingapp.data.database.RecipesDao;
import it.communikein.bakingapp.data.model.Ingredient;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;

public class RecipesLoader extends AsyncTaskLoader<List<Recipe>> {

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;
    private final RecipesDao mRecipesDao;

    private RecipesLoader(Activity activity, RecipesDao recipesDao) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mRecipesDao = recipesDao;
    }

    public static RecipesLoader createRecipeLoader(Activity activity, RecipesDao recipesDao) {
        return new RecipesLoader(activity, recipesDao);
    }


    @Override
    public List<Recipe> loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        try {
            URL url = NetworkUtils.getRecipesUrl();
            if (url == null)
                return null;

            Bundle response = NetworkUtils.getResponseFromHttpUrl(url);
            if (response.containsKey(NetworkUtils.KEY_DATA)) {
                Type type = new TypeToken<List<Recipe>>(){}.getType();
                List<Recipe> recipes = new Gson()
                        .fromJson(response.getString(NetworkUtils.KEY_DATA), type);

                for (Recipe recipe : recipes) {
                    for (Ingredient ingredient : recipe.getIngredients())
                        ingredient.setRecipeId(recipe.getId());

                    for (Step step : recipe.getSteps())
                        step.setRecipeId(recipe.getId());

                    boolean favourite = mRecipesDao.getRecipe(recipe.getId()) != null;
                    recipe.setFavourite(favourite);
                }

                return recipes;
            }
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

}