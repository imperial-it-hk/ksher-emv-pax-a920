package com.evp.payment.ksher.customview.gridmenu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager2.widget.ViewPager2;


import com.evp.payment.ksher.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Action;

public class GridMenu extends LinearLayout {
    private List<GridMenuItemAdapter.MenuItem> menuList;
    private List<GridMenuPageAdapter.MenuPage> pageList = new ArrayList<>();
    private int space;
    private int columns;
    private int pageItemSize;
    private ViewPager2 vp2Container;
    private LinearLayout layoutPageIndicator;

    public GridMenu(Context context, List<GridMenuItemAdapter.MenuItem> menuList, int space, int rows, int columns) {
        super(context);
        this.menuList = menuList;
        this.space = space;
        this.columns = columns;
        this.pageItemSize = rows * columns;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_grid_menu_container, this, true);
        initViewPager(view);
        initPageIndicator(view);
        vp2Container.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePageIndicator(position);
            }
        });
    }

    private void initViewPager(View view) {
        vp2Container = view.findViewById(R.id.vp2_container);
        int itemCount = (menuList.size() + pageItemSize - 1) / pageItemSize * pageItemSize;
        int menuSize = menuList.size();
        List<GridMenuItemAdapter.MenuItem> itemList = null;
        for (int i = 0; i < itemCount; i++) {
            if (i % pageItemSize == 0) {
                itemList = new ArrayList<>();
                pageList.add(new GridMenuPageAdapter.MenuPage(itemList));
            }
            if (i < menuSize) {
                itemList.add(menuList.get(i));
            } else {
                itemList.add(new GridMenuItemAdapter.MenuItem(true));
            }
        }
        vp2Container.setAdapter(new GridMenuPageAdapter(pageList, space, columns));
        // Preload next page
        vp2Container.setOffscreenPageLimit(1);
    }

    private void initPageIndicator(View view) {
        layoutPageIndicator = view.findViewById(R.id.layout_indicator);
        int pageSize = pageList.size();
        for (int i = 0; i < pageSize; i++) {
            ImageView image = new ImageView(view.getContext());
            LayoutParams params = new LayoutParams(20, 20);
            params.setMargins(10, 0, 10, 0);
            image.setImageResource(R.drawable.shape_guide_dot_normal);
            layoutPageIndicator.addView(image, params);
        }
    }

    private void updatePageIndicator(int position) {
        int size = layoutPageIndicator.getChildCount();
        for (int i = 0; i < size; i++) {
            View view = layoutPageIndicator.getChildAt(i);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(i == position ? R.drawable.shape_guide_dot_select : R.drawable.shape_guide_dot_normal);
            }
        }
    }

    public static class Builder {

        private Context context;
        private int space = 8;
        private int rows = 3;
        private int columns = 3;
        private List<GridMenuItemAdapter.MenuItem> itemList = new ArrayList<>();

        public Builder(Context context) {
            this.context = context;
        }

        public Builder(Context context, int space, int rows, int columns) {
            this.context = context;
            this.space = space;
            this.rows = rows;
            this.columns = columns;
        }
        public Builder addTransaction(String title, Bitmap icon) {
            GridMenuItemAdapter.MenuItem item = new GridMenuItemAdapter.MenuItem(icon, title);
            itemList.add(item);
            return this;
        }

//        public Builder addTransaction(String title, int icon, AbsTransaction transaction) {
//            GridMenuItemAdapter.MenuItem item = new GridMenuItemAdapter.MenuItem(icon, title);
//            item.setTransaction(transaction);
//            itemList.add(item);
//            return this;
//        }

//        public Builder addTransaction(String title, int icon, AbsTransaction transaction, Callable<Boolean> callableCheckSupport) {
//            GridMenuItemAdapter.MenuItem item = new GridMenuItemAdapter.MenuItem(icon, title);
//            item.setTransaction(transaction);
//            item.setCallableCheckSupport(callableCheckSupport);
//            itemList.add(item);
//            return this;
//        }

        public Builder addAction(Boolean isDisplay, String title, Bitmap icon, Action action) {
            if(isDisplay) {
                GridMenuItemAdapter.MenuItem item = new GridMenuItemAdapter.MenuItem(icon, title);
                item.setRxAction(action);
                itemList.add(item);
            }
            return this;
        }

        public Builder addActivity(String title, Bitmap icon, Class<?> activityClass) {
            GridMenuItemAdapter.MenuItem item = new GridMenuItemAdapter.MenuItem(icon, title);
            item.setActivity(activityClass);
            itemList.add(item);
            return this;
        }

        public Builder addIntent(String title, Bitmap icon, Intent intent) {
            GridMenuItemAdapter.MenuItem item = new GridMenuItemAdapter.MenuItem(icon, title);
            item.setIntent(intent);
            itemList.add(item);
            return this;
        }

        public GridMenu create() {
            return new GridMenu(context, itemList, space, rows, columns);
        }
    }
}
