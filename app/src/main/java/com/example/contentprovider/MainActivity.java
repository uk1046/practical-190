package com.example.contentprovider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName, editTextValue;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editTextValue = findViewById(R.id.editTextValue);
        textViewResult = findViewById(R.id.textViewResult);

        Button buttonInsert = findViewById(R.id.buttonInsert);
        Button buttonQuery = findViewById(R.id.buttonQuery);

        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
            }
        });

        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryData();
            }
        });
    }

    private void insertData() {
        String name = editTextName.getText().toString().trim();
        String value = editTextValue.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(MyContract.DataEntry.COLUMN_NAME, name);
        values.put(MyContract.DataEntry.COLUMN_VALUE, value);

        Uri uri = getContentResolver().insert(MyContract.DataEntry.CONTENT_URI, values);
        if (uri != null) {
            textViewResult.setText("Data inserted successfully!");
        } else {
            textViewResult.setText("Failed to insert data!");
        }
    }

    private void queryData() {
        Cursor cursor = getContentResolver().query(MyContract.DataEntry.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            StringBuilder result = new StringBuilder();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(MyContract.DataEntry.COLUMN_NAME));
                String value = cursor.getString(cursor.getColumnIndex(MyContract.DataEntry.COLUMN_VALUE));
                result.append("Name: ").append(name).append(", Value: ").append(value).append("\n");
            }
            cursor.close();
            textViewResult.setText(result.toString());
        } else {
            textViewResult.setText("No data found!");
        }
    }
}
