package com.example.guoziliang.horizontalrefresh;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.library.HorizontalDragView;
import com.example.library.HorizontalScrollLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    HorizontalScrollLayout horizontalLayout;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        horizontalLayout = (HorizontalScrollLayout) findViewById(R.id.horizontal);
        btn = (Button) findViewById(R.id.btn);
        HorizontalDragView dragView = new HorizontalDragView(this);
        horizontalLayout.addDragView(dragView);
        horizontalLayout.setOnDragCallBack(dragView);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        final Adapter adapter = new Adapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL));
        recyclerView.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.add(5);
            }
        });
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{

        LinkedList<String> list = new LinkedList<>();

        Context context;
        public Adapter(Context context) {
            this.context = context;
            for (int i = 0; i < 100; i ++) {
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
            holder.textView.setText(list.get(position) + "");
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void add(int count) {
            Random random = new Random();
            int x = random.nextInt();
            LinkedList<String> strings = new LinkedList<>();
            for (int i = 0; i < count; i ++) {
                strings.add(x + "i" + i);
            }
            changeDate(strings, count);
        }

        private void changeDate(LinkedList strs, int count) {
            for (int i = 0; i < count; i ++) {
                list.remove(i);
            }
            list.addAll(strs);
            notifyDataSetChanged();
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
