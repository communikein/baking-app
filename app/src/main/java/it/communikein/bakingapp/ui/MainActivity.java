package it.communikein.bakingapp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import it.communikein.bakingapp.AppExecutors;
import it.communikein.bakingapp.R;
import it.communikein.bakingapp.RecipesGridAdapter;
import it.communikein.bakingapp.data.database.BakingDatabase;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.ActivityMainBinding;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.network.NetworkUtils;
import it.communikein.bakingapp.data.network.RecipesLoader;

public class MainActivity extends AppCompatActivity implements
        RecipesGridAdapter.RecipeClickCallback, LoaderManager.LoaderCallbacks,
        SwipeRefreshLayout.OnRefreshListener,
        RecipeDetailFragment.OnFavouriteClickListener, RecipeDetailFragment.OnStepClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String KEY_DATASET = "dataset";
    private static final String KEY_FIRST_VISIBLE_ITEM_POS = "first_visible_item_pos";
    private static final String KEY_SHOW_GRID = "show_grid";
    private static final String KEY_SELECTED_RECIPE = "selected_recipe";

    private static final int LOADER_RECIPES_ID = 1001;

    private ActivityMainBinding mBinding;
    private RecipeDetailFragment mRecipeDetailFragment;

    private RecyclerView listRecyclerview;
    private FloatingActionButton changeLayoutFab;
    private SwipeRefreshLayout swipeRefresh;

    private List<Recipe> mData;
    private Recipe mSelectedRecipe;

    private int firstVisibleItemPosition = -1;
    private boolean mLandscape;
    private boolean mGridLayout;
    private boolean mIsTablet;
    private boolean mHasSecondPane;

    @Inject
    BakingDatabase bakingDatabase;

    @Inject
    AppExecutors mExecutors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        listRecyclerview = findViewById(R.id.list_recyclerview);
        changeLayoutFab = findViewById(R.id.change_layout_fab);

        /* Show data downloading */
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setRefreshing(false);

        mLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        mIsTablet = getResources().getBoolean(R.bool.isTablet);
        mHasSecondPane = findViewById(R.id.panes_divider) != null;

        parseData(savedInstanceState);
        initUI();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        parseData(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mData != null)
            outState.putParcelableArrayList(KEY_DATASET, new ArrayList<>(mData));
        if (mSelectedRecipe != null)
            outState.putParcelable(KEY_SELECTED_RECIPE, mSelectedRecipe);

        outState.putInt(KEY_FIRST_VISIBLE_ITEM_POS, firstVisibleItemPosition);
        outState.putBoolean(KEY_SHOW_GRID, mGridLayout);
    }

    private void showProgressBar() {
        swipeRefresh.setRefreshing(true);
    }

    private void hideProgressBar() {
        swipeRefresh.setRefreshing(false);
    }


    private void initUI() {
        initRecipesList();

        if (mLandscape || mIsTablet)
            changeLayoutFab.setVisibility(View.GONE);
        else
            initFab();

        if (mHasSecondPane) {
            mRecipeDetailFragment = (RecipeDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.recipe_detail_fragment);
            updateSecondPane();
        }
    }

    private void initFab() {
        changeLayoutFab.setVisibility(View.VISIBLE);

        int fabResource = R.drawable.ic_view_module;
        if (mGridLayout) fabResource = R.drawable.ic_view_list;
        changeLayoutFab.setImageResource(fabResource);
        changeLayoutFab.setOnClickListener(v -> {
            mGridLayout = !mGridLayout;

            GridLayoutManager layoutManager = (GridLayoutManager)
                    listRecyclerview.getLayoutManager();
            RecipesGridAdapter adapter = (RecipesGridAdapter)
                    listRecyclerview.getAdapter();

            updateFab();
            updateListLayout(layoutManager, adapter);
        });
    }

    private void updateFab() {
        if (mLandscape || mIsTablet)
            changeLayoutFab.setVisibility(View.GONE);
        else {
            changeLayoutFab.setVisibility(View.VISIBLE);

            if (showGridLayout())
                changeLayoutFab.setImageResource(R.drawable.ic_view_list);
            else
                changeLayoutFab.setImageResource(R.drawable.ic_view_module);
        }
    }

    private void updateListLayout(@NonNull GridLayoutManager layoutManager, @NonNull RecipesGridAdapter adapter) {
        adapter.setLayout(mGridLayout);

        int cols = 1;
        if (showGridLayout()) cols = numberOfColumns();
        layoutManager.setSpanCount(cols);

        if (listRecyclerview.getAdapter() == null) {
            listRecyclerview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        else {
            listRecyclerview.getRecycledViewPool().clear();
            listRecyclerview.swapAdapter(adapter, true);
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }

        listRecyclerview.scheduleLayoutAnimation();
    }

    private boolean showGridLayout() {
        return (mIsTablet && !mLandscape) || (!mIsTablet && mLandscape) || mGridLayout;
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // You can change this divider to adjust the size of the poster
        int widthDivider = getResources().getDimensionPixelSize(R.dimen.grid_item_recipe_width);
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2 ) return 2;	// to keep the grid aspect
        return nColumns;
    }

    private void initRecipesList() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1,
                GridLayoutManager.VERTICAL, false);
        listRecyclerview.setLayoutManager(layoutManager);

        RecipesGridAdapter adapter = new RecipesGridAdapter(showGridLayout(), this, this);
        adapter.setList(mData);

        updateListLayout(layoutManager, adapter);
        listRecyclerview.setHasFixedSize(true);
        listRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            }
        });
    }

    private void parseData(Bundle savedInstanceState) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_DATASET))
            onRefresh();

        if (savedInstanceState != null) {
            hideProgressBar();

            mGridLayout = savedInstanceState.getBoolean(KEY_SHOW_GRID, false);
            mSelectedRecipe = savedInstanceState.getParcelable(KEY_SELECTED_RECIPE);
            mData = savedInstanceState.getParcelableArrayList(KEY_DATASET);

            if (mData == null)
                onRefresh();
            else if (mData.size() > 0) {
                if (savedInstanceState.containsKey(KEY_FIRST_VISIBLE_ITEM_POS))
                    firstVisibleItemPosition = savedInstanceState.getInt(KEY_FIRST_VISIBLE_ITEM_POS);
                else
                    firstVisibleItemPosition = 0;
                listRecyclerview.smoothScrollToPosition(firstVisibleItemPosition);
            }

            RecipesGridAdapter adapter = (RecipesGridAdapter) listRecyclerview.getAdapter();
            if (adapter == null)
                adapter = new RecipesGridAdapter(showGridLayout(), this, this);

            adapter.setList(mData);
            adapter.setLayout(showGridLayout());
            adapter.notifyDataSetChanged();
        }
    }

    private void updateSecondPane() {
        if (mSelectedRecipe == null) {
            mBinding.labelRecipeNotSelected.setVisibility(View.VISIBLE);
            findViewById(R.id.recipe_detail_fragment).setVisibility(View.GONE);
        }
        else {
            mBinding.labelRecipeNotSelected.setVisibility(View.GONE);
            findViewById(R.id.recipe_detail_fragment).setVisibility(View.VISIBLE);

            mRecipeDetailFragment.updateRecipe(mSelectedRecipe);
        }
    }



    private void startLoader(int loader_id) {
        showProgressBar();

        if (loader_id == LOADER_RECIPES_ID || NetworkUtils.isDeviceOnline(this)) {
            getSupportLoaderManager()
                    .restartLoader(loader_id, null, MainActivity.this)
                    .forceLoad();
        }
        else {
            hideProgressBar();

            Snackbar.make(mBinding.coordinatorView, R.string.error_no_internet,
                    Snackbar.LENGTH_LONG).setAction(R.string.retry, v -> {
                getSupportLoaderManager()
                        .restartLoader(loader_id, null, MainActivity.this)
                        .forceLoad();
            }).show();

            mData = new ArrayList<>();
            handleRecipes();
        }
    }

    private void handleRecipes() {
        if (mData != null) {
            RecipesGridAdapter adapter = (RecipesGridAdapter) listRecyclerview.getAdapter();
            adapter.setList(mData);
            adapter.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_RECIPES_ID:
                showProgressBar();
                return RecipesLoader.createRecipeLoader(this, bakingDatabase.recipesDao());

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        hideProgressBar();

        switch (loader.getId()) {
            case LOADER_RECIPES_ID:
                mData = (List<Recipe>) data;
                break;
        }

        handleRecipes();
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    @Override
    public void onRefresh() {
        startLoader(LOADER_RECIPES_ID);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        mSelectedRecipe = recipe;

        if (mIsTablet && mLandscape)
            updateSecondPane();
        else {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(StepDetailActivity.KEY_RECIPE, mSelectedRecipe);
            startActivity(intent);
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

            mRecipeDetailFragment.databaseUpdated(added);
        });
    }

    @Override
    public void onStepClicked(Step step) {
        Intent intent;
        if (mIsTablet && mLandscape)
            intent = new Intent(this, RecipeDetailActivity.class);
        else
            intent = new Intent(this, StepDetailActivity.class);

        intent.putExtra(StepDetailActivity.KEY_RECIPE, mSelectedRecipe);
        intent.putExtra(StepDetailActivity.KEY_SELECTED_STEP, step);
        startActivity(intent);
    }
}
