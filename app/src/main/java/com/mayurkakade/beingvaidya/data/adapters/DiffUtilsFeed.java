package com.mayurkakade.beingvaidya.data.adapters;

import androidx.recyclerview.widget.DiffUtil;

import com.mayurkakade.beingvaidya.data.models.FeedModel;

import java.util.List;

public class DiffUtilsFeed extends  DiffUtil.Callback{
    private List<FeedModel> oldList;
    private List<FeedModel> newList;

    public DiffUtilsFeed(List<FeedModel> oldList, List<FeedModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition) == newList.get(newItemPosition);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).DocId.equals(newList.get(newItemPosition).DocId);
    }
}
