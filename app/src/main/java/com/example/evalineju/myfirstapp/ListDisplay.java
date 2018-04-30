package com.example.evalineju.myfirstapp;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListDisplay extends Activity {
    // Array of strings...
    String[] mobileArray = {"Blur","Edge Detection"};
    ListView listView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_display);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, mobileArray);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);



                // Show Alert
                //Toast.makeText(getApplicationContext(),
                //        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                //        .show();
                if (itemPosition == 0) {
                    Intent intent = new Intent(ListDisplay.this, InfoActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(ListDisplay.this, InfoActivity.class);
                    startActivity(intent);
                }

            }

        });


    }


}

