package com.example.evalineju.myfirstapp;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.Manifest;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.support.annotation.NonNull;

public class ContactViewActivity extends ListActivity {

    private ArrayList<String> conNames;
    private ArrayList<String> conNumbers;
    private Cursor crContacts;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 1;
    private static final int PERMISSION_REQUEST_WRITE_CONTACTS = 2;
    protected static final String TAG = "ContactActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        conNames = new ArrayList<String>();
        conNumbers = new ArrayList<String>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs contact access");
                builder.setMessage("Please grant contact access so this app can read contacts.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
                    }
                });
                builder.show();
            }
            if (this.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs contact access");
                builder.setMessage("Please grant contact access so this app can write contacts.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_WRITE_CONTACTS);
                    }
                });
                builder.show();
            }
        }

        crContacts = ContactHelper.getContactCursor(getContentResolver(), "");
        crContacts.moveToFirst();

        while (!crContacts.isAfterLast()) {
            conNames.add(crContacts.getString(1));
            conNumbers.add(crContacts.getString(2));
            crContacts.moveToNext();
        }

        setListAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1,
                R.id.tvNameMain, conNames));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "read contact permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since read contact access has not been granted, this app will not be able to read contacts.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_WRITE_CONTACTS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "write contact permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since write contact access has not been granted, this app will not be able to write contacts.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }


    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, int resource, int textViewResourceId,
                         ArrayList<String> conNames) {
            super(context, resource, textViewResourceId, conNames);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = setList(position, parent);
            return row;
        }

        private View setList(int position, ViewGroup parent) {
            LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = inf.inflate(R.layout.liststyle, parent, false);

            TextView tvName = (TextView) row.findViewById(R.id.tvNameMain);
            TextView tvNumber = (TextView) row.findViewById(R.id.tvNumberMain);

            tvName.setText(conNames.get(position));
            tvNumber.setText("No: " + conNumbers.get(position));

            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater imf = getMenuInflater();
        imf.inflate(R.menu.contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            Intent intent = new Intent(ContactViewActivity.this, AddContact.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.item2) {
            Intent intent = new Intent(ContactViewActivity.this, DeleteContacts.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}

