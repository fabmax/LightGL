package de.fabmax.lightgl.demo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ListActivity {

    private final ArrayList<HashMap<String, Object>> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, Object> item = new HashMap<>();
        item.put("text1", "Simple Scene");
        item.put("text2", "A simple scene with touch input and dynamic shadows");
        item.put("start", SimpleScene.class);
        mData.add(item);

        item = new HashMap<>();
        item.put("text1", "Physics");
        item.put("text2", "A simple physics demo using the JBullet physics engine (Tap to spawn cubes)");
        item.put("start", PhysicsScene.class);
        mData.add(item);

        String from[] = new String[] {
                "text1",
                "text2"
        };
        int[] to = new int[] {
                android.R.id.text1,
                android.R.id.text2
        };
        SimpleAdapter listAdapter = new SimpleAdapter(this, mData, android.R.layout.simple_list_item_2, from ,to);
        getListView().setAdapter(listAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(new Intent(this, (Class<?>) mData.get(position).get("start")));
    }
}
