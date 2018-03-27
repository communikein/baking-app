package it.communikein.bakingapp.data.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import it.communikein.bakingapp.data.model.Ingredient;

@Dao
public interface IngredientsDao {

    @Insert
    long addIngredient(Ingredient entry);

    @Insert
    void addIngredients(List<Ingredient> entries);

    @Query("DELETE FROM ingredients WHERE id = :id")
    int deleteIngredient(int id);

    @Query("DELETE FROM ingredients WHERE recipe_id = :recipe_id")
    int deleteRecipeIngredients(int recipe_id);

    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipe_id")
    List<Ingredient> getRecipeIngredients(int recipe_id);

    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipe_id")
    Cursor getCursorRecipeIngredients(int recipe_id);

    @Query("SELECT * FROM ingredients WHERE id = :id")
    Ingredient getIngredient(int id);

    @Query("SELECT COUNT(id) FROM ingredients WHERE recipe_id = :recipe_id")
    int getRecipeIngredientsCount(int recipe_id);

}
