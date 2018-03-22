package it.communikein.bakingapp.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

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

import java.util.ArrayList;

import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.ActivityStepDetailBinding;

public class StepDetailActivity extends AppCompatActivity {

    public static final String KEY_RECIPE = "recipe";
    public static final String KEY_STEP_SELECTED = "step_selected";

    ActivityStepDetailBinding mBinding;
    SimpleExoPlayer mExoPlayer;

    private Recipe mRecipe;
    private int mSelectedStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_step_detail);

        parseData();
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_RECIPE, mRecipe);
        outState.putInt(KEY_STEP_SELECTED, mSelectedStep);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(KEY_RECIPE))
            mRecipe = savedInstanceState.getParcelable(KEY_RECIPE);
        if (savedInstanceState.containsKey(KEY_STEP_SELECTED))
            mSelectedStep = savedInstanceState.getInt(KEY_STEP_SELECTED);
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
        mSelectedStep = startIntent.getIntExtra(KEY_STEP_SELECTED, 0);
    }

    private void initUI() {
        if (mRecipe == null || mSelectedStep == -1) return;

        initToolbar();
        initFab();
        initPlayer();

        updateUI();
    }

    private void updateUI() {
        mBinding.labelStepDetail.setText(mRecipe.getSteps()
                .get(mSelectedStep).getShortDescription());
        mBinding.stepDetailTextview.setText(mRecipe.getSteps()
                .get(mSelectedStep).getDescription());

        updateFab();
        updatePlayer();
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(mRecipe.getName());
    }

    private void initFab() {
        updateFab();

        mBinding.nextStepFab.setOnClickListener(v -> {
            mSelectedStep++;
            updateUI();
        });
        mBinding.previousStepFab.setOnClickListener(v -> {
            mSelectedStep--;
            updateUI();
        });
    }

    private void updateFab() {
        if (mSelectedStep == 0) {
            mBinding.previousStepFab.setVisibility(View.GONE);
            mBinding.previousStepFab.setEnabled(false);
        }
        else {
            mBinding.previousStepFab.setVisibility(View.VISIBLE);
            mBinding.previousStepFab.setEnabled(true);
        }

        if (mSelectedStep == mRecipe.getSteps().size() - 1) {
            mBinding.nextStepFab.setVisibility(View.GONE);
            mBinding.nextStepFab.setEnabled(false);
        }
        else {
            mBinding.nextStepFab.setVisibility(View.VISIBLE);
            mBinding.nextStepFab.setEnabled(true);
        }
    }

    private void initPlayer() {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

            mBinding.playerView.requestFocus();
            mBinding.playerView.setPlayer(mExoPlayer);
        }
    }

    private void updatePlayer() {
        if (!TextUtils.isEmpty(mRecipe.getSteps().get(mSelectedStep).getVideoURL())) {
            mBinding.labelVideoNotAvailable.setVisibility(View.GONE);
            mBinding.playerView.setVisibility(View.VISIBLE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mBinding.constraintView);
            constraintSet.connect(R.id.label_step_detail, ConstraintSet.TOP,
                    R.id.playerView, ConstraintSet.BOTTOM);
            constraintSet.applyTo(mBinding.constraintView);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(this, "ClassicalMusicQuiz");
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

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mBinding.constraintView);
            constraintSet.connect(R.id.label_step_detail, ConstraintSet.TOP,
                    R.id.label_video_not_available, ConstraintSet.BOTTOM);
            constraintSet.applyTo(mBinding.constraintView);
        }
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }
}
