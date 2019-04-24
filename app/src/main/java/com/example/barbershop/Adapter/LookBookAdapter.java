package com.example.barbershop.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.barbershop.Model.Banner;
import com.example.barbershop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LookBookAdapter  extends RecyclerView.Adapter<LookBookAdapter.ViewHolder> {

    List<Banner> lookbook;
    Context context;
    public LookBookAdapter(List<Banner> lookbook, Context context) {
        this.lookbook = lookbook;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_look_book,viewGroup,false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Picasso.get().load(lookbook.get(i).getImage()).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return lookbook.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_look_book);
        }
    }
}
