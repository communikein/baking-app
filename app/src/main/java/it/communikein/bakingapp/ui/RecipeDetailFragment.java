package it.communikein.bakingapp.ui;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import it.communikein.bakingapp.R;
import it.communikein.bakingapp.StepsListAdapter;
import it.communikein.bakingapp.Utils;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.FragmentRecipeDetailBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeDetailFragment extends Fragment implements
        StepsListAdapter.StepClickCallback {

    public static final String LOG_TAG = RecipeDetailFragment.class.getSimpleName();

    OnFavouriteClickListener mfavouriteClickCallback;
    public interface OnFavouriteClickListener {
        void onFavouriteClicked(Recipe recipe);
    }

    OnStepClickListener mStepClickCallback;
    public interface OnStepClickListener {
        void onStepClicked(Step step);
    }

    FragmentRecipeDetailBinding mBinding;

    private Recipe mRecipe;

    public RecipeDetailFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mfavouriteClickCallback = (OnFavouriteClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFavouriteClickListener");
        }

        try {
            mStepClickCallback = (OnStepClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnStepClickListener");
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipe_detail,
                container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();
    }

    public void setFavouriteClickListener(OnFavouriteClickListener listener) {
        this.mfavouriteClickCallback = listener;
    }

    private void initUI() {
        if (mRecipe == null) return;

        mBinding.nameTextview.setText(mRecipe.getName());
        mBinding.servingsTextview.setText(String.valueOf(mRecipe.getServings()));
        mBinding.ingredientsTextview.setText(mRecipe.printIngredients());

        updateUI();

        initFab();
        initStepsList();
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return mBinding.coordinatorView;
    }

    private void initFab() {
        updateFab(mRecipe.isFavourite());

        mBinding.favoriteFab.setOnClickListener(v -> {
            if (mfavouriteClickCallback != null)
                mfavouriteClickCallback.onFavouriteClicked(mRecipe);
        });
    }

    public void updateFab(boolean favourite) {
        if (favourite)
            mBinding.favoriteFab.setImageDrawable(Utils.getDrawableColored(
                    R.drawable.ic_star_border,
                    R.color.black,
                    getActivity()));
        else
            mBinding.favoriteFab.setImageDrawable(Utils.getDrawableColored(
                    R.drawable.ic_star,
                    R.color.black,
                    getActivity()));
    }

    private void initStepsList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
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
        else
            Picasso.get()
                    .load(mRecipe.getImage())
                    .error(Utils.getDrawableColored(R.drawable.ic_broken_image,
                            R.color.black, getActivity()))
                    .placeholder(Utils.getDrawableColored(R.drawable.ic_image,
                            R.color.black, getActivity()))
                    .into(mBinding.recipeImageview);
    }


    public void setStepClickListener(OnStepClickListener listener) {
        this.mStepClickCallback = listener;
    }

    @Override
    public void onStepClick(Step step) {
        if (mStepClickCallback != null)
            mStepClickCallback.onStepClicked(step);
    }

    public void updateRecipe(Recipe recipe) {
        mRecipe = recipe;
        initUI();
    }

    public void databaseUpdated(boolean added) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                updateFab(added);

                String message;
                if (added)
                    message = getString(R.string.label_recipe_added_to_favourites);
                else
                    message = getString(R.string.label_recipe_removed_from_favourites);

                Snackbar.make(mBinding.coordinatorView, message, Snackbar.LENGTH_LONG).show();
            });
        }
    }
}
