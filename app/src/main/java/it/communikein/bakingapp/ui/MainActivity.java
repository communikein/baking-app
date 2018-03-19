package it.communikein.bakingapp.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import it.communikein.bakingapp.R;
import it.communikein.bakingapp.RecipesGridAdapter;
import it.communikein.bakingapp.RecipesListAdapter;
import it.communikein.bakingapp.databinding.ActivityMainBinding;
import it.communikein.bakingapp.model.Recipe;
import it.communikein.bakingapp.network.NetworkUtils;
import it.communikein.bakingapp.network.RecipesLoader;

public class MainActivity extends AppCompatActivity implements
        HasSupportFragmentInjector, RecipesListAdapter.RecipeClickCallback,
        LoaderManager.LoaderCallbacks, SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String KEY_DATASET = "dataset";
    private static final String KEY_FIRST_VISIBLE_ITEM_POS = "first_visible_item_pos";
    private static final String KEY_SHOW_LIST = "show_list";

    private static final int LOADER_RECIPES_ID = 1001;

    private ActivityMainBinding mBinding;

    private List<Recipe> mData;
    private boolean showList;
    private RecyclerView.Adapter recipesAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private int lastItemPosition = -1;
    private int firstVisibleItemPosition = -1;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;


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
        RecipesListAdapter adapter = (RecipesListAdapter) mBinding.listRecyclerview.getAdapter();
        adapter.setList(mData);
        adapter.notifyDataSetChanged();

        if (mData.size() > 0) {
            if (savedInstanceState.containsKey(KEY_FIRST_VISIBLE_ITEM_POS))
                firstVisibleItemPosition = savedInstanceState.getInt(KEY_FIRST_VISIBLE_ITEM_POS);
            else
                firstVisibleItemPosition = 0;
            mBinding.listRecyclerview.smoothScrollToPosition(firstVisibleItemPosition);
        }

        showList = savedInstanceState.getBoolean(KEY_SHOW_LIST, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mData != null)
            outState.putParcelableArrayList(KEY_DATASET, new ArrayList<>(mData));
        outState.putInt(KEY_FIRST_VISIBLE_ITEM_POS, firstVisibleItemPosition);
        outState.putBoolean(KEY_SHOW_LIST, showList);
    }

    private void showProgressBar() {
        mBinding.swipeRefresh.setRefreshing(true);
    }

    private void hideProgressBar() {
        mBinding.swipeRefresh.setRefreshing(false);
    }


    private void initUI() {
        int fabResource = R.drawable.ic_view_module_black;
        if (showList) fabResource = R.drawable.ic_view_list_black;
        mBinding.changeLayoutFab.setImageResource(fabResource);
        mBinding.changeLayoutFab.setOnClickListener(v -> {
            showList = !showList;
            updateListLayout();
        });

        initRecipesList();
    }

    private void updateListLayout() {
        LayoutAnimationController animation;
        if (showList) {
            mBinding.changeLayoutFab.setImageResource(R.drawable.ic_view_module_black);

            layoutManager = new LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL, false);
            recipesAdapter = new RecipesListAdapter(this);
            ((RecipesListAdapter) recipesAdapter).setList(mData);

            animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_from_bottom);
        }
        else {
            mBinding.changeLayoutFab.setImageResource(R.drawable.ic_view_list_black);

            layoutManager = new GridLayoutManager(this,
                    numberOfColumns(), GridLayoutManager.VERTICAL, false);
            recipesAdapter = new RecipesGridAdapter(this);
            ((RecipesGridAdapter) recipesAdapter).setList(mData);

            animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_from_bottom);
        }

        mBinding.listRecyclerview.setLayoutManager(layoutManager);
        mBinding.listRecyclerview.setAdapter(recipesAdapter);
        mBinding.listRecyclerview.setLayoutAnimation(animation);
        mBinding.listRecyclerview.scheduleLayoutAnimation();
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
        if (layoutManager == null)
            updateListLayout();
        mBinding.listRecyclerview.setHasFixedSize(true);

        mBinding.listRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (showList) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

                    lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                    firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                }
                else {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;

                    lastItemPosition = gridLayoutManager.findLastVisibleItemPosition();
                    firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition();
                }
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
        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_SHOW_LIST))
            showList = true;
    }

    private void handleRecipes() {
        if (mData != null) {
            RecipesListAdapter adapter = (RecipesListAdapter) mBinding.listRecyclerview.getAdapter();
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
                return RecipesLoader.createRecipeLoader(this);

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
        // TODO: Implement the RecipeDetailsActivity class
        /*
        Intent intent = new Intent(this, RecipeDetailsActivity.class);
        intent.putExtra(RecipeDetailsActivity.KEY_RECIPE, recipe);
        startActivity(intent);
        */
    }




    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}
