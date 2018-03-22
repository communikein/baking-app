package it.communikein.bakingapp.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import it.communikein.bakingapp.data.model.Recipe;

@Dao
public interface RecipesDao {

    @Insert
    long addRecipe(Recipe entry);

    @Insert
    void addRecipes(List<Recipe> entries);

    @Query("DELETE FROM recipes WHERE id = :id")
    int deleteRecipe(int id);

    @Query("SELECT * FROM recipes WHERE id = :id")
    LiveData<List<Recipe>> getObservableRecipes(int id);

    @Query("SELECT * FROM recipes")
    List<Recipe> getRecipes();

    @Query("SELECT * FROM recipes")
    Cursor getCursorRecipes();

    @Query("SELECT * FROM recipes WHERE id = :id")
    LiveData<Recipe> getObservableRecipe(int id);

    @Query("SELECT * FROM recipes WHERE id = :id")
    Recipe getRecipe(int id);

    @Query("SELECT * FROM recipes WHERE id = :id")
    Cursor getCursorRecipe(int id);

    @Query("SELECT COUNT(id) FROM recipes")
    int getRecipesCount();

}
