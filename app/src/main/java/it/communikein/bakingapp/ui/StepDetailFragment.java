package it.communikein.bakingapp.ui;


import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.squareup.picasso.Picasso;

import it.communikein.bakingapp.BakingApp;
import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.FragmentStepDetailBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepDetailFragment extends Fragment {

    public static final String LOG_TAG = StepDetailFragment.class.getSimpleName();

    public static final String KEY_RECIPE = StepDetailActivity.KEY_RECIPE;
    public static final String KEY_SELECTED_STEP = StepDetailActivity.KEY_SELECTED_STEP;
    public static final String KEY_PLAYER_WINDOW = "player_window";
    public static final String KEY_PLAYER_POSITION = "player_position";
    //public static final String KEY_PLAYER_PLAY_WHEN_READY = "play_when_ready";

    OnChangeStepListener mStepChangesCallback;
    public interface OnChangeStepListener {
        void onStepChanged(Step step);
    }

    FragmentStepDetailBinding mBinding;

    private Recipe mRecipe;
    private Step mSelectedStep;

    SimpleExoPlayer mExoPlayer;
    private int mResumeWindow;
    private long mResumePosition;
    //private boolean mPlayWhenReady;

    private boolean mLandscape;
    private boolean mIsTablet;


    public StepDetailFragment() { }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_step_detail,
                container, false);

        //setRetainInstance(true);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        mIsTablet = getResources().getBoolean(R.bool.isTablet);

        initUI();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        parseData(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mResumeWindow = mExoPlayer.getCurrentWindowIndex();
        mResumePosition = Math.max(0, mExoPlayer.getCurrentPosition());
        //mPlayWhenReady = mExoPlayer.getPlayWhenReady();

        outState.putParcelable(KEY_RECIPE, mRecipe);
        outState.putParcelable(KEY_SELECTED_STEP, mSelectedStep);
        outState.putInt(KEY_PLAYER_WINDOW, mResumeWindow);
        outState.putLong(KEY_PLAYER_POSITION, mResumePosition);
        //outState.putBoolean(KEY_PLAYER_PLAY_WHEN_READY, mPlayWhenReady);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        parseData(savedInstanceState);
    }

    private void parseData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_RECIPE))
                mRecipe = savedInstanceState.getParcelable(KEY_RECIPE);
            if (savedInstanceState.containsKey(KEY_SELECTED_STEP)) {
                mSelectedStep = savedInstanceState.getParcelable(KEY_SELECTED_STEP);
                if (mStepChangesCallback != null)
                    mStepChangesCallback.onStepChanged(mSelectedStep);
            }
            if (savedInstanceState.containsKey(KEY_PLAYER_WINDOW))
                mResumeWindow = savedInstanceState.getInt(KEY_PLAYER_WINDOW);
            if (savedInstanceState.containsKey(KEY_PLAYER_POSITION))
                mResumePosition = savedInstanceState.getLong(KEY_PLAYER_POSITION);
            //if (savedInstanceState.containsKey(KEY_PLAYER_PLAY_WHEN_READY))
            //    mPlayWhenReady = savedInstanceState.getBoolean(KEY_PLAYER_PLAY_WHEN_READY);
        }
    }

    public void printDebug(Bundle savedInstanceState, boolean save) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_RECIPE))
                Log.d(LOG_TAG, (save ? "saved" : "restored") + " recipe: " + savedInstanceState.getParcelable(KEY_RECIPE));
            if (savedInstanceState.containsKey(KEY_SELECTED_STEP))
                Log.d(LOG_TAG, (save ? "saved" : "restored") + " selected step: " + savedInstanceState.getParcelable(KEY_SELECTED_STEP));
        }
    }

    public void updateRecipe(Recipe recipe) {
        mRecipe = recipe;
        if (mSelectedStep != null)
            updateStepsControlButtons();
    }

    public void updateSelectedStep(Step step) {
        this.mSelectedStep = step;

        if (mExoPlayer != null)
            mExoPlayer.stop();
        updateUI();
    }

    private void initUI() {
        if (mRecipe == null || mSelectedStep == null) return;

        if (!mLandscape || mIsTablet)
            initStepsControlButtons();
        initPlayer();
    }

    private void updateUI() {
        if (!mLandscape || mIsTablet) {
            mBinding.labelStepDetail.setText(mSelectedStep.getShortDescription());
            mBinding.stepDetailTextview.setText(mSelectedStep.getDescription());

            if (!mBinding.nextStepButton.hasOnClickListeners() ||
                    !mBinding.prevStepButton.hasOnClickListeners())
                initStepsControlButtons();
            updateStepsControlButtons();
        }
        updatePlayer();
    }

    private void initStepsControlButtons() {
        mBinding.nextStepButton.setOnClickListener(v -> {
            mSelectedStep = mRecipe.getSteps().get(mRecipe.getSteps().indexOf(mSelectedStep) + 1);
            if (mStepChangesCallback != null)
                mStepChangesCallback.onStepChanged(mSelectedStep);

            mExoPlayer.stop();
            mResumePosition = 0;
            mResumeWindow = 0;

            updateUI();
        });
        mBinding.prevStepButton.setOnClickListener(v -> {
            mSelectedStep = mRecipe.getSteps().get(mRecipe.getSteps().indexOf(mSelectedStep) - 1);
            if (mStepChangesCallback != null)
                mStepChangesCallback.onStepChanged(mSelectedStep);

            mExoPlayer.stop();
            mResumePosition = 0;
            mResumeWindow = 0;

            updateUI();
        });
    }

    private void updateStepsControlButtons() {
        Step firstStep = mRecipe.getSteps().get(0);
        if (mSelectedStep.equals(firstStep))
            mBinding.prevStepButton.setVisibility(View.GONE);
        else
            mBinding.prevStepButton.setVisibility(View.VISIBLE);

        Step lastStep = mRecipe.getSteps().get(mRecipe.getSteps().size() -1);
        if (mSelectedStep.equals(lastStep))
            mBinding.nextStepButton.setVisibility(View.GONE);
        else
            mBinding.nextStepButton.setVisibility(View.VISIBLE);
    }

    public void setStepChangedListener(OnChangeStepListener listener) {
        this.mStepChangesCallback = listener;
    }

    private void initPlayer() {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);

            mBinding.playerView.requestFocus();
            mBinding.playerView.setPlayer(mExoPlayer);
        }
    }

    public void enterFullScreen() {
        mBinding.labelVideoNotAvailable.setVisibility(View.VISIBLE);
        mBinding.stepDetailContainer.setVisibility(View.GONE);

        ViewGroup.LayoutParams layoutParams = mBinding.playerView.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

        mBinding.playerView.setLayoutParams(layoutParams);
        mBinding.playerView.requestLayout();
    }

    public void exitFullScreen() {
        mBinding.labelVideoNotAvailable.setVisibility(View.GONE);
        mBinding.stepDetailContainer.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams layoutParams = mBinding.playerView.getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.video_height);
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

        mBinding.playerView.setLayoutParams(layoutParams);
        mBinding.playerView.requestLayout();
    }

    private void updatePlayer() {
        if (mExoPlayer == null) initPlayer();

        boolean hasVideo = !TextUtils.isEmpty(mSelectedStep.getVideoURL());
        boolean hasThumbnail = !TextUtils.isEmpty(mSelectedStep.getThumbnailURL());
        if (hasVideo) {
            mBinding.labelVideoNotAvailable.setVisibility(View.GONE);
            mBinding.stepThumbnail.setVisibility(View.GONE);
            mBinding.playerView.setVisibility(View.VISIBLE);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getActivity(), BakingApp.class.getName());
            String videoURL = mSelectedStep.getVideoURL();

            MediaSource mediaSource = new ExtractorMediaSource(
                    Uri.parse(videoURL),
                    new DefaultDataSourceFactory(getActivity(), userAgent),
                    new DefaultExtractorsFactory(),
                    null,
                    null);
            mExoPlayer.prepare(mediaSource);

            boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
            if (haveResumePosition)
                mExoPlayer.seekTo(mResumeWindow, mResumePosition);
            mExoPlayer.setPlayWhenReady(true);
        }
        else if (hasThumbnail) {
            mBinding.labelVideoNotAvailable.setVisibility(View.GONE);
            mBinding.stepThumbnail.setVisibility(View.VISIBLE);
            mBinding.playerView.setVisibility(View.GONE);

            Picasso.get().load(mSelectedStep.getThumbnailURL()).into(mBinding.stepThumbnail);
        }
        else {
            mBinding.labelVideoNotAvailable.setVisibility(View.VISIBLE);
            mBinding.stepThumbnail.setVisibility(View.GONE);
            mBinding.playerView.setVisibility(View.GONE);
        }
    }


}
