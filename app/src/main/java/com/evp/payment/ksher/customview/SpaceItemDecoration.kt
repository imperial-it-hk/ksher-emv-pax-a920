package com.evp.payment.ksher.customview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpaceItemDecoration(private val space: Int, private val spanCount: Int) : ItemDecoration() {
    private val halfSpace: Int = space / 2
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = halfSpace
        outRect.right = halfSpace
        outRect.bottom = space
        if (parent.getChildLayoutPosition(view) % spanCount == 0) {
            outRect.left = space
        }
        if ((parent.getChildLayoutPosition(view) + 1) % spanCount == 0) {
            outRect.right = space
        }
    }

}