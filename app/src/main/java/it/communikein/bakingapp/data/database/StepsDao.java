package it.communikein.bakingapp.data.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import it.communikein.bakingapp.data.model.Step;

@Dao
public interface StepsDao {

    @Insert
    long addStep(Step entry);

    @Insert
    void addSteps(List<Step> entries);

    @Query("DELETE FROM steps WHERE _id = :id")
    int deleteStep(int id);

    @Query("DELETE FROM steps WHERE recipe_id = :recipe_id")
    int deleteRecipeSteps(int recipe_id);

    @Query("SELECT * FROM steps WHERE recipe_id = :recipe_id")
    List<Step> getRecipeSteps(int recipe_id);

    @Query("SELECT * FROM steps WHERE recipe_id = :recipe_id")
    Cursor getCursorRecipeSteps(int recipe_id);

    @Query("SELECT * FROM steps WHERE _id = :id")
    Step getStep(int id);

    @Query("SELECT COUNT(_id) FROM steps WHERE recipe_id = :recipe_id")
    int getRecipeStepsCount(int recipe_id);

}
