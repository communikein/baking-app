package it.communikein.bakingapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Ingredient implements Parcelable {

    public static final String QUANTITY = "quantity";
    public static final String MEASURE = "measure";
    public static final String INGREDIENT = "ingredient";

    @SerializedName(QUANTITY)
    private float quantity;

    @SerializedName(MEASURE)
    private String measure;

    @SerializedName(INGREDIENT)
    private String ingredient;


    public Ingredient(Parcel in) {
        setQuantity(in.readFloat());
        setMeasure(in.readString());
        setIngredient(in.readString());
    }

    public Ingredient(float quantity, String measure, String ingredient) {
        setQuantity(quantity);
        setMeasure(measure);
        setIngredient(ingredient);
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
        dest.writeFloat(getQuantity());
        dest.writeString(getMeasure());
        dest.writeString(getIngredient());
    }

    public static final Parcelable.Creator<Ingredient> CREATOR = new Parcelable.Creator<Ingredient>() {

        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };
}
