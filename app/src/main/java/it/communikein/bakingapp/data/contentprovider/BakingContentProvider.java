package it.communikein.bakingapp.data.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasContentProviderInjector;
import it.communikein.bakingapp.data.database.BakingDatabase;
import it.communikein.bakingapp.data.model.Ingredient;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;


public class BakingContentProvider extends ContentProvider implements
        HasContentProviderInjector {

    public static final String AUTHORITY = "it.communikein.bakingapp.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static final int CODE_RECIPES_DIR = 1000;
    private static final int CODE_RECIPE_ITEM = 1001;

    private static final int CODE_INGREDIENTS_RECIPE = 2000;
    private static final int CODE_INGREDIENT_ITEM = 2001;

    private static final int CODE_STEPS_RECIPE = 3000;
    private static final int CODE_STEP_ITEM = 3001;

    /** The URI matcher. */
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    @Inject
    DispatchingAndroidInjector<ContentProvider> dispatchingAndroidInjector;

    @Inject
    Lazy<BakingDatabase> bakingDatabase;

    static {
        MATCHER.addURI(AUTHORITY, RecipeContract.RecipeEntry.TABLE_NAME, CODE_RECIPES_DIR);
        MATCHER.addURI(AUTHORITY, RecipeContract.RecipeEntry.TABLE_NAME + "/#", CODE_RECIPE_ITEM);

        MATCHER.addURI(AUTHORITY, IngredientContract.IngredientEntry.TABLE_NAME, CODE_INGREDIENTS_RECIPE);
        MATCHER.addURI(AUTHORITY, IngredientContract.IngredientEntry.TABLE_NAME + "/#", CODE_INGREDIENT_ITEM);

        MATCHER.addURI(AUTHORITY, StepContract.StepEntry.TABLE_NAME, CODE_STEPS_RECIPE);
        MATCHER.addURI(AUTHORITY, StepContract.StepEntry.TABLE_NAME + "/#", CODE_STEP_ITEM);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final Context context = getContext();
        if (context == null) return null;

        if (bakingDatabase == null) AndroidInjection.inject(this);

        Cursor cursor;
        String id;
        final int code = MATCHER.match(uri);
        switch (code) {
            case CODE_RECIPES_DIR:
                cursor = bakingDatabase.get().recipesDao().getCursorRecipes();
                cursor.setNotificationUri(context.getContentResolver(), uri);
                break;

            case CODE_INGREDIENTS_RECIPE:
                id = uri.getQueryParameter(IngredientContract.IngredientEntry.COLUMN_RECIPE_ID);
                cursor = bakingDatabase.get().ingredientsDao()
                        .getCursorRecipeIngredients(Integer.valueOf(id));
                cursor.setNotificationUri(context.getContentResolver(), uri);
                break;

            case CODE_STEPS_RECIPE:
                id = uri.getQueryParameter(StepContract.StepEntry.COLUMN_RECIPE_ID);
                cursor = bakingDatabase.get().stepsDao()
                        .getCursorRecipeSteps(Integer.valueOf(id));
                cursor.setNotificationUri(context.getContentResolver(), uri);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final Context context = getContext();
        if (context == null) {
            return null;
        }

        long id;
        switch (MATCHER.match(uri)) {
            case CODE_RECIPE_ITEM:
                id = bakingDatabase.get().recipesDao()
                        .addRecipe(Recipe.fromContentValues(values));
                context.getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            case CODE_INGREDIENT_ITEM:
                id = bakingDatabase.get().ingredientsDao()
                        .addIngredient(Ingredient.fromContentValues(values));
                context.getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            case CODE_STEP_ITEM:
                id = bakingDatabase.get().stepsDao()
                        .addStep(Step.fromContentValues(values));
                context.getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] valuesArray) {
        final Context context = getContext();
        if (context == null) {
            return 0;
        }

        switch (MATCHER.match(uri)) {
            case CODE_INGREDIENTS_RECIPE:
                final ArrayList<Ingredient> ingredients = new ArrayList<>();
                for (ContentValues ingredient : valuesArray)
                    ingredients.add(Ingredient.fromContentValues(ingredient));

                bakingDatabase.get().ingredientsDao().addIngredients(ingredients);
                return valuesArray.length;
            case CODE_STEPS_RECIPE:
                final ArrayList<Step> steps = new ArrayList<>();
                for (ContentValues step : valuesArray)
                    steps.add(Step.fromContentValues(step));

                bakingDatabase.get().stepsDao().addSteps(steps);
                return valuesArray.length;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final Context context = getContext();
        if (context == null) return 0;

        int count;
        String id;
        switch (MATCHER.match(uri)) {
            case CODE_RECIPE_ITEM:
                count = bakingDatabase.get().recipesDao()
                        .deleteRecipe((int) ContentUris.parseId(uri));
                context.getContentResolver().notifyChange(uri, null);
                return count;
            case CODE_INGREDIENTS_RECIPE:
                id = uri.getQueryParameter(IngredientContract.IngredientEntry.COLUMN_RECIPE_ID);
                count = bakingDatabase.get().ingredientsDao()
                        .deleteRecipeIngredients(Integer.parseInt(id));
                context.getContentResolver().notifyChange(uri, null);
                return count;
            case CODE_STEPS_RECIPE:
                id = uri.getQueryParameter(StepContract.StepEntry.COLUMN_RECIPE_ID);
                count = bakingDatabase.get().stepsDao()
                        .deleteRecipeSteps(Integer.parseInt(id));
                context.getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }





    @Override
    public AndroidInjector<ContentProvider> contentProviderInjector() {
        return dispatchingAndroidInjector;
    }
}
