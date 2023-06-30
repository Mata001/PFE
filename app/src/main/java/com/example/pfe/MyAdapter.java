package com.example.pfe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<StationItem> stationItems;

    public MyAdapter(Context context, ArrayList<StationItem> stationItems) {
        this.context = context;
        this.stationItems = stationItems;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.station_layout_dg, parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        StationItem stationItem = stationItems.get(position);
        holder.stationName.setText(stationItems.get(position).getName());
        holder.stationShape.setImageResource(stationItems.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return stationItems.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView stationName;
        ImageView stationShape;
        public MyViewHolder(View itemView){
            super(itemView);
            stationName = itemView.findViewById(R.id.stationName);
            stationShape = itemView.findViewById(R.id.stationShape);

        }
    }

}
