package it.communikein.bakingapp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import it.communikein.bakingapp.R;
import it.communikein.bakingapp.RecipesGridAdapter;
import it.communikein.bakingapp.data.database.RecipesDao;
import it.communikein.bakingapp.databinding.ActivityMainBinding;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.network.NetworkUtils;
import it.communikein.bakingapp.data.network.RecipesLoader;

public class MainActivity extends AppCompatActivity implements
        RecipesGridAdapter.RecipeClickCallback, LoaderManager.LoaderCallbacks,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String KEY_DATASET = "dataset";
    private static final String KEY_FIRST_VISIBLE_ITEM_POS = "first_visible_item_pos";
    private static final String KEY_SHOW_GRID = "show_grid";

    private static final int LOADER_RECIPES_ID = 1001;

    private ActivityMainBinding mBinding;

    private List<Recipe> mData;

    private int firstVisibleItemPosition = -1;
    private boolean mLandscape;
    private boolean mGridLayout;

    @Inject
    RecipesDao recipesDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        /* Show data downloading */
        mBinding.swipeRefresh.setOnRefreshListener(this);
        mBinding.swipeRefresh.setRefreshing(false);

        mLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;

        initData(savedInstanceState);
        initUI();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        hideProgressBar();
        if (savedInstanceState == null) return;
        if (!savedInstanceState.containsKey(KEY_DATASET)) return;

        mData = savedInstanceState.getParcelable(KEY_DATASET);
        if (mData == null)
            onRefresh();
        else if (mData.size() > 0) {
            if (savedInstanceState.containsKey(KEY_FIRST_VISIBLE_ITEM_POS))
                firstVisibleItemPosition = savedInstanceState.getInt(KEY_FIRST_VISIBLE_ITEM_POS);
            else
                firstVisibleItemPosition = 0;
            mBinding.listRecyclerview.smoothScrollToPosition(firstVisibleItemPosition);
        }

        mGridLayout = savedInstanceState.getBoolean(KEY_SHOW_GRID, false);

        RecipesGridAdapter adapter = (RecipesGridAdapter) mBinding.listRecyclerview.getAdapter();
        adapter.setList(mData);
        adapter.setLayout(showGridLayout());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mData != null)
            outState.putParcelableArrayList(KEY_DATASET, new ArrayList<>(mData));
        outState.putInt(KEY_FIRST_VISIBLE_ITEM_POS, firstVisibleItemPosition);
        outState.putBoolean(KEY_SHOW_GRID, mGridLayout);
    }

    private void showProgressBar() {
        mBinding.swipeRefresh.setRefreshing(true);
    }

    private void hideProgressBar() {
        mBinding.swipeRefresh.setRefreshing(false);
    }


    private void initUI() {
        initRecipesList();

        if (mLandscape)
            mBinding.changeLayoutFab.setVisibility(View.GONE);
        else
            initFab();
    }

    private void initFab() {
        mBinding.changeLayoutFab.setVisibility(View.VISIBLE);

        int fabResource = R.drawable.ic_view_module;
        if (mGridLayout) fabResource = R.drawable.ic_view_list;
        mBinding.changeLayoutFab.setImageResource(fabResource);
        mBinding.changeLayoutFab.setOnClickListener(v -> {
            mGridLayout = !mGridLayout;

            GridLayoutManager layoutManager = (GridLayoutManager)
                    mBinding.listRecyclerview.getLayoutManager();
            RecipesGridAdapter adapter = (RecipesGridAdapter)
                    mBinding.listRecyclerview.getAdapter();

            updateFab();
            updateListLayout(layoutManager, adapter);
        });
    }

    private void updateFab() {
        if (mLandscape)
            mBinding.changeLayoutFab.setVisibility(View.GONE);
        else {
            mBinding.changeLayoutFab.setVisibility(View.VISIBLE);

            if (showGridLayout())
                mBinding.changeLayoutFab.setImageResource(R.drawable.ic_view_list);
            else
                mBinding.changeLayoutFab.setImageResource(R.drawable.ic_view_module);
        }
    }

    private void updateListLayout(@NonNull GridLayoutManager layoutManager, @NonNull RecipesGridAdapter adapter) {
        adapter.setLayout(mGridLayout);

        int cols = 1;
        if (showGridLayout()) cols = numberOfColumns();
        layoutManager.setSpanCount(cols);

        if (mBinding.listRecyclerview.getAdapter() == null) {
            mBinding.listRecyclerview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        else {
            mBinding.listRecyclerview.getRecycledViewPool().clear();
            mBinding.listRecyclerview.swapAdapter(adapter, true);
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }

        mBinding.listRecyclerview.scheduleLayoutAnimation();
    }

    private boolean showGridLayout() {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        return isTablet || mLandscape || mGridLayout;
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
        mBinding.listRecyclerview.setLayoutManager(layoutManager);

        RecipesGridAdapter adapter = new RecipesGridAdapter(showGridLayout(), this, this);
        adapter.setList(mData);

        updateListLayout(layoutManager, adapter);
        mBinding.listRecyclerview.setHasFixedSize(true);
        mBinding.listRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            }
        });
    }

    private void startLoader(int loader_id) {
        mBinding.swipeRefresh.setRefreshing(true);

        if (loader_id == LOADER_RECIPES_ID || NetworkUtils.isDeviceOnline(this)) {
            getSupportLoaderManager()
                    .restartLoader(loader_id, null, MainActivity.this)
                    .forceLoad();
        }
        else {
            mBinding.swipeRefresh.setRefreshing(false);

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


    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_DATASET))
            onRefresh();
        mGridLayout = savedInstanceState != null &&
                savedInstanceState.getBoolean(KEY_SHOW_GRID, false);
    }

    private void handleRecipes() {
        if (mData != null) {
            RecipesGridAdapter adapter = (RecipesGridAdapter) mBinding.listRecyclerview.getAdapter();
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
                return RecipesLoader.createRecipeLoader(this, recipesDao);

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
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.KEY_RECIPE, recipe);
        startActivity(intent);
    }
}
