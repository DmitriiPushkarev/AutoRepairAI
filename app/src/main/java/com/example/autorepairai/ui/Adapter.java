package com.example.autorepairai.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autorepairai.R;
import com.example.autorepairai.ViewHolder;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {

    Context context;
    List<String> damageList;

    public Adapter(Context context, List<String> damageList) {
        this.context = context;
        this.damageList = damageList;
        Log.i("DamageList: ", damageList.toString());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.fragment_step3,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.damageTV.setText(damageList.get(position));
    }

    @Override
    public int getItemCount() {
        return damageList.size();
    }
}