package com.joker.recyclerviewtest;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Item2 extends LinearLayout {
  private TextView tv;

  public Item2(Context context) {
    this(context, null);
  }

  public Item2(Context context,
      @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public Item2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    inflate(context, R.layout.item_2_origin, this);
    tv = findViewById(R.id.tv_content);
  }

  @Override public String toString() {
    return "Item2---" + tv.getText();
  }
}
