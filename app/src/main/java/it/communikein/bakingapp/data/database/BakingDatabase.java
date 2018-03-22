package it.communikein.bakingapp.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import it.communikein.bakingapp.data.model.Ingredient;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;

@Database(entities = {Ingredient.class, Recipe.class, Step.class},
        version = 1, exportSchema = false)
public abstract class BakingDatabase extends RoomDatabase {

    public static final String NAME = "baking_data";

    public abstract IngredientsDao ingredientsDao();
    public abstract StepsDao stepsDao();
    public abstract RecipesDao recipesDao();
}
