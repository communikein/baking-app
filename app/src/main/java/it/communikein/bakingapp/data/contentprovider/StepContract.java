package it.communikein.bakingapp.data.contentprovider;

import android.net.Uri;

import static it.communikein.bakingapp.data.contentprovider.BakingContentProvider.BASE_CONTENT_URI;

public class StepContract {

    public static final long INVALID_STEP_ID = -1;

    public static final class StepEntry {

        public static final String TABLE_NAME = "steps";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_STEP_NUM = "step_num";
        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_SHORT_DESCRIPTION = "short_description";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VIDEO_URL = "video_url";
        public static final String COLUMN_THUMBNAIL_URL = "thumbnail_url";

        private static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();

        public static Uri getStepsUri() {
            return CONTENT_URI;
        }

        public static Uri getStepUri(int id) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(COLUMN_ID, String.valueOf(id))
                    .build();
        }

        public static Uri getRecipeStepsUri(int recipeId) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(COLUMN_RECIPE_ID, String.valueOf(recipeId))
                    .build();
        }

    }

}
