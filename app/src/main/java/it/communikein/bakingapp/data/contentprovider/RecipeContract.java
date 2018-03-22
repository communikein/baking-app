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

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();
    }

}
