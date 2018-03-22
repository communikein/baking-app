package it.communikein.bakingapp.widget;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;

import java.util.ArrayList;

import it.communikein.bakingapp.data.contentprovider.IngredientContract;
import it.communikein.bakingapp.data.contentprovider.RecipeContract;
import it.communikein.bakingapp.data.model.Ingredient;
import it.communikein.bakingapp.data.model.Recipe;

public class RecipesLoader extends AsyncTaskLoader<ArrayList<Recipe>> {

    private ContentResolver mContentResolver;

    public RecipesLoader(@NonNull Context context) {
        super(context);

        this.mContentResolver = context.getContentResolver();
    }

    @Override
    public ArrayList<Recipe> loadInBackground() {
        Cursor cursor = mContentResolver.query(
                RecipeContract.RecipeEntry.getRecipesUri(),
                null,
                null,
                null,
                null);

        return parseCursor(cursor);
    }

    private ArrayList<Recipe> parseCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;

        ArrayList<Recipe> recipes = new ArrayList<>();
        while(cursor.moveToNext()){
            Recipe recipe = Recipe.fromCursor(cursor);

            Cursor ingredientsCursor = mContentResolver.query(
                    IngredientContract.IngredientEntry.getRecipeIngredientsUri(recipe.getId()),
                    null,
                    null,
                    null,
                    null);

            if (ingredientsCursor == null || ingredientsCursor.getCount() == 0) continue;
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            while (ingredientsCursor.moveToNext()) {
                Ingredient ingredient = Ingredient.fromCursor(ingredientsCursor);
                ingredients.add(ingredient);
            }

            recipe.setIngredients(ingredients);
            recipes.add(recipe);
        }

        return recipes;
    }
}
