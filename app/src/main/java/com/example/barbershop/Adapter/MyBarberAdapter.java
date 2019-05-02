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
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.barbershop.Common.Common;
import com.example.barbershop.Interface.IRecyclerItemSelectedListner;
import com.example.barbershop.Model.Barber;
import com.example.barbershop.R;

import java.util.ArrayList;
import java.util.List;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.ViewHolder> {
    private Context context;
    private List<Barber> barberList;
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;

    public MyBarberAdapter(Context context, List<Barber> barberList) {
        this.context = context;
        this.barberList = barberList;
        cardViewList = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_barber,viewGroup,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.txt_barber_name.setText(barberList.get(i).getName());
        viewHolder.ratingBar.setRating((float) barberList.get(i).getRating());

        if (!cardViewList.contains(viewHolder.card_barber)){
            cardViewList.add(viewHolder.card_barber);
        }

        viewHolder.setiRecyclerItemSelectedListner(new IRecyclerItemSelectedListner() {
            @Override
            public void onItemSelectedListner(View view, int position) {
                // SET BACKGROUND FOR ALL ITEM NOT CHOSEN
                for (CardView cardView:cardViewList){
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                }

                // set background for chosen one
                viewHolder.card_barber.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                // send local broadcast to enable next button

                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_BARBER_SELECTED,barberList.get(position));
                intent.putExtra(Common.KEY_STEP,2);
                localBroadcastManager.sendBroadcast(intent);

            }
        });



    }


    @Override
    public int getItemCount() {
        return barberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_barber_name;
        RatingBar ratingBar;
        CardView card_barber;
        IRecyclerItemSelectedListner iRecyclerItemSelectedListner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_barber_name=itemView.findViewById(R.id.txt_barber_name);
            ratingBar=itemView.findViewById(R.id.rtb_barber);
            card_barber=itemView.findViewById(R.id.card_barber);
            itemView.setOnClickListener(this);

        }

        public void setiRecyclerItemSelectedListner(IRecyclerItemSelectedListner iRecyclerItemSelectedListner) {
            this.iRecyclerItemSelectedListner = iRecyclerItemSelectedListner;
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListner.onItemSelectedListner(v,getAdapterPosition());
        }
    }
}
