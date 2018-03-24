package it.communikein.bakingapp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import it.communikein.bakingapp.BakingApp;
import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.databinding.ActivityStepDetailBinding;

public class StepDetailActivity extends AppCompatActivity {

    public static final String KEY_RECIPE = "recipe";
    public static final String KEY_STEP_SELECTED = "step_selected";

    public static final String KEY_PLAYER_WINDOW = "player_window";
    public static final String KEY_PLAYER_POSITION = "player_position";

    ActivityStepDetailBinding mBinding;
    SimpleExoPlayer mExoPlayer;
    private int mResumeWindow;
    private long mResumePosition;
    private boolean mLandscape;

    private Recipe mRecipe;
    private int mSelectedStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_step_detail);

        mLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        parseData(savedInstanceState);
        initUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mExoPlayer != null) {
            mResumeWindow = mExoPlayer.getCurrentWindowIndex();
            mResumePosition = Math.max(0, mExoPlayer.getCurrentPosition());

            mExoPlayer.release();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_RECIPE, mRecipe);
        outState.putInt(KEY_STEP_SELECTED, mSelectedStep);
        outState.putInt(KEY_PLAYER_WINDOW, mResumeWindow);
        outState.putLong(KEY_PLAYER_POSITION, mResumePosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        parseDataFromBundle(savedInstanceState);
    }

    private void parseDataFromBundle(Bundle data) {
        if (data == null) return;

        if (data.containsKey(KEY_RECIPE))
            mRecipe = data.getParcelable(KEY_RECIPE);
        if (data.containsKey(KEY_STEP_SELECTED))
            mSelectedStep = data.getInt(KEY_STEP_SELECTED);
        if (data.containsKey(KEY_PLAYER_WINDOW))
            mResumeWindow = data.getInt(KEY_PLAYER_WINDOW);
        if (data.containsKey(KEY_PLAYER_POSITION))
            mResumePosition = data.getLong(KEY_PLAYER_POSITION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);

        return super.onOptionsItemSelected(item);
    }


    private void parseData(Bundle savedInstanceState) {
        Intent startIntent = getIntent();
        if (startIntent == null) {
            finish();
            return;
        }

        mRecipe = startIntent.getParcelableExtra(KEY_RECIPE);
        mSelectedStep = startIntent.getIntExtra(KEY_STEP_SELECTED, 0);

        parseDataFromBundle(savedInstanceState);
    }

    private void initUI() {
        if (mRecipe == null || mSelectedStep == -1) return;

        if (!mLandscape) {
            initToolbar();
            initStepsControlButtons();
        }
        initPlayer();

        updateUI();
    }

    private void updateUI() {
        if (!mLandscape) {
            mBinding.labelStepDetail.setText(mRecipe.getSteps()
                    .get(mSelectedStep).getShortDescription());
            mBinding.stepDetailTextview.setText(mRecipe.getSteps()
                    .get(mSelectedStep).getDescription());

            updateStepsControlButtons();
        }
        updatePlayer();
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(mRecipe.getName());
    }

    private void initStepsControlButtons() {
        updateStepsControlButtons();

        mBinding.nextStepButton.setOnClickListener(v -> {
            mSelectedStep++;

            mExoPlayer.stop();
            mResumePosition = 0;
            mResumeWindow = 0;

            updateUI();
        });
        mBinding.prevStepButton.setOnClickListener(v -> {
            mSelectedStep--;

            mExoPlayer.stop();
            mResumePosition = 0;
            mResumeWindow = 0;

            updateUI();
        });
    }

    private void updateStepsControlButtons() {
        if (mSelectedStep == 0)
            mBinding.prevStepButton.setVisibility(View.GONE);
        else
            mBinding.prevStepButton.setVisibility(View.VISIBLE);

        if (mSelectedStep == mRecipe.getSteps().size() - 1)
            mBinding.nextStepButton.setVisibility(View.GONE);
        else
            mBinding.nextStepButton.setVisibility(View.VISIBLE);
    }

    private void initPlayer() {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

            mBinding.playerView.requestFocus();
            mBinding.playerView.setPlayer(mExoPlayer);

            int fullscreenFlags;
            if (mLandscape) {
                fullscreenFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    fullscreenFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            }
            else
                fullscreenFlags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            getWindow().getDecorView().setSystemUiVisibility(fullscreenFlags);
        }
    }

    private void updatePlayer() {
        boolean hasVideo = !TextUtils.isEmpty(mRecipe.getSteps().get(mSelectedStep).getVideoURL());
        if (hasVideo) {
            mBinding.labelVideoNotAvailable.setVisibility(View.GONE);
            mBinding.playerView.setVisibility(View.VISIBLE);

            if (!mLandscape) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mBinding.constraintView);
                constraintSet.connect(R.id.label_step_detail, ConstraintSet.TOP,
                        R.id.playerView, ConstraintSet.BOTTOM);
                constraintSet.applyTo(mBinding.constraintView);
            }

            boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
            if (haveResumePosition)
                mBinding.playerView.getPlayer().seekTo(mResumeWindow, mResumePosition);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(this, BakingApp.class.getName());
            MediaSource mediaSource = new ExtractorMediaSource(
                    Uri.parse(mRecipe.getSteps().get(mSelectedStep).getVideoURL()),
                    new DefaultDataSourceFactory(this, userAgent),
                    new DefaultExtractorsFactory(),
                    null,
                    null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
        else {
            mBinding.labelVideoNotAvailable.setVisibility(View.VISIBLE);
            mBinding.playerView.setVisibility(View.GONE);

            if (!mLandscape) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mBinding.constraintView);
                constraintSet.connect(R.id.label_step_detail, ConstraintSet.TOP,
                        R.id.label_video_not_available, ConstraintSet.BOTTOM);
                constraintSet.applyTo(mBinding.constraintView);
            }
        }
    }
}
