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

public class RecipesGridAdapter extends RecyclerView.Adapter<RecipeViewHolder> {

    private Context mContext;

    private List<Recipe> mList;
    private boolean listLayout;

    @Nullable
    private final RecipeClickCallback mOnClickListener;
    public interface RecipeClickCallback {
        void onRecipeClick(Recipe recipe);
    }

    public RecipesGridAdapter(Context context, @Nullable RecipeClickCallback listener) {
        this.mContext = context;
        this.mOnClickListener = listener;
        this.listLayout = true;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (listLayout) {
            ListItemRecipeBinding mBinding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.list_item_recipe,
                            parent,
                            false);

            return new RecipeViewHolder(mBinding);
        }
        else {
            GridItemRecipeBinding mBinding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.grid_item_recipe,
                            parent,
                            false);

            return new RecipeViewHolder(mBinding);
        }
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

    public void toListLayout() {
        this.listLayout = true;
    }

    public void toGridLayout() {
        this.listLayout = false;
    }

    public boolean isListLayout() { return this.listLayout; }

    public boolean isGridLayout() { return !this.listLayout; }

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
                    mListBinding.recipeImageview.setVisibility(View.GONE);
                else {
                    mListBinding.recipeImageview.setVisibility(View.VISIBLE);
                    Picasso.get()
                            .load(recipe.getImage())
                            .placeholder(R.drawable.ic_image)
                            .error(R.drawable.ic_broken_image)
                            .into(mListBinding.recipeImageview);
                }
            }
            else {
                mGridBinding.setRecipe(recipe);

                mGridBinding.recipeNameTextview.setText(recipe.getName());
                if (TextUtils.isEmpty(recipe.getImage())) {
                    int color = mContext.getResources().getColor(R.color.primary_text);
                    mGridBinding.recipeNameTextview.setTextColor(color);

                    mGridBinding.recipeImageview.setVisibility(View.GONE);
                    mGridBinding.recipeNameBackground.setVisibility(View.GONE);
                }
                else {
                    int color = mContext.getResources().getColor(R.color.white);
                    mGridBinding.recipeNameTextview.setTextColor(color);
                    mGridBinding.recipeImageview.setVisibility(View.VISIBLE);
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
