package com.example.guoziliang.horizontalrefresh;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.library.DragView;
import com.example.library.HorizontalLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    HorizontalLayout horizontalLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        horizontalLayout = (HorizontalLayout) findViewById(R.id.horizontal);
        DragView dragView = new DragView(this);
        horizontalLayout.addDragView(dragView);
        horizontalLayout.setOnDragCallBack(dragView);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        Adapter adapter = new Adapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{

        List<String> list = new ArrayList<>();

        Context context;
        public Adapter(Context context) {
            this.context = context;
            for (int i = 0; i < 20; i ++) {
                list.add(i + "");
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(position + "");
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }
    }


}
