package com.chupchia.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chupchia.R;
import com.chupchia.adapters.NotificationAdapter;
import com.chupchia.models.Notification;

public class NotificationSwipeHelper extends ItemTouchHelper.SimpleCallback {

    private Context context;
    private NotificationAdapter adapter;
    private NotificationAdapter.OnNotificationDeleteListener deleteListener;
    private Drawable deleteIcon;
    private Paint paint;

    public NotificationSwipeHelper(Context context, NotificationAdapter adapter,
                                   NotificationAdapter.OnNotificationDeleteListener deleteListener) {
        super(0, ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
        this.deleteListener = deleteListener;
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        this.paint = new Paint();
        paint.setColor(Color.RED);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Notification notification = adapter.getNotifications().get(position);

        if (deleteListener != null) {
            deleteListener.onNotificationDelete(notification, position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        float alpha = Math.abs(dX) / itemView.getWidth();
        
        // Vẽ nền đỏ
        paint.setAlpha((int) (255 * alpha));
        c.drawRect(itemView.getLeft(), itemView.getTop(),
                   itemView.getLeft() + dX, itemView.getBottom(), paint);

        // Vẽ biểu tượng xóa
        if (deleteIcon != null) {
            int iconHeight = deleteIcon.getIntrinsicHeight();
            int iconWidth = deleteIcon.getIntrinsicWidth();
            int iconMargin = (itemView.getHeight() - iconHeight) / 2;
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = iconLeft + iconWidth;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + iconHeight;

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.setAlpha((int) (255 * alpha));
            deleteIcon.draw(c);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.3f;
    }
}
