package com.example.barbershop.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.barbershop.Common.Common;
import com.example.barbershop.Interface.IRecyclerItemSelectedListner;
import com.example.barbershop.Model.TimeSlot;
import com.example.barbershop.R;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.ViewHolder> {

    private Context context;
    private List<TimeSlot> timeSlotList;
    private List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_time_slot, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());

        if (timeSlotList.size() == 0)// if all position is available just show list
        {
            viewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            viewHolder.txt_time_slot_description.setText("Available");
            viewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
            viewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));

        } else //if position is full (booked)
        {
            for (TimeSlot slotValue : timeSlotList) {
                // loop all time slot from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == i) {

                    viewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                    viewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                    viewHolder.txt_time_slot_description.setText("Full");
                    viewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                    viewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));

                }
            }
        }

        //ADD ALL CARD TO LIST
        if (!cardViewList.contains(viewHolder.card_time_slot)){
            cardViewList.add(viewHolder.card_time_slot);
        }

        //CHECK IF CARD TIME SLOT IS AVAILABLE
        if (!timeSlotList.contains(i)){
            viewHolder.setiRecyclerItemSelectedListner(new IRecyclerItemSelectedListner() {
                @Override
                public void onItemSelectedListner(View view, int position) {
                    for (CardView cardView:cardViewList){
                        if (cardView.getTag() == null) // only available card time slot be changed
                        {
                            cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                        }
                    }

                    //our selected card view will change
                    viewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                    //after that send broadcast to enable next button

                    Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                    intent.putExtra(Common.KEY_TIME_SLOT,i);
                    intent.putExtra(Common.KEY_STEP,3);
                    localBroadcastManager.sendBroadcast(intent);




                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        public void setiRecyclerItemSelectedListner(IRecyclerItemSelectedListner iRecyclerItemSelectedListner) {
            this.iRecyclerItemSelectedListner = iRecyclerItemSelectedListner;
        }

        IRecyclerItemSelectedListner iRecyclerItemSelectedListner;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = itemView.findViewById(R.id.txt_time_slot_description);
            card_time_slot = itemView.findViewById(R.id.card_time_slot);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListner.onItemSelectedListner(v,getAdapterPosition());
        }
    }
}
