package com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// implementation(libs.recyclerview.v121)
// implementation("androidx.recyclerview:recyclerview:1.2.1")

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.GenericViewHolder> {

    private List<T>                     items;
    private int                         layoutId;
    private BindViewHolder<T>           bindViewHolder;
    private InitializeViewHolder        initializeViewHolder;

    //region Click & LongClick Listeners
    private OnItemClickListener<T>      clickListener;
    private OnItemLongClickListener<T>  longClickListener;

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.longClickListener = listener;
    }
    //endregion

    //region Swipe Rigth & Letf Listeners
    private OnItemSwipeListener<T> swipeListener;

    public void setOnItemSwipeListener(OnItemSwipeListener<T> listener) {
        this.swipeListener = listener;
    }

    public OnItemSwipeListener<T> getSwipeListener() {
        return swipeListener;
    }

    public interface OnItemSwipeListener<T> {
        void onItemSwipeRight(T item, int position);
        void onItemSwipeLeft(T item, int position);
    }//endregion


    public GenericAdapter(List<T> items, int layoutId, InitializeViewHolder initializeViewHolder, BindViewHolder<T> bindViewHolder) {
        this.items                  = items;
        this.layoutId               = layoutId;
        this.initializeViewHolder   = initializeViewHolder;
        this.bindViewHolder         = bindViewHolder;
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        GenericViewHolder holder = new GenericViewHolder(view);
        initializeViewHolder.onInitialize(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        T item = items.get(position);
        bindViewHolder.onBind(holder, item, position);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(item, position);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }

    public void setItems(List<T> items){
        this.items = items;
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return items;
    }

    public T getItem(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    public static class GenericViewHolder extends RecyclerView.ViewHolder {
        private Map<String, View> views;

        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);
            views = new HashMap<>();
        }

        public void putView(String key, View view) {
            views.put(key, view);
        }

        public <V extends View> V getView(String key) {
            return (V) views.get(key);
        }
    }

    public interface InitializeViewHolder {
        void onInitialize(GenericViewHolder holder);
    }

    public interface BindViewHolder<T> {
        void onBind(GenericViewHolder holder, T item, int position);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item, int position);
    }

    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(T item, int position);
    }

    //region USAGE (in Activity)
    /*
    adapter = new YourAdapter(null, R.layout.single_member_layout,
                                 // Initialize ViewHolder - this runs only once per ViewHolder
    holder -> {
        holder.putView("txtName", holder.itemView.findViewById(R.id.tvName));
    },
    ((holder, item, position) -> {
        // Bind data to ViewHolder - this runs for every item
        ((TextView)holder.getView("txtName")).setText(item.getName());
    })
    );
    */

    //region LISTENERS
    /*
   adapter.setOnItemClickListener(new GenericAdapter.OnItemClickListener<YourItem>() {
        @Override
        public void onItemClick(YourItem item, int position) {

        }
    });

    adapter.setOnItemLongClickListener(new GenericAdapter.OnItemLongClickListener<YourItem>() {
        @Override
        public boolean onItemLongClick(YourItem item, int position) {

            return false;
        }
    });
     */
    //endregion

    //region SWIPE
    /*
        adapter.setOnItemSwipeListener(new GenericAdapter.OnItemSwipeListener<Member>() {
        @Override
        public void onItemSwipeRight(YourItem item, int position) {
            // Any action when swipe right

            // Perform any action you want for right swipe
            adapter.notifyItemChanged(position);  // This will redraw the item
        }

        @Override
        public void onItemSwipeLeft(YourItem item, int position) {
            // Delete when swipe left

            // Don't remove the item unless you want to delete it
            new MaterialAlertDialogBuilder(YourActivity.this)
                    .setMessage("Delete " + item.getName())
                    .setIcon(R.drawable.trashcan)
                    .setCancelable(true)
                    .setTitle("Delete")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyItemChanged(position);  // This will redraw the item
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            viewModel.delete(item, "picture", "pictureUrl");
                            adapter.getItems().remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                    })
                    .show();
        }
    });

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeCallback<YourItem>(adapter, this));
        itemTouchHelper.attachToRecyclerView(RecyclerView שם ה-);
        */
    //endregion
    //endregion
}