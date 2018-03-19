package it.communikein.bakingapp.network;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import it.communikein.bakingapp.model.Recipe;

public class RecipesLoader extends AsyncTaskLoader<List<Recipe>> {

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;

    private RecipesLoader(Activity activity) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
    }

    public static RecipesLoader createRecipeLoader(Activity activity) {
        return new RecipesLoader(activity);
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
                return new Gson().fromJson(response.getString(NetworkUtils.KEY_DATA), type);
            }
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

}