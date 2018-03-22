package it.communikein.bakingapp.data.contentprovider;

import android.net.Uri;

import static it.communikein.bakingapp.data.contentprovider.BakingContentProvider.BASE_CONTENT_URI;

public class IngredientContract {

    public static final long INVALID_INGREDIENT_ID = -1;

    public static final class IngredientEntry {

        public static final String TABLE_NAME = "ingredients";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_MEASURE = "measure";
        public static final String COLUMN_INGREDIENT = "ingredient";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();

    }

}
