package com.evp.payment.ksher.customview.gridmenu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.payment.ksher.R;
import com.evp.eos.utils.LogUtil;

import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Action;

public class GridMenuItemAdapter extends RecyclerView.Adapter<GridMenuItemAdapter.ViewHolder> {

    private static final String TAG = "GridMenuItemAdapter";

    private List<MenuItem> list;

    public GridMenuItemAdapter(List<MenuItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.layout_grid_menu_item, parent, false));
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
        @BindView(R.id.tv_bg_item)
        TextView tvBgItem;
        @BindView(R.id.iv_item)
        ImageView ivItem;
        @BindView(R.id.tv_item)
        TextView tvItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(MenuItem data) {
            if (!data.blankFlag) {
                tvBgItem.setEnabled(true);
                ivItem.setImageBitmap(data.iconResId);
                ivItem.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivItem.setVisibility(View.VISIBLE);
                tvItem.setText(data.text);
                tvItem.setVisibility(View.VISIBLE);

                if( data.text.length()>0){
                    tvItem.setVisibility(View.VISIBLE);
                }else{
                    tvItem.setVisibility(View.GONE);
                }

                tvBgItem.setOnClickListener(v -> process(v.getContext(), data));
            } else {
                tvBgItem.setEnabled(false);
                ivItem.setVisibility(View.GONE);
                tvItem.setVisibility(View.GONE);
            }


        }
    }

    private void process(Context context, MenuItem item) {
//        try {
//            // Check support
//            if (item.callableCheckSupport != null && !item.callableCheckSupport.call()) {
//                DeviceUtil.INSTANCE.beepErr();
//                DialogUtils.showAlertTimeout(context.getString(R.string.is_not_support_xliff, item.text), DialogUtils.TIMEOUT_FAIL)
//                        .subscribe();
//                return;
//            }
//        } catch (Exception e) {
//            LogUtil.e(TAG, e);
//        }
//
//        AbsTransaction transaction = item.transaction;
//        if (transaction != null) {
//            transaction.execute();
//            return;
//        }
//
        Action rxAction = item.rxAction;
        if (rxAction != null) {
            try {
                rxAction.run();
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }
            return;
        }
//
        Class<?> clazz = item.activity;
        if (clazz != null) {
            context.startActivity(new Intent(context, clazz));
            return;
        }

        Intent intent = item.intent;
        if (intent != null) {
            context.startActivity(intent);
        }

    }

    public static class MenuItem {
        private Bitmap iconResId;
        private String text;
        private boolean blankFlag;

//        private AbsTransaction transaction;
        private Class<?> activity;
        private Action rxAction;
        private Intent intent;
        private Callable<Boolean> callableCheckSupport;

        public MenuItem(Bitmap iconResId, String text) {
            this.iconResId = iconResId;
            this.text = text;
        }

        public MenuItem(boolean blankFlag) {
            this.blankFlag = blankFlag;
        }

//        public void setTransaction(AbsTransaction transaction) {
//            this.transaction = transaction;
//        }

        public void setActivity(Class<?> activity) {
            this.activity = activity;
        }

        public void setRxAction(Action rxAction) {
            this.rxAction = rxAction;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public void setIconResId(Bitmap iconResId) {
            this.iconResId = iconResId;
        }

        public void setCallableCheckSupport(Callable<Boolean> callableCheckSupport) {
            this.callableCheckSupport = callableCheckSupport;
        }
    }

}
