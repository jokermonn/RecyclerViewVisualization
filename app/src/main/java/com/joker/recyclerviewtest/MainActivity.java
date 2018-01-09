package com.joker.recyclerviewtest;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by @author jokermonn
 */
public class MainActivity extends AppCompatActivity {
  private List<String> data = new ArrayList<>(32);
  private TextView mBindInfo;
  private ScrollView mSV;

  {
    for (int i = 1; i < 33; i++) {
      data.add(String.valueOf(i));
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final RecyclerViewWrapper rv = findViewById(R.id.rv_content);
    final TextView tv = findViewById(R.id.tv_info);
    mBindInfo = findViewById(R.id.tv_bind_info);
    mSV = findViewById(R.id.sv_content);
    findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mBindInfo.setText("");
      }
    });
    rv.setAdapter(new Adapter());
    rv.setLayoutListener(new RecyclerViewWrapper.LayoutListener() {
      @SuppressWarnings("unchecked") @Override public void beforeLayout() {
        try {
          Field mRecycler =
              Class.forName("android.support.v7.widget.RecyclerView").getDeclaredField("mRecycler");
          mRecycler.setAccessible(true);
          RecyclerView.Recycler recyclerInstance =
              (RecyclerView.Recycler) mRecycler.get(rv);

          Class<?> recyclerClass = Class.forName(mRecycler.getType().getName());
          Field mAttachedScrap = recyclerClass.getDeclaredField("mAttachedScrap");
          mAttachedScrap.setAccessible(true);
          mAttachedScrap.set(recyclerInstance, new ArrayListWrapper<RecyclerView.ViewHolder>());

          ArrayList<RecyclerView.ViewHolder> mAttached =
              (ArrayList<RecyclerView.ViewHolder>) mAttachedScrap.get(recyclerInstance);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void afterLayout() {
        showMessage(rv, tv);
      }
    });
    rv.addItemDecoration(new ItemDecoration());
    rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        mSV.fullScroll(ScrollView.FOCUS_DOWN);
        showMessage(rv, tv);
      }
    });
  }

  @SuppressLint("SetTextI18n") @SuppressWarnings("unchecked")
  private void showMessage(RecyclerViewWrapper rv, TextView tv) {
    try {
      Field mRecycler =
          Class.forName("android.support.v7.widget.RecyclerView").getDeclaredField("mRecycler");
      mRecycler.setAccessible(true);
      RecyclerView.Recycler recyclerInstance = (RecyclerView.Recycler) mRecycler.get(rv);

      Class<?> recyclerClass = Class.forName(mRecycler.getType().getName());
      Field mViewCacheMax = recyclerClass.getDeclaredField("mViewCacheMax");
      Field mAttachedScrap = recyclerClass.getDeclaredField("mAttachedScrap");
      Field mChangedScrap = recyclerClass.getDeclaredField("mChangedScrap");
      Field mCachedViews = recyclerClass.getDeclaredField("mCachedViews");
      Field mRecyclerPool = recyclerClass.getDeclaredField("mRecyclerPool");
      mViewCacheMax.setAccessible(true);
      mAttachedScrap.setAccessible(true);
      mChangedScrap.setAccessible(true);
      mCachedViews.setAccessible(true);
      mRecyclerPool.setAccessible(true);

      int mViewCacheSize = (int) mViewCacheMax.get(recyclerInstance);
      ArrayListWrapper<RecyclerView.ViewHolder> mAttached =
          (ArrayListWrapper<RecyclerView.ViewHolder>) mAttachedScrap.get(recyclerInstance);
      ArrayList<RecyclerView.ViewHolder> mChanged =
          (ArrayList<RecyclerView.ViewHolder>) mChangedScrap.get(recyclerInstance);
      ArrayList<RecyclerView.ViewHolder> mCached =
          (ArrayList<RecyclerView.ViewHolder>) mCachedViews.get(recyclerInstance);
      RecyclerView.RecycledViewPool recycledViewPool =
          (RecyclerView.RecycledViewPool) mRecyclerPool.get(recyclerInstance);

      Class<?> recyclerPoolClass = Class.forName(mRecyclerPool.getType().getName());
      tv.setText(
          "mAttachedScrap（一缓） size is:"
              + mAttached.maxSize
              + "\n"
              + "mCachedViews（二缓） max size is:"
              + mViewCacheSize
              + "\n"
              + getMChangedScrapViewsInfo(mChanged)
              + getMCachedViewsInfo(mCached)
              + getRVPoolInfo(recyclerPoolClass, recycledViewPool)
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @SuppressLint("SetTextI18n") public void setText(String append) {
    mBindInfo.setText(mBindInfo.getText() + append);
  }

  @NonNull @SuppressWarnings("unchecked")
  private String getRVPoolInfo(Class<?> aClass, RecyclerView.RecycledViewPool recycledViewPool) {
    try {
      Field mScrapField = aClass.getDeclaredField("mScrap");
      mScrapField.setAccessible(true);
      SparseArray mScrap = (SparseArray) mScrapField.get(recycledViewPool);

      Class<?> scrapDataClass =
          Class.forName("android.support.v7.widget.RecyclerView$RecycledViewPool$ScrapData");
      Field mScrapHeapField = scrapDataClass.getDeclaredField("mScrapHeap");
      mScrapHeapField.setAccessible(true);
      String s = "mRecyclerPool（四缓） info:\n";
      for (int i = 0; i < mScrap.size(); i++) {
        ArrayList<RecyclerView.ViewHolder> item =
            (ArrayList<RecyclerView.ViewHolder>) mScrapHeapField.get(mScrap.get(i));
        for (int j = 0; j < item.size(); j++) {
          if (j == item.size() - 1) {
            s += ">>> ";
          }
          s += "mScrap[" + i + "] 中的 mScrapHeap[" + j + "] info is:" + item.get(j) + "\n";
        }
        s += "\n";
      }
      return s;
    } catch (Exception e) {
      e.printStackTrace();
      return "\n";
    }
  }

  private String getMCachedViewsInfo(ArrayList<RecyclerView.ViewHolder> viewHolders) {
    String s = "mCachedViews（二缓） info:\n";
    if (viewHolders.size() > 0) {
      int i = 0;
      for (; i < viewHolders.size(); i++) {
        if (i == viewHolders.size() - 1) {
          s += ">>> ";
        }
        s += "mCachedViews[" + i + "] is " + viewHolders.get(i).toString() + "\n";
      }

      // append \n
      if (i == 0) {
        s += "\n\n\n";
      } else if (i == 1) {
        s += "\n\n";
      } else if (i == 2) {
        s += "\n";
      }
    } else {
      s += "\n\n\n";
    }
    return "mCachedViews（二缓）current size is:"
        + viewHolders.size()
        + "\n"
        + s
        + "\n";
  }

  @NonNull
  private String getMChangedScrapViewsInfo(ArrayList<RecyclerView.ViewHolder> viewHolders) {
    if (viewHolders != null) {
      String s = "";
      for (int i = 0; i < viewHolders.size(); i++) {
        if (i == 0) {
          s += "mChangedScrap info:\n";
        }
        s += "mChangedScrap[" + i + "] is " + viewHolders.get(i).toString();
      }
      return "mChangedScrap size is: "
          + viewHolders.size()
          + "\n"
          + s;
    }
    return "";
  }

  class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    final int TYPE_1 = 0;
    final int TYPE_2 = 1;

    @Override public void setHasStableIds(boolean hasStableIds) {
      super.setHasStableIds(hasStableIds);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      setText(viewType == TYPE_1 ? ""
          + "\nItem1 createViewHolder" : "\nItem2 createViewHolder");
      return new ViewHolder(
          LayoutInflater.from(MainActivity.this)
              .inflate(
                  viewType == TYPE_1 ? R.layout.item_1 : R.layout.item_2,
                  parent,
                  false
              )
      );
    }

    @SuppressLint("SetTextI18n") @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      String targetText = data.get(position);
      setText("\n『" + targetText + "』 BindViewHolder");
      holder.tv.setText(targetText);
    }

    @Override public int getItemCount() {
      return data.size();
    }

    @Override public int getItemViewType(int position) {
      return isFirstViewType(position) ? TYPE_1 : TYPE_2;
    }

    private boolean isFirstViewType(int position) {
      return position < 17;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
      TextView tv;

      ViewHolder(View itemView) {
        super(itemView);
        tv = itemView.findViewById(R.id.tv_content);
      }

      @Override public String toString() {
        return itemView.toString();
      }
    }
  }

  class ItemDecoration extends RecyclerView.ItemDecoration {
    ItemDecoration() {
    }

    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
        RecyclerView.State state) {
      outRect.set(0, 0, 0, 2);
    }

    @Override public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
      super.onDraw(c, parent, state);
      c.drawColor(0xFFEEEEEE);
    }
  }
}
