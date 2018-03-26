package it.communikein.bakingapp.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import it.communikein.bakingapp.data.contentprovider.RecipeContract;

import static it.communikein.bakingapp.data.contentprovider.RecipeContract.RecipeEntry.COLUMN_ID;
import static it.communikein.bakingapp.data.contentprovider.RecipeContract.RecipeEntry.COLUMN_IMAGE;
import static it.communikein.bakingapp.data.contentprovider.RecipeContract.RecipeEntry.COLUMN_NAME;
import static it.communikein.bakingapp.data.contentprovider.RecipeContract.RecipeEntry.COLUMN_SERVINGS;
import static it.communikein.bakingapp.data.contentprovider.RecipeContract.RecipeEntry.TABLE_NAME;

@Entity(tableName = TABLE_NAME)
public class Recipe implements Parcelable {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String INGREDIENTS = "ingredients";
    public static final String STEPS = "steps";
    public static final String SERVINGS = "servings";
    public static final String IMAGE = "image";
    public static final String FAVOURITE = "favourite";

    @SerializedName(ID)
    @PrimaryKey @ColumnInfo(index = true, name = COLUMN_ID)
    private int id;

    @SerializedName(NAME)
    @ColumnInfo(index = true, name = COLUMN_NAME)
    private String name;

    @SerializedName(INGREDIENTS)
    @Ignore
    private List<Ingredient> ingredients;

    @SerializedName(STEPS)
    @Ignore
    private List<Step> steps;

    @SerializedName(SERVINGS)
    @ColumnInfo(name = COLUMN_SERVINGS)
    private int servings;

    @SerializedName(IMAGE)
    @ColumnInfo(name = COLUMN_IMAGE)
    private String image;

    @SerializedName(FAVOURITE)
    @Ignore
    private boolean favourite;


    public static Recipe fromParcel(Parcel origin) {
        final Recipe recipe = new Recipe();

        recipe.setId(origin.readInt());
        recipe.setName(origin.readString());

        recipe.setIngredients(new ArrayList<>());
        if (origin.readInt() == 1)
            origin.readTypedList(recipe.ingredients, Ingredient.CREATOR);

        recipe.setSteps(new ArrayList<>());
        if (origin.readInt() == 1)
            origin.readTypedList(recipe.steps, Step.CREATOR);

        recipe.setServings(origin.readInt());
        recipe.setImage(origin.readString());
        recipe.setFavourite(origin.readInt() == 1);

        return recipe;
    }

    public static Recipe fromContentValues(ContentValues origin) {
        if (origin == null) return null;

        final Recipe recipe = new Recipe();

        if (origin.containsKey(COLUMN_ID))
            recipe.setId(origin.getAsInteger(COLUMN_ID));
        if (origin.containsKey(COLUMN_NAME))
            recipe.setName(origin.getAsString(COLUMN_NAME));
        if (origin.containsKey(COLUMN_SERVINGS))
            recipe.setServings(origin.getAsInteger(COLUMN_SERVINGS));
        if (origin.containsKey(COLUMN_IMAGE))
            recipe.setImage(origin.getAsString(COLUMN_IMAGE));

        return recipe;
    }

    public static Recipe fromJsonString(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<Recipe>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static Recipe fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_ID);
        int nameIndex = cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_NAME);
        int servingsIndex = cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SERVINGS);
        int imageIndex = cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_IMAGE);

        int recipeId = cursor.getInt(idIndex);
        String recipeName = cursor.getString(nameIndex);
        int recipeServings = cursor.getInt(servingsIndex);
        String recipeImage = cursor.getString(imageIndex);

        return new Recipe(recipeId, recipeName, recipeServings, recipeImage);
    }


    private Recipe() { }

    public Recipe(int id, String name, int servings, String image) {
        setId(id);
        setName(name);
        setIngredients(new ArrayList<>());
        setSteps(new ArrayList<>());
        setServings(servings);
        setImage(image);
    }

    public Recipe(int id, String name, List<Ingredient> ingredients, List<Step> steps,
                  int servings, String image) {
        setId(id);
        setName(name);
        setIngredients(ingredients);
        setSteps(steps);
        setServings(servings);
        setImage(image);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }




    public String toJsonString() {
        Gson gson = new Gson();
        Type type = new TypeToken<Recipe>() {}.getType();
        return gson.toJson(this, type);
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(RecipeContract.RecipeEntry.COLUMN_ID, getId());
        contentValues.put(RecipeContract.RecipeEntry.COLUMN_NAME, getName());
        contentValues.put(RecipeContract.RecipeEntry.COLUMN_SERVINGS, getServings());
        contentValues.put(RecipeContract.RecipeEntry.COLUMN_IMAGE, getImage());

        return contentValues;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Recipe)) return false;

        Recipe other = (Recipe) obj;
        return this.getId() == other.getId();
    }

    public boolean displayEquals(Object obj) {
        if (! (obj instanceof Recipe)) return false;

        Recipe other = (Recipe) obj;
        return this.getName().equals(other.getName());
    }

    public String printIngredients() {
        if (getIngredients() == null) return null;

        StringBuilder result = new StringBuilder();
        for (Ingredient ingredient : getIngredients())
            result.append(ingredient.getIngredient()).append(", ");
        result.replace(result.length() - 3, result.length() -1, "");

        return result.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getName());

        boolean areThereIngredients = getIngredients() != null && getIngredients().size() > 0;
        dest.writeInt(areThereIngredients ? 1 : 0);
        if (areThereIngredients)
            dest.writeTypedList(getIngredients());

        boolean areThereSteps = getSteps() != null && getSteps().size() > 0;
        dest.writeInt(areThereSteps ? 1 : 0);
        if (areThereSteps)
            dest.writeTypedList(getSteps());

        dest.writeInt(getServings());
        dest.writeString(getImage());
        dest.writeInt(isFavourite() ? 1 : 0);
    }

    public static final Parcelable.Creator<Recipe> CREATOR = new Parcelable.Creator<Recipe>() {

        public Recipe createFromParcel(Parcel in) {
            return Recipe.fromParcel(in);
        }

        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public ContentValues writeToContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ID, getId());
        contentValues.put(COLUMN_NAME, getName());
        contentValues.put(COLUMN_SERVINGS, getServings());
        contentValues.put(COLUMN_IMAGE, getImage());

        return contentValues;
    }
}
