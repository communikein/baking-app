package it.communikein.bakingapp;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.communikein.bakingapp.StepsListAdapter.StepViewHolder;
import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.databinding.ListItemStepBinding;

public class StepsListAdapter extends RecyclerView.Adapter<StepViewHolder> {

    private List<Step> mList;

    @Nullable
    private final StepClickCallback mOnClickListener;
    public interface StepClickCallback {
        void onStepClick(Step step);
    }

    public StepsListAdapter(@Nullable StepClickCallback listener) {
        this.mOnClickListener = listener;
    }

    @Override @NonNull
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemStepBinding mBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_item_step,
                        parent,
                        false);

        return new StepViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = mList.get(position);

        holder.bindData(step);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public void setList(final List<Step> newList) {
        final List<Step> tempList = new ArrayList<>();
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
                    Step newItem = tempList.get(newItemPosition);
                    Step oldItem = mList.get(oldItemPosition);
                    return oldItem.displayEquals(newItem);
                }
            });
            mList = tempList;
            result.dispatchUpdatesTo(this);
        }
    }

    class StepViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ListItemStepBinding mBinding;

        StepViewHolder(ListItemStepBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setFocusable(true);
            binding.getRoot().setClickable(true);

            this.mBinding = binding;
        }

        @Override
        public void onClick(View v) {
            Step clicked = mBinding.getStep();

            if (mOnClickListener != null)
                mOnClickListener.onStepClick(clicked);
        }

        void bindData(Step step) {
            mBinding.setStep(step);

            if (step.getStepNum() == 0) {
                mBinding.upperLine.setVisibility(View.GONE);
                mBinding.lowerLine.setVisibility(View.VISIBLE);
            }
            else if(step.getStepNum() == getItemCount() - 1) {
                mBinding.upperLine.setVisibility(View.VISIBLE);
                mBinding.lowerLine.setVisibility(View.GONE);
            }
            else {
                mBinding.upperLine.setVisibility(View.VISIBLE);
                mBinding.lowerLine.setVisibility(View.VISIBLE);
            }

            mBinding.stepNumberTextview.setText(String.valueOf(step.getStepNum() + 1));
            mBinding.stepShortDescriptionTextview.setText(step.getShortDescription());
            mBinding.stepFullDescriptionTextview.setText(step.getDescription());
        }
    }

}
