package com.example.autorepairai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class StateAdapterStep4 extends RecyclerView.Adapter<StateAdapterStep4.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<String> strList;

    public StateAdapterStep4(Context context, List<String> strList) {
        this.strList = strList;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public StateAdapterStep4.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item_step4, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StateAdapterStep4.ViewHolder holder, int position) {
        String str = strList.get(position);
        if(!Objects.equals(str, "null")) {
            int index = str.indexOf(":");
            holder.info.setText(str.substring(0, index).replace("\"", ""));
            holder.price.setText("~" + str.substring(index + 1).replace("\"", "") + " рублей");
        } else {
            holder.info.setText("Деталей не найдено");
            holder.price.setText("Попробуйте еще раз");
        }
    }

    @Override
    public int getItemCount() {
        return strList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView info;
        final TextView price;
        ViewHolder(View view){
            super(view);
            info = view.findViewById(R.id.textStep4_info);
            price = view.findViewById(R.id.textStep4_price);
        }
    }
}
