package it.communikein.bakingapp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.ActivityStepDetailBinding;

public class StepDetailActivity extends AppCompatActivity
        implements StepDetailFragment.OnChangeStepListener {

    public static final String LOG_TAG = StepDetailActivity.class.getSimpleName();

    public static final String KEY_RECIPE = "recipe";
    public static final String KEY_SELECTED_STEP = "selected_step";
    public static final String KEY_FIRST_LAUNCH = "first_launch";

    ActivityStepDetailBinding mBinding;
    StepDetailFragment mStepDetailFragment;

    private boolean mLandscape;
    private boolean mIsTablet;
    private boolean mIsFirstLaunch;

    private Recipe mRecipe;
    private Step mSelectedStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_step_detail);

        mLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        mIsTablet = getResources().getBoolean(R.bool.isTablet);

        Log.d(LOG_TAG, LOG_TAG + " - onCreate");
        printDebug(savedInstanceState, false);

        parseData(savedInstanceState);
        initUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_FIRST_LAUNCH, false);

        Log.d(LOG_TAG, LOG_TAG + " - onSaveInstanceState");
        printDebug(outState, true);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(LOG_TAG, LOG_TAG + " - onRestoreInstanceState");
        printDebug(savedInstanceState, false);
    }

    public void printDebug(Bundle savedInstanceState, boolean save) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_RECIPE))
                Log.d(LOG_TAG, (save ? "saved" : "restored") + " recipe: " + savedInstanceState.getParcelable(KEY_RECIPE));
            if (savedInstanceState.containsKey(KEY_SELECTED_STEP))
                Log.d(LOG_TAG, (save ? "saved" : "restored") + " step: " + savedInstanceState.getParcelable(KEY_SELECTED_STEP));
        }
    }


    private void parseData(Bundle savedInstanceState) {
        Intent startIntent = getIntent();
        if (startIntent == null) {
            finish();
            return;
        }
        mRecipe = startIntent.getParcelableExtra(KEY_RECIPE);

        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_FIRST_LAUNCH)) {
            mIsFirstLaunch = true;
            mSelectedStep = startIntent.getParcelableExtra(KEY_SELECTED_STEP);
        }
        else mIsFirstLaunch = false;
    }

    private void initUI() {
        if (mIsFirstLaunch && (mRecipe == null || mSelectedStep == null)) return;

        mStepDetailFragment = (StepDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.step_detail_fragment);
        mStepDetailFragment.updateRecipe(mRecipe);

        if (!mLandscape || mIsTablet)
            initToolbar();

        if (mLandscape)
            enterFullScreen();
        else
            exitFullScreen();

        if (mIsFirstLaunch) {
            mStepDetailFragment = (StepDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.step_detail_fragment);
            mStepDetailFragment.updateRecipe(mRecipe);
            mStepDetailFragment.updateSelectedStep(mSelectedStep);
            mStepDetailFragment.setStepChangedListener(this);
        }
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(mRecipe.getName());
    }

    private void enterFullScreen() {
        mBinding.appbar.setVisibility(View.GONE);

        int fullscreenFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            fullscreenFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().getDecorView().setSystemUiVisibility(fullscreenFlags);

        mStepDetailFragment.enterFullScreen();
    }

    private void exitFullScreen() {
        mBinding.appbar.setVisibility(View.VISIBLE);
        getWindow().getDecorView().setSystemUiVisibility(0);

        mStepDetailFragment.exitFullScreen();
    }

    @Override
    public void onStepChanged(Step step) {
        this.mSelectedStep = step;
    }
}
