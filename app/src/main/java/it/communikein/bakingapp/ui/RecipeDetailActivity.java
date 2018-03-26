package it.communikein.bakingapp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import it.communikein.bakingapp.AppExecutors;
import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.database.BakingDatabase;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.ActivityRecipeDetailBinding;

public class RecipeDetailActivity extends AppCompatActivity implements
        RecipeDetailFragment.OnFavouriteClickListener, RecipeDetailFragment.OnStepClickListener, StepDetailFragment.OnChangeStepListener {

    public static final String KEY_RECIPE = StepDetailActivity.KEY_RECIPE;
    public static final String KEY_SELECTED_STEP = StepDetailActivity.KEY_SELECTED_STEP;

    ActivityRecipeDetailBinding mBinding;
    RecipeDetailFragment mRecipeDetailFragment;
    StepDetailFragment mStepDetailFragment;

    @Inject
    BakingDatabase bakingDatabase;

    @Inject
    AppExecutors mExecutors;

    private Recipe mRecipe;
    private Step mSelectedStep;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_detail);

        parseData(savedInstanceState);
        initUI();
        initToolbar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_RECIPE, mRecipe);
        outState.putParcelable(KEY_SELECTED_STEP, mSelectedStep);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(KEY_RECIPE))
            mRecipe = savedInstanceState.getParcelable(KEY_RECIPE);
        if (savedInstanceState.containsKey(KEY_SELECTED_STEP))
            mSelectedStep = savedInstanceState.getParcelable(KEY_SELECTED_STEP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void parseData(Bundle savedInstanceState) {
        Intent startIntent = getIntent();
        if (startIntent == null) {
            finish();
            return;
        }

        mRecipe = startIntent.getParcelableExtra(KEY_RECIPE);
        mSelectedStep = startIntent.getParcelableExtra(KEY_SELECTED_STEP);

        if (savedInstanceState != null)
            mSelectedStep = savedInstanceState.getParcelable(KEY_SELECTED_STEP);
    }

    private void initUI() {
        mRecipeDetailFragment = (RecipeDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.recipe_detail_fragment);
        mRecipeDetailFragment.updateRecipe(mRecipe);
        mRecipeDetailFragment.setFavouriteClickListener(this);
        mRecipeDetailFragment.setStepClickListener(this);

        mStepDetailFragment = (StepDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.step_detail_fragment);
        if (mBinding.panesDivider != null) {
            updateSecondPane();

            mStepDetailFragment.updateRecipe(mRecipe);
            mStepDetailFragment.updateSelectedStep(mSelectedStep);
            mStepDetailFragment.setStepChangedListener(this);
        }
    }

    private void updateSecondPane() {
        if (mSelectedStep == null) {
            mBinding.labelStepNotSelected.setVisibility(View.VISIBLE);
            findViewById(R.id.step_detail_fragment).setVisibility(View.GONE);
        }
        else {
            mBinding.labelStepNotSelected.setVisibility(View.GONE);
            findViewById(R.id.step_detail_fragment).setVisibility(View.VISIBLE);
        }
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mRecipe.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onFavouriteClicked(Recipe recipe) {
        mExecutors.diskIO().execute(() -> {
            boolean added;
            if (recipe.isFavourite()) {
                added = false;
                bakingDatabase.recipesDao().deleteRecipe(recipe.getId());
            }
            else {
                added = true;

                bakingDatabase.recipesDao().addRecipe(recipe);
                bakingDatabase.ingredientsDao().addIngredients(recipe.getIngredients());
                bakingDatabase.stepsDao().addSteps(recipe.getSteps());
            }
            recipe.setFavourite(!recipe.isFavourite());

            runOnUiThread(() -> mRecipeDetailFragment.updateFab(added));
            String message;
            if (added)
                message = getString(R.string.label_recipe_added_to_favourites);
            else
                message = getString(R.string.label_recipe_removed_from_favourites);

            Snackbar.make(mRecipeDetailFragment.getCoordinatorLayout(),
                    message, Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onStepClicked(Step step) {
        this.mSelectedStep = step;
        if (mBinding.panesDivider != null) {
            mBinding.labelStepNotSelected.setVisibility(View.GONE);
            findViewById(R.id.step_detail_fragment).setVisibility(View.VISIBLE);
            mStepDetailFragment.updateSelectedStep(mSelectedStep);
        }
        else {
            Intent intent = new Intent(this, StepDetailActivity.class);
            intent.putExtra(StepDetailActivity.KEY_RECIPE, mRecipe);
            intent.putExtra(StepDetailActivity.KEY_SELECTED_STEP, mSelectedStep);
            startActivity(intent);
        }
    }

    @Override
    public void onStepChanged(Step step) {
        this.mSelectedStep = step;
        int position = mRecipe.getSteps().indexOf(mSelectedStep);
        int maxPosition = mRecipe.getSteps().size() -1;

        if (position < maxPosition)
            mRecipeDetailFragment.mBinding.stepsList.scrollToPosition(maxPosition);
        mRecipeDetailFragment.mBinding.stepsList.scrollToPosition(position);
    }
}
