package com.example.autorepairai;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    public TextView damageTV;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        //damageTV = itemView.findViewById(R.id.damage);
    }
}
