package com.americavoice.backup.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by javier on 10/31/17.
 * Extension of the recycler view widget to add support for empty view
 */

public class RecyclerView  extends android.support.v7.widget.RecyclerView {

    /**
     * View to show on empty
     */
    private View emptyView;


    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && emptyView != null) {
                if (adapter.getItemCount() == 0) {
                    emptyView.setVisibility(VISIBLE);
                    setVisibility(GONE);
                } else {
                    emptyView.setVisibility(GONE);
                    setVisibility(VISIBLE);
                }
            }
        }
    };

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }

    public void setEmptyView(View view) {
        emptyView = view;
    }
}
