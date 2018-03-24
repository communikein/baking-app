package it.communikein.bakingapp;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.communikein.bakingapp.databinding.GridItemRecipeBinding;
import it.communikein.bakingapp.data.model.Recipe;
import it.communikein.bakingapp.RecipesGridAdapter.RecipeViewHolder;
import it.communikein.bakingapp.databinding.ListItemRecipeBinding;

/*
    Icons made by Vectors Market (https://www.flaticon.com/authors/vectors-market)
    from https://www.flaticon.com/
    is licensed by CC 3.0 BY (http://creativecommons.org/licenses/by/3.0/)
*/
public class RecipesGridAdapter extends RecyclerView.Adapter<RecipeViewHolder> {

    private static final int GRID_ITEM = -1;
    private static final int LIST_ITEM = -2;

    private Context mContext;

    private List<Recipe> mList;
    private int mLayoutType;

    @Nullable
    private final RecipeClickCallback mOnClickListener;
    public interface RecipeClickCallback {
        void onRecipeClick(Recipe recipe);
    }

    public RecipesGridAdapter(boolean gridLayout, Context context, @Nullable RecipeClickCallback listener) {
        this.mContext = context;
        this.mOnClickListener = listener;
        this.mLayoutType = gridLayout ? GRID_ITEM : LIST_ITEM;
    }

    @Override @NonNull
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GRID_ITEM) {
            GridItemRecipeBinding mBinding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.grid_item_recipe,
                            parent,
                            false);

            return new RecipeViewHolder(mBinding);
        }
        else {
            ListItemRecipeBinding mBinding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.list_item_recipe,
                            parent,
                            false);

            return new RecipeViewHolder(mBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = mList.get(position);

        holder.bindData(recipe);
    }

    @Override
    public int getItemViewType(int position) {
        return mLayoutType;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setLayout(boolean gridLayout) {
        this.mLayoutType = gridLayout ? GRID_ITEM : LIST_ITEM;
    }

    public boolean isListLayout() { return this.mLayoutType == LIST_ITEM; }

    public boolean isGridLayout() { return this.mLayoutType == GRID_ITEM; }

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

        private final GridItemRecipeBinding mGridBinding;
        private final ListItemRecipeBinding mListBinding;

        RecipeViewHolder(GridItemRecipeBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setFocusable(true);
            binding.getRoot().setClickable(true);

            this.mGridBinding = binding;
            this.mListBinding = null;
        }

        RecipeViewHolder(ListItemRecipeBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setFocusable(true);
            binding.getRoot().setClickable(true);

            this.mListBinding = binding;
            this.mGridBinding = null;
        }

        @Override
        public void onClick(View v) {
            Recipe clicked;
            if (mGridBinding == null)
                clicked = mListBinding.getRecipe();
            else
                clicked = mGridBinding.getRecipe();

            if (mOnClickListener != null)
                mOnClickListener.onRecipeClick(clicked);
        }

        void bindData(Recipe recipe) {
            if (mGridBinding == null) {
                mListBinding.setRecipe(recipe);

                mListBinding.recipeNameTextview.setText(recipe.getName());
                if (TextUtils.isEmpty(recipe.getImage()))
                    mListBinding.recipeImageview.setImageResource(R.drawable.piece_of_cake);
                else
                    Picasso.get()
                            .load(recipe.getImage())
                            .placeholder(R.drawable.ic_image)
                            .error(R.drawable.ic_broken_image)
                            .into(mListBinding.recipeImageview);
            }
            else {
                mGridBinding.setRecipe(recipe);

                mGridBinding.recipeNameTextview.setText(recipe.getName());
                if (TextUtils.isEmpty(recipe.getImage())) {
                    int color = mContext.getResources().getColor(R.color.primary_text);
                    mGridBinding.recipeNameTextview.setTextColor(color);
                    mGridBinding.recipeNameBackground.setVisibility(View.GONE);

                    mGridBinding.recipeImageview.setImageResource(R.drawable.piece_of_cake);
                }
                else {
                    int color = mContext.getResources().getColor(R.color.white);
                    mGridBinding.recipeNameTextview.setTextColor(color);
                    mGridBinding.recipeNameBackground.setVisibility(View.VISIBLE);

                    Picasso.get()
                            .load(recipe.getImage())
                            .placeholder(R.drawable.ic_image)
                            .error(R.drawable.ic_broken_image)
                            .into(mGridBinding.recipeImageview);
                }
            }
        }
    }

}
