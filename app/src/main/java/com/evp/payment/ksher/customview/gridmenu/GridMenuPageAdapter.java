package com.evp.payment.ksher.customview.gridmenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.evp.payment.ksher.R;
import com.evp.payment.ksher.customview.SpaceItemDecoration;
import com.evp.payment.ksher.utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GridMenuPageAdapter extends RecyclerView.Adapter<GridMenuPageAdapter.ViewHolder> {

    private List<MenuPage> list;
    private int space;
    private int spanCount;

    public GridMenuPageAdapter(List<MenuPage> list, int space, int spanCount) {
        this.list = list;
        this.space = space;
        this.spanCount = spanCount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.layout_grid_menu_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rv_container)
        RecyclerView rvContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            rvContainer.setLayoutManager(new GridLayoutManager(itemView.getContext(), spanCount));
            rvContainer.addItemDecoration(new SpaceItemDecoration(Utils.INSTANCE.dp2px(itemView.getContext(), space), spanCount));
        }

        void bindData(MenuPage data) {
            rvContainer.setAdapter(new GridMenuItemAdapter(data.items));
        }
    }

    public static class MenuPage {
        private List<GridMenuItemAdapter.MenuItem> items;

        public MenuPage(List<GridMenuItemAdapter.MenuItem> items) {
            this.items = items;
        }
    }

}
