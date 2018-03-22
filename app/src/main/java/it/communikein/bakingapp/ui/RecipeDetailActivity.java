package it.communikein.bakingapp.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import it.communikein.bakingapp.AppExecutors;
import it.communikein.bakingapp.R;
import it.communikein.bakingapp.StepsListAdapter;
import it.communikein.bakingapp.Utils;
import it.communikein.bakingapp.data.database.BakingDatabase;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.ActivityRecipeDetailBinding;

public class RecipeDetailActivity extends AppCompatActivity implements
        StepsListAdapter.StepClickCallback{

    public static final String KEY_RECIPE = "recipe";

    ActivityRecipeDetailBinding mBinding;

    private Recipe mRecipe;

    @Inject
    public AppExecutors mExecutors;

    @Inject
    public BakingDatabase mDatabase;


    public interface FavouriteRecipeUpdateListener {
        void onFavouriteRecipeUpdated(Recipe recipe);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_detail);

        parseData();
        initUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_RECIPE, mRecipe);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(KEY_RECIPE))
            mRecipe = savedInstanceState.getParcelable(KEY_RECIPE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);

        return super.onOptionsItemSelected(item);
    }

    private void parseData() {
        Intent startIntent = getIntent();
        if (startIntent == null) {
            finish();
            return;
        }

        mRecipe = startIntent.getParcelableExtra(KEY_RECIPE);
    }

    private void initUI() {
        if (mRecipe == null) return;

        updateUI();

        mBinding.nameTextview.setText(mRecipe.getName());
        mBinding.servingsTextview.setText(String.valueOf(mRecipe.getServings()));
        mBinding.ingredientsTextview.setText(mRecipe.printIngredients());

        if (!TextUtils.isEmpty(mRecipe.getImage()))
            Picasso.get()
                    .load(mRecipe.getImage())
                    .error(Utils.getDrawableColored(R.drawable.ic_broken_image,
                            R.color.black, this))
                    .placeholder(Utils.getDrawableColored(R.drawable.ic_image,
                            R.color.black, this))
                    .into(mBinding.recipeImageview);

        initToolbar();
        initFab();
        initStepsList();
    }

    private void updateUI() {
        if (TextUtils.isEmpty(mRecipe.getImage())) {
            mBinding.recipeImageContainer.setVisibility(View.GONE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mBinding.constraintView);

            constraintSet.connect(R.id.recipe_info_container, ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(R.id.recipe_info_container, ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(R.id.recipe_info_container, ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(R.id.label_steps, ConstraintSet.TOP,
                    R.id.recipe_info_container, ConstraintSet.BOTTOM);

            constraintSet.applyTo(mBinding.constraintView);

            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams)
                    mBinding.recipeInfoContainer.getLayoutParams();
            p.setMargins(0, 0, 0, 0);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                p.setMarginStart(0);
                p.setMarginEnd(0);
            }
            mBinding.recipeInfoContainer.requestLayout();
        }
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(mRecipe.getName());
    }

    private void initFab() {
        updateFab(mRecipe);

        mBinding.favoriteFab.setOnClickListener(v -> {
            updateFavourite(mRecipe, recipe -> mExecutors.mainThread().execute(() -> {
                updateFab(recipe);

                if (recipe.isFavourite())
                    Snackbar.make(mBinding.coordinatorView, R.string.label_recipe_added_to_favourites,
                            Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(mBinding.coordinatorView, R.string.label_recipe_removed_from_favourites,
                            Snackbar.LENGTH_LONG).show();
            }));
        });
    }

    private void updateFab(Recipe recipe) {
        if (recipe.isFavourite())
            mBinding.favoriteFab.setImageDrawable(Utils.getDrawableColored(
                    R.drawable.ic_star_border,
                    R.color.black,
                    this));
        else
            mBinding.favoriteFab.setImageDrawable(Utils.getDrawableColored(
                    R.drawable.ic_star,
                    R.color.black,
                    this));
    }

    private void initStepsList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        mBinding.stepsList.setLayoutManager(layoutManager);

        StepsListAdapter stepsListAdapter = new StepsListAdapter(this);
        mBinding.stepsList.setAdapter(stepsListAdapter);

        if (mRecipe.getSteps() != null) {
            stepsListAdapter.setList(mRecipe.getSteps());
            stepsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStepClick(Step step) {
        Intent intent = new Intent(this, StepDetailActivity.class);
        intent.putExtra(StepDetailActivity.KEY_RECIPE, mRecipe);
        intent.putExtra(StepDetailActivity.KEY_STEP_SELECTED, step.getStepNum());
        startActivity(intent);
    }

    private void updateFavourite(Recipe recipe, FavouriteRecipeUpdateListener listener) {
        mExecutors.diskIO().execute(() -> {
            if (recipe.isFavourite())
                mDatabase.recipesDao().deleteRecipe(recipe.getId());
            else {
                mDatabase.recipesDao().addRecipe(recipe);
                mDatabase.ingredientsDao().addIngredients(recipe.getIngredients());
                mDatabase.stepsDao().addSteps(recipe.getSteps());
            }

            recipe.setFavourite(!recipe.isFavourite());
            listener.onFavouriteRecipeUpdated(recipe);
        });
    }
}
