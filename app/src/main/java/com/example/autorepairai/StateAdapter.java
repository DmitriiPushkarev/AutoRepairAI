package com.example.autorepairai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StateAdapter  extends RecyclerView.Adapter<StateAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<String> strList;

    public StateAdapter(Context context, List<String> strList) {
        this.strList = strList;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public StateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StateAdapter.ViewHolder holder, int position) {
        String str = strList.get(position);
        holder.tvDamage.setText(str);
    }

    @Override
    public int getItemCount() {
        return strList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDamage;
        ViewHolder(View view){
            super(view);
            tvDamage = view.findViewById(R.id.tvDamage);
        }
    }
}
