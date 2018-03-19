package it.communikein.bakingapp;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.communikein.bakingapp.databinding.GridItemRecipeBinding;
import it.communikein.bakingapp.model.Recipe;
import it.communikein.bakingapp.RecipesGridAdapter.RecipeViewHolder;

public class RecipesGridAdapter extends RecyclerView.Adapter<RecipeViewHolder> {

    private List<Recipe> mList;

    @Nullable
    private final RecipesListAdapter.RecipeClickCallback mOnClickListener;

    public RecipesGridAdapter(@Nullable RecipesListAdapter.RecipeClickCallback listener) {
        this.mOnClickListener = listener;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        GridItemRecipeBinding mBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.grid_item_recipe,
                        parent,
                        false);

        return new RecipeViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = mList.get(position);

        holder.bindData(recipe);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public void setList(final List<Recipe> newList) {
        final List<Recipe> tempList = new ArrayList<>();
        if (newList != null)
            tempList.addAll(newList);

        if (mList == null && tempList.size() > 0) {
            mList = tempList;
            notifyItemRangeInserted(0, mList.size());
        }
        else if (tempList.size() == 0) {
            mList = new ArrayList<>();
            notifyDataSetChanged();
        }
        else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return tempList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).equals(tempList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Recipe newItem = tempList.get(newItemPosition);
                    Recipe oldItem = mList.get(oldItemPosition);
                    return oldItem.displayEquals(newItem);
                }
            });
            mList = tempList;
            result.dispatchUpdatesTo(this);
        }
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final GridItemRecipeBinding mBinding;

        RecipeViewHolder(GridItemRecipeBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setFocusable(true);
            binding.getRoot().setClickable(true);

            this.mBinding = binding;
        }

        @Override
        public void onClick(View v) {
            Recipe clicked = mBinding.getRecipe();

            if (mOnClickListener != null)
                mOnClickListener.onRecipeClick(clicked);
        }

        void bindData(Recipe recipe) {
            mBinding.setRecipe(recipe);

            mBinding.recipeNameTextview.setText(recipe.getName());
        }
    }

}
