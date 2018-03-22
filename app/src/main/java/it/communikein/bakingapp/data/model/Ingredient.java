package it.communikein.bakingapp.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import it.communikein.bakingapp.data.contentprovider.IngredientContract;
import it.communikein.bakingapp.data.contentprovider.RecipeContract;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static it.communikein.bakingapp.data.contentprovider.IngredientContract.IngredientEntry.COLUMN_ID;
import static it.communikein.bakingapp.data.contentprovider.IngredientContract.IngredientEntry.COLUMN_INGREDIENT;
import static it.communikein.bakingapp.data.contentprovider.IngredientContract.IngredientEntry.COLUMN_MEASURE;
import static it.communikein.bakingapp.data.contentprovider.IngredientContract.IngredientEntry.COLUMN_QUANTITY;
import static it.communikein.bakingapp.data.contentprovider.IngredientContract.IngredientEntry.COLUMN_RECIPE_ID;
import static it.communikein.bakingapp.data.contentprovider.IngredientContract.IngredientEntry.TABLE_NAME;
import static it.communikein.bakingapp.data.model.Ingredient.ID;

@Entity(tableName = TABLE_NAME,
        foreignKeys = @ForeignKey(entity = Recipe.class,
                parentColumns = ID,
                childColumns = COLUMN_RECIPE_ID,
                onDelete = CASCADE))
public class Ingredient implements Parcelable {

    public static final String ID = "id";
    public static final String RECIPE_ID = "recipe_id";
    public static final String QUANTITY = "quantity";
    public static final String MEASURE = "measure";
    public static final String INGREDIENT = "ingredient";

    @SerializedName(ID)
    @PrimaryKey(autoGenerate = true) @ColumnInfo(index = true, name = COLUMN_ID)
    public int id;

    @SerializedName(RECIPE_ID)
    @ColumnInfo(index = true, name = RECIPE_ID)
    private int recipeId;

    @SerializedName(QUANTITY)
    @ColumnInfo(name = COLUMN_QUANTITY)
    private float quantity;

    @SerializedName(MEASURE)
    @ColumnInfo(name = COLUMN_MEASURE)
    private String measure;

    @SerializedName(INGREDIENT)
    @ColumnInfo(name = COLUMN_INGREDIENT)
    private String ingredient;


    public static Ingredient fromParcel(Parcel origin) {
        final Ingredient ingredient = new Ingredient();

        ingredient.setId(origin.readInt());
        ingredient.setRecipeId(origin.readInt());
        ingredient.setQuantity(origin.readFloat());
        ingredient.setMeasure(origin.readString());
        ingredient.setIngredient(origin.readString());

        return ingredient;
    }

    public static Ingredient fromContentValues(ContentValues origin) {
        if (origin == null) return null;

        final Ingredient ingredient = new Ingredient();

        if (origin.containsKey(COLUMN_ID))
            ingredient.setId(origin.getAsInteger(COLUMN_ID));
        if (origin.containsKey(COLUMN_RECIPE_ID))
            ingredient.setRecipeId(origin.getAsInteger(COLUMN_RECIPE_ID));
        if (origin.containsKey(COLUMN_QUANTITY))
            ingredient.setQuantity(origin.getAsFloat(COLUMN_QUANTITY));
        if (origin.containsKey(COLUMN_MEASURE))
            ingredient.setMeasure(origin.getAsString(COLUMN_MEASURE));
        if (origin.containsKey(COLUMN_INGREDIENT))
            ingredient.setIngredient(origin.getAsString(COLUMN_INGREDIENT));

        return ingredient;
    }

    public static Ingredient fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_ID);
        int recipeIdIndex = cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_RECIPE_ID);
        int quantityIndex = cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_QUANTITY);
        int measureIndex = cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_MEASURE);
        int ingredientIndex = cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_INGREDIENT);

        int ingredientId = cursor.getInt(idIndex);
        int ingredientRecipeId = cursor.getInt(recipeIdIndex);
        float ingredientQuantity = cursor.getFloat(quantityIndex);
        String ingredientMeasure = cursor.getString(measureIndex);
        String ingredient = cursor.getString(ingredientIndex);

        return new Ingredient(ingredientId, ingredientRecipeId, ingredientQuantity, ingredientMeasure, ingredient);
    }


    private Ingredient() {}

    public Ingredient(int id, int recipeId, float quantity, String measure, String ingredient) {
        setId(id);
        setRecipeId(recipeId);
        setQuantity(quantity);
        setMeasure(measure);
        setIngredient(ingredient);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }



    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Ingredient)) return false;

        Ingredient other = (Ingredient) obj;
        return this.getQuantity() == other.getQuantity() &&
                this.getMeasure().equals(other.getMeasure()) &&
                this.getIngredient().equals(other.getIngredient());
    }

    public boolean displayEquals(Object obj) {
        return this.equals(obj);
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeInt(getRecipeId());
        dest.writeFloat(getQuantity());
        dest.writeString(getMeasure());
        dest.writeString(getIngredient());
    }

    public static final Parcelable.Creator<Ingredient> CREATOR = new Parcelable.Creator<Ingredient>() {

        public Ingredient createFromParcel(Parcel in) {
            return Ingredient.fromParcel(in);
        }

        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    public ContentValues writeToContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ID, getId());
        contentValues.put(COLUMN_RECIPE_ID, getRecipeId());
        contentValues.put(COLUMN_QUANTITY, getQuantity());
        contentValues.put(COLUMN_MEASURE, getMeasure());
        contentValues.put(COLUMN_INGREDIENT, getIngredient());

        return contentValues;
    }
}
