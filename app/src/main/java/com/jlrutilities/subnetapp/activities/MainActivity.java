package com.jlrutilities.subnetapp.activities;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jlrutilities.subnetapp.R;

public class MainActivity extends AppCompatActivity {

  EditText inputTextView;
  Spinner spinner;
  AlertDialog.Builder builder;
  private int defaultNetmask;

  protected static final String IP_STRING_MESSAGE = "com.example.IPSTRING.Message";
  protected static final String CIDR_NETMASK_MESSAGE = "com.example.NETMASK.Message";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    init();
  }

  private void init() {
    //Init TextView
    inputTextView = findViewById(R.id.ipEntryTv);

    //Init Spinner
    spinner = findViewById(R.id.subnet_spinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.split_locations, R.layout.spinner_item);
    adapter.setDropDownViewResource(R.layout.spinner_item);
    spinner.setAdapter(adapter);
    defaultNetmask = 16; // default is "/24"
    spinner.setSelection(defaultNetmask);

    //Dialog Builder
    builder = new AlertDialog.Builder(this);
  }

  //Transition to Cheatsheet View
  public void viewCheat(View view) {
    Intent intent = new Intent( this, CheatsheetActivity.class);
    startActivity(intent);
  }

  //Transition to Splitter View
  public void subnetTransition(View view) {
    //Get Text From View
    String message = inputTextView.getText().toString();
    String[] ipArray = message.split("\\.");

    //InputValidation
    boolean check = isValid(ipArray);
    if (check == false) {
      return;
    }

    //Get Netmask From Spinner
    Spinner spinner = findViewById(R.id.subnet_spinner);
    String spinnerItem = spinner.getSelectedItem().toString();

    //Intent
    Intent intent = new Intent( this, SplitterActivity.class);
    intent.putExtra(IP_STRING_MESSAGE, message);
    intent.putExtra(CIDR_NETMASK_MESSAGE, spinnerItem);
    startActivity(intent);
  }

  //Reset input
  public void clearInput(View view){
    inputTextView.setText("");
    spinner.setSelection(defaultNetmask);
  }

  //Validate Input
  private boolean isValid(String[] array) {
    //Number of Tokens
    if (array.length != 4) {
      Toast.makeText(getApplicationContext(),"IP Invalid", Toast.LENGTH_SHORT).show();
      return false;
    }

    //String array to Integer array for checks
    Integer[] intArray = new Integer[4];
    for (int i = 0; i < array.length; i++) {
      try {
        intArray[i] = Integer.parseInt(array[i]);
      } catch (NumberFormatException nfe) {
        Toast.makeText(getApplicationContext(),"IP Invalid", Toast.LENGTH_SHORT).show();
        return false;
      }
    }

    //First Section of IP cannot be zero
    if (intArray[0] == 0) {
      Toast.makeText(getApplicationContext(),"IP Invalid", Toast.LENGTH_SHORT).show();
      return false;
    }

    //Testing bounds for valid input
    for (int i = 0; i < intArray.length; i++) {
      if ( intArray[i] >= 256 ) {
        Toast.makeText(getApplicationContext(),"IP Invalid", Toast.LENGTH_SHORT).show();
        return false;
      } else if (intArray[i] < 0) {
        Toast.makeText(getApplicationContext(),"IP Invalid", Toast.LENGTH_SHORT).show();
        return false;
      }
    }
    return true;
  }
}
