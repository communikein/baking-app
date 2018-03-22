package it.communikein.bakingapp.data.contentprovider;

import android.net.Uri;

import static it.communikein.bakingapp.data.contentprovider.BakingContentProvider.BASE_CONTENT_URI;

public class RecipeContract {

    public static final long INVALID_RECIPE_ID = -1;

    public static final class RecipeEntry {
        public static final String TABLE_NAME = "recipes";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SERVINGS = "servings";
        public static final String COLUMN_IMAGE = "image";

        private static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();

        public static Uri getRecipesUri() {
            return CONTENT_URI;
        }

        public static Uri getRecipe(int id) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(COLUMN_ID, String.valueOf(id))
                    .build();
        }
    }

}
