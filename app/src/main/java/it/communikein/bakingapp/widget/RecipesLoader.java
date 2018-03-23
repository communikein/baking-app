package it.communikein.bakingapp.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import it.communikein.bakingapp.data.contentprovider.IngredientContract;
import it.communikein.bakingapp.data.contentprovider.RecipeContract;
import it.communikein.bakingapp.data.contentprovider.StepContract;
import it.communikein.bakingapp.data.model.Ingredient;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;

public class RecipesLoader extends AsyncTaskLoader<ArrayList<Recipe>> {

    private ContentResolver mContentResolver;

    RecipesLoader(@NonNull Context context) {
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
            ArrayList<Ingredient> ingredients = getIngredientsFromCursor(ingredientsCursor);
            recipe.setIngredients(ingredients);
            if (ingredientsCursor != null) ingredientsCursor.close();

            Cursor stepsCursor = mContentResolver.query(
                    StepContract.StepEntry.getRecipeStepsUri(recipe.getId()),
                    null,
                    null,
                    null,
                    null);
            ArrayList<Step> steps = getStepsFromCursor(stepsCursor);
            recipe.setSteps(steps);
            if (stepsCursor != null) stepsCursor.close();

            recipes.add(recipe);
        }

        return recipes;
    }

    private ArrayList<Ingredient> getIngredientsFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        while (cursor.moveToNext()) {
            Ingredient ingredient = Ingredient.fromCursor(cursor);
            ingredients.add(ingredient);
        }

        return ingredients;
    }

    private ArrayList<Step> getStepsFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;
        ArrayList<Step> steps = new ArrayList<>();
        while (cursor.moveToNext()) {
            Step step = Step.fromCursor(cursor);
            steps.add(step);
        }

        return steps;
    }
}
