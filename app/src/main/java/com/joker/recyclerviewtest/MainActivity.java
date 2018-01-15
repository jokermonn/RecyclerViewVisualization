package com.joker.recyclerviewtest;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
  private List<Character> data = new ArrayList<>(32);
  private TextView mBindInfo;
  private ScrollView mSV;
  private Adapter mAdapter;
  private RecyclerViewWrapper mRv;
  private TextView mTv;
  private int refreshCount = 0;

  {
    for (int i = 1; i < 33; i++) {
      data.add((char) (i + 64));
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.tb_title);
    // 如果你要试试刷新机制的话
    // toolbar.setVisibility(View.VISIBLE);
    setSupportActionBar(toolbar);
    mRv = findViewById(R.id.rv_content);
    mTv = findViewById(R.id.tv_info);
    mBindInfo = findViewById(R.id.tv_bind_info);
    mSV = findViewById(R.id.sv_content);
    findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mBindInfo.setText("");
      }
    });
    mAdapter = new Adapter();
    mRv.setAdapter(mAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
    mRv.setLayoutListener(new RecyclerViewWrapper.LayoutListener() {
      @SuppressWarnings("unchecked") @Override public void beforeLayout() {
        try {
          Field mRecycler =
              Class.forName("android.support.v7.widget.RecyclerView").getDeclaredField("mRecycler");
          mRecycler.setAccessible(true);
          RecyclerView.Recycler recyclerInstance =
              (RecyclerView.Recycler) mRecycler.get(mRv);

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
        // 第二种方式：添加在这里
        //rv.getRecycledViewPool().setMaxRecycledViews(0, 8);
        //for (int i = 0; i < 8; i++) {
        //  Adapter.ViewHolder viewHolder = adapter.createViewHolder(rv, 0);
        //  rv.getRecycledViewPool().putRecycledView(viewHolder);
        //}
        showMessage(mRv, mTv, layoutManager);
      }
    });
    mRv.addItemDecoration(new ItemDecoration());
    mRv.setLayoutManager(layoutManager);
    mRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        showMessage();
      }
    });
    // 第一种方式：添加在这里
    //rv.getRecycledViewPool().setMaxRecycledViews(0, 8);
    //for (int i = 0; i < 8; i++) {
    //  Adapter.ViewHolder viewHolder = adapter.createViewHolder(rv, 0);
    //  rv.getRecycledViewPool().putRecycledView(viewHolder);
    //}
  }

  private void showMessage() {
    showMessage(mRv, mTv, (LinearLayoutManager) mRv.getLayoutManager());
    mSV.fullScroll(ScrollView.FOCUS_DOWN);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar_menu, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    data.add(1, (char) ((refreshCount++) + 33));
    switch (item.getItemId()) {
      case R.id.part_refresh:
        mAdapter.notifyItemInserted(1);
        return true;
      case R.id.all_refresh:
        mAdapter.notifyDataSetChanged();
        return true;
    }
    return false;
  }

  @SuppressLint("SetTextI18n") @SuppressWarnings("unchecked")
  private void showMessage(RecyclerViewWrapper rv, TextView tv,
      LinearLayoutManager layoutManager) {
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
              + getMCacheViewTargetPositionInfo(rv, layoutManager)
              + getMCachedViewsInfo(mCached)
              + getRVPoolInfo(recyclerPoolClass, recycledViewPool)
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getMCacheViewTargetPositionInfo(RecyclerView recycledView,
      LinearLayoutManager layoutManager) {
    int lastItemPosition = layoutManager.findLastVisibleItemPosition();
    int firstItemPosition = layoutManager.findFirstVisibleItemPosition();

    try {
      Field mAdapterHelper = Class.forName("android.support.v7.widget.RecyclerView")
          .getDeclaredField("mAdapterHelper");
      mAdapterHelper.setAccessible(true);
      Object o = mAdapterHelper.get(recycledView);

      Field mLayoutStateField = layoutManager.getClass().getDeclaredField("mLayoutState");
      mLayoutStateField.setAccessible(true);
      Class<?> layoutStateClass =
          Class.forName("android.support.v7.widget.LinearLayoutManager$LayoutState");
      Field mCurrentPositionField = layoutStateClass.getDeclaredField("mCurrentPosition");
      mCurrentPositionField.setAccessible(true);

      return "target mCachedView position:" + mCurrentPositionField.get(
          mLayoutStateField.get(recycledView.getLayoutManager())) + "\n";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  @SuppressLint("SetTextI18n") public void appendText(final String append) {
    mBindInfo.post(new Runnable() {
      @Override public void run() {
        mBindInfo.setText(mBindInfo.getText() + append);
        mSV.fullScroll(ScrollView.FOCUS_DOWN);
      }
    });
  }

  @SuppressWarnings("unchecked")
  private String getRVPoolInfo(Class<?> aClass, RecyclerView.RecycledViewPool recycledViewPool) {
    try {
      Field mScrapField = aClass.getDeclaredField("mScrap");
      mScrapField.setAccessible(true);
      SparseArray mScrap = (SparseArray) mScrapField.get(recycledViewPool);

      Class<?> scrapDataClass =
          Class.forName("android.support.v7.widget.RecyclerView$RecycledViewPool$ScrapData");
      Field mScrapHeapField = scrapDataClass.getDeclaredField("mScrapHeap");
      Field mMaxScrapField = scrapDataClass.getDeclaredField("mMaxScrap");
      mScrapHeapField.setAccessible(true);
      mMaxScrapField.setAccessible(true);
      String s = "mRecyclerPool（四缓） info:\n";
      for (int i = 0; i < mScrap.size(); i++) {
        ArrayList<RecyclerView.ViewHolder> item =
            (ArrayList<RecyclerView.ViewHolder>) mScrapHeapField.get(mScrap.get(i));
        for (int j = 0; j < item.size(); j++) {
          if (j == item.size() - 1) {
            s += ">>> ";
          } else if (j == 0) {
            s += "mScrap[" + i + "] max size is:" + (mMaxScrapField.get(mScrap.get(i))) + "\n";
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
    return s
        + "\n";
  }

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

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      appendText(viewType == TYPE_1 ? ""
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
      String targetText = data.get(position) + "(layoutPosition:" + position + ")";
      appendText("\n『" + targetText + "』 BindViewHolder");
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
