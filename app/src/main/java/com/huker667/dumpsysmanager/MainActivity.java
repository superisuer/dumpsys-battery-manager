package com.huker667.dumpsysmanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button superUserButton;
    private Button battery_button;
    private Button apply_button;
    private TextView temp_text;
    private EditText mkatext;
    private CheckBox usb_checkbox;
    private SeekBar seekBar;
    private Button reset_button;
    private CheckBox invalid_checkbox;
    private EditText numtext;
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        battery_button = findViewById(R.id.button2);
        apply_button = findViewById(R.id.button3);
        usb_checkbox = findViewById(R.id.checkBox2);
        invalid_checkbox = findViewById(R.id.checkBox3);
        temp_text = findViewById(R.id.textView3);
        reset_button = findViewById(R.id.button4);
        reset_button.setEnabled(false);
        seekBar = findViewById(R.id.seekBar);
        apply_button.setEnabled(false);
        battery_button.setEnabled(false);
        mkatext = findViewById(R.id.editTextNumberSigned2);
        superUserButton = findViewById(R.id.button);
        superUserButton.setText(R.string.grant_root);
        TextView outputtext = findViewById(R.id.textView2);
        numtext = findViewById(R.id.editTextNumberSigned);
        superUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSuperUser();
            }
        });
        battery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputtext.setText(executeCommand("dumpsys battery"));
            }
        });
        apply_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle(getString(R.string.alert_t))
                        .setMessage(getString(R.string.alert_d))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                outputtext.setText(executeCommand("dumpsys battery set level " + numtext.getText().toString()));
                                outputtext.setText(outputtext.getText() + "\n" + executeCommand("dumpsys battery set counter " + mkatext.getText().toString()));
                                outputtext.setText(outputtext.getText() + "\n" + executeCommand("dumpsys battery set temp " + seekBar.getProgress()));

                                int temp = Integer.parseInt(executeCommand("dumpsys battery get temp").trim());
                                double tempCelsius = temp / 10.0;
                                @SuppressLint("DefaultLocale") String result = String.format("%.1f °C", tempCelsius);
                                temp_text.setText(getString(R.string.temp) + " (" + result + ")");
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
        });

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputtext.setText(executeCommand("dumpsys battery reset"));
                getValues();
            }
        });

        usb_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    outputtext.setText(executeCommand("dumpsys battery set usb 1"));
                } else {
                    outputtext.setText(executeCommand("dumpsys battery set usb 0"));
                }
            }
        });

    }
    @SuppressLint("SetTextI18n")
    private void getValues(){
        if (Objects.equals(executeCommand("dumpsys battery get usb").trim(), "false")) {
            usb_checkbox.setChecked(false);
        }
        else if (Objects.equals(executeCommand("dumpsys battery get usb").trim(), "true")) {
            usb_checkbox.setChecked(true);
        }
        if (Objects.equals(executeCommand("dumpsys battery get invalid").trim(), "0")) {
            invalid_checkbox.setChecked(false);
        }
        else if (Objects.equals(executeCommand("dumpsys battery get invalid").trim(), "1")) {
            invalid_checkbox.setChecked(true);
        }
        numtext.setText(executeCommand("dumpsys battery get level").trim());
        mkatext.setText(executeCommand("dumpsys battery get counter").trim());
        seekBar.setProgress(Integer.parseInt(executeCommand("dumpsys battery get temp").trim()));
        int temp = Integer.parseInt(executeCommand("dumpsys battery get temp").trim());
        double tempCelsius = temp / 10.0;
        @SuppressLint("DefaultLocale") String result = String.format("%.1f °C", tempCelsius);
        temp_text.setText(getString(R.string.temp) + " (" + result + ")");

    }
    private void requestSuperUser() {
        try {
            Process process = Runtime.getRuntime().exec("su -c id"); // i use this command for root check
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            String result = output.toString();
            if (result.contains("uid=0")) {
                superUserButton.setEnabled(false);
                battery_button.setEnabled(true);
                apply_button.setEnabled(true);
                reset_button.setEnabled(true);
                superUserButton.setText(R.string.super_user_granted);
                getValues();
            } else {
                throw new Exception("Access denied");
            }

        } catch (Exception e) {
            superUserButton.setEnabled(true);
            battery_button.setEnabled(false);
            apply_button.setEnabled(false);
            reset_button.setEnabled(false);
            superUserButton.setText(R.string.super_user_denied);
        }
    }

    public String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su -c " + command);
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            return output.toString();

        } catch (Exception e) {
            return "Failed to execute: " + command + ". Check that you have granted access to root permissions. (" + e + ")";
        }
    }


}