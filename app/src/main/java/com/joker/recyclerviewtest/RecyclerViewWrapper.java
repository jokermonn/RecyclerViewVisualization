package com.joker.recyclerviewtest;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class RecyclerViewWrapper extends RecyclerView {
  private LayoutListener listener;

  public RecyclerViewWrapper(Context context) {
    super(context);
  }

  public RecyclerViewWrapper(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public RecyclerViewWrapper(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public interface LayoutListener {
    void beforeLayout();

    void afterLayout();
  }

  public void setLayoutListener(LayoutListener listener) {
    this.listener = listener;
  }

  @SuppressWarnings("unchecked")
  public void reflectObject(RecyclerViewWrapper recyclerViewWrapper) {
    try {
      Field mRecycler =
          Class.forName("android.support.v7.widget.RecyclerView").getDeclaredField("mRecycler");
      mRecycler.setAccessible(true);
      RecyclerView.Recycler recyclerInstance =
          (RecyclerView.Recycler) mRecycler.get(recyclerViewWrapper);

      Class<?> recyclerClass = Class.forName(mRecycler.getType().getName());
      Field mAttachedScrap = recyclerClass.getDeclaredField("mAttachedScrap");
      mAttachedScrap.setAccessible(true);
      mAttachedScrap.set(recyclerInstance, new ArrayListWrapper<RecyclerView.ViewHolder>());

      ArrayList<ViewHolder> mAttached =
          (ArrayList<RecyclerView.ViewHolder>) mAttachedScrap.get(recyclerInstance);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (listener != null) {
      listener.beforeLayout();
    }
    super.onLayout(changed, l, t, r, b);
    if (listener != null) {
      listener.afterLayout();
    }
  }
}
