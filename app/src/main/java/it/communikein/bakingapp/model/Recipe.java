package it.communikein.bakingapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Recipe implements Parcelable {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String INGREDIENTS = "ingredients";
    public static final String STEPS = "steps";
    public static final String SERVINGS = "servings";
    public static final String IMAGE = "image";

    @SerializedName(ID)
    private int id;

    @SerializedName(NAME)
    private String name;

    @SerializedName(INGREDIENTS)
    private List<Ingredient> ingredients;

    @SerializedName(STEPS)
    private List<Step> steps;

    @SerializedName(SERVINGS)
    private int servings;

    @SerializedName(IMAGE)
    private String image;


    public Recipe(Parcel in) {
        setId(in.readInt());
        setName(in.readString());

        if (in.readInt() == 1) {
            setIngredients(new ArrayList<Ingredient>());
            in.readTypedList(this.ingredients, Ingredient.CREATOR);
        }
        else
            setIngredients(null);

        if (in.readInt() == 1) {
            setSteps(new ArrayList<Step>());
            in.readTypedList(this.steps, Step.CREATOR);
        }
        else
            setSteps(null);

        setServings(in.readInt());
        setImage(in.readString());
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
    }

    public static final Parcelable.Creator<Recipe> CREATOR = new Parcelable.Creator<Recipe>() {

        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };
}
