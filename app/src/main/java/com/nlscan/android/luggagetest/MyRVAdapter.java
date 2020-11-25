package com.nlscan.android.luggagetest;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyTVHolder> {

    private static final String TAG = "LuggageService";
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
//    private  List<PersonInfo> mData;
    private List<Map<String,String>> mData;

    public MyRVAdapter(Context context, List<Map<String,String>> dataList) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;

        mData = dataList;
    }

    @Override
    public MyRVAdapter.MyTVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyRVAdapter.MyTVHolder(mLayoutInflater.inflate(R.layout.text_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyRVAdapter.MyTVHolder holder, int pos) {
        holder.view.setBackgroundColor(pos % 2 == 0 ?mContext.getResources().getColor(R.color.gray):
                                                     mContext.getResources().getColor(R.color.white));


        Log.d(TAG,"the bind view pos " + pos);

        holder.mIndexView.setText(""+ (pos+1));

        Map<String,String> map = mData.get(pos);

        holder.mEpcView.setText(map.get(Constants.RV_HEAD_EPC));

        String boxState = map.get(Constants.RV_HEAD_BOX_STATE);
        if ("放置正确拖车".equals(boxState)){
            holder.mStateView.setTextColor(Color.GREEN);
        }
        else if("放置错误拖车".equals(boxState)) {
            holder.mStateView.setTextColor(Color.RED);
        }
        else if("非法搬运".equals(boxState)) {
            holder.mStateView.setTextColor(Color.RED);
        }
        else if("携带至正确拖车".equals(boxState)) {
            holder.mStateView.setTextColor(Color.BLUE);
        }
        else if("携带至错误拖车".equals(boxState)) {
            holder.mStateView.setTextColor(mContext.getColor(R.color.orange));
        }
        else {
            holder.mStateView.setTextColor(Color.BLACK);
        }

        holder.mStateView.setText(boxState);

        holder.mAroundView.setText(map.get(Constants.RV_HEAD_CAR));



    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class MyTVHolder extends RecyclerView.ViewHolder {
        TextView mIndexView;
        BorderTextView mEpcView;
        BorderTextView mStateView;
        BorderTextView mAroundView;
        View view;

        MyTVHolder(View itemView) {
            super(itemView);
            mIndexView = (TextView) itemView.findViewById(R.id.tv_item_index);
            mEpcView = (BorderTextView) itemView.findViewById(R.id.tv_item_epc);
            mStateView = (BorderTextView) itemView.findViewById(R.id.tv_item_state);
            mAroundView = (BorderTextView) itemView.findViewById(R.id.tv_item_around);
            view = itemView;
        }
    }
}