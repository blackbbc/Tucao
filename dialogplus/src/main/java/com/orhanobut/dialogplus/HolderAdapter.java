package com.orhanobut.dialogplus;

import android.widget.BaseAdapter;

import com.orhanobut.dialogplus.listener.OnHolderListener;

public interface HolderAdapter extends Holder {

  void setAdapter(BaseAdapter adapter);

  void setOnItemClickListener(OnHolderListener listener);
}
