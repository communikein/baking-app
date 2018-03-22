package it.communikein.bakingapp.ui;

import android.content.Intent;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

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
    private static final String KEY_SHOW_LIST = "show_list";

    private static final int LOADER_RECIPES_ID = 1001;

    private ActivityMainBinding mBinding;

    private List<Recipe> mData;

    private int lastItemPosition = -1;
    private int firstVisibleItemPosition = -1;

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
        RecipesGridAdapter adapter = (RecipesGridAdapter) mBinding.listRecyclerview.getAdapter();
        adapter.setList(mData);
        adapter.notifyDataSetChanged();

        if (mData.size() > 0) {
            if (savedInstanceState.containsKey(KEY_FIRST_VISIBLE_ITEM_POS))
                firstVisibleItemPosition = savedInstanceState.getInt(KEY_FIRST_VISIBLE_ITEM_POS);
            else
                firstVisibleItemPosition = 0;
            mBinding.listRecyclerview.smoothScrollToPosition(firstVisibleItemPosition);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mData != null)
            outState.putParcelableArrayList(KEY_DATASET, new ArrayList<>(mData));
        outState.putInt(KEY_FIRST_VISIBLE_ITEM_POS, firstVisibleItemPosition);
    }

    private void showProgressBar() {
        mBinding.swipeRefresh.setRefreshing(true);
    }

    private void hideProgressBar() {
        mBinding.swipeRefresh.setRefreshing(false);
    }


    private void initUI() {
        initRecipesList();
        RecipesGridAdapter adapter = (RecipesGridAdapter) mBinding.listRecyclerview.getAdapter();

        int fabResource = R.drawable.ic_view_module;
        if (adapter.isListLayout()) fabResource = R.drawable.ic_view_list;
        mBinding.changeLayoutFab.setImageResource(fabResource);
        mBinding.changeLayoutFab.setOnClickListener(v -> {
            updateListLayout(!adapter.isListLayout());
        });
    }

    private void updateListLayout(boolean listLayout) {
        RecipesGridAdapter adapter = (RecipesGridAdapter)
                mBinding.listRecyclerview.getAdapter();
        GridLayoutManager layoutManager = (GridLayoutManager)
                mBinding.listRecyclerview.getLayoutManager();
        int cols = 1;

        if (listLayout) {
            mBinding.changeLayoutFab.setImageResource(R.drawable.ic_view_module);
            adapter.toListLayout();
        }
        else {
            mBinding.changeLayoutFab.setImageResource(R.drawable.ic_view_list);
            cols = numberOfColumns();
            adapter.toGridLayout();
        }

        layoutManager.setSpanCount(cols);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        adapter.setList(mData);
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2 ) return 2;	// to keep the grid aspect
        return nColumns;
    }

    private void initRecipesList() {
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 1,
                GridLayoutManager.VERTICAL, false);
        mBinding.listRecyclerview.setLayoutManager(layoutManager);

        final LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this,
                R.anim.layout_animation_from_bottom);
        mBinding.listRecyclerview.setLayoutAnimation(animation);

        final RecipesGridAdapter recipesAdapter = new RecipesGridAdapter(this, this);
        mBinding.listRecyclerview.setAdapter(recipesAdapter);
        mBinding.listRecyclerview.setHasFixedSize(true);

        recipesAdapter.setList(mData);
        recipesAdapter.notifyDataSetChanged();
        mBinding.listRecyclerview.scheduleLayoutAnimation();

        mBinding.listRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                lastItemPosition = layoutManager.findLastVisibleItemPosition();
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
