package com.huker667.dumpsysmanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Toast;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button superUserButton;
    private Button battery_button;
    private Button apply_button;
    private TextView temp_text;
    private Boolean via_shizuku;
    private Button shizuku_button;
    private EditText mkatext;
    private CheckBox usb_checkbox;
    private SeekBar seekBar;
    private Button reset_button;
    private CheckBox invalid_checkbox;
    private CheckBox force_checkbox;
    private EditText numtext;
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Toast.makeText(getApplicationContext(), "The app is crashed!", Toast.LENGTH_SHORT).show();
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            String error = sw.toString();

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Crash", error);
            clipboard.setPrimaryClip(clip);
            Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler()).uncaughtException(thread, throwable);
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        battery_button = findViewById(R.id.button2);
        via_shizuku = false;
        apply_button = findViewById(R.id.button3);
        usb_checkbox = findViewById(R.id.checkBox2);
        invalid_checkbox = findViewById(R.id.checkBox3);
        temp_text = findViewById(R.id.textView3);
        reset_button = findViewById(R.id.button4);
        reset_button.setEnabled(false);
        force_checkbox = findViewById(R.id.checkBox3);
        seekBar = findViewById(R.id.seekBar);
        apply_button.setEnabled(false);
        shizuku_button = findViewById(R.id.button6);
        battery_button.setEnabled(false);
        mkatext = findViewById(R.id.editTextNumberSigned2);
        superUserButton = findViewById(R.id.button);
        superUserButton.setText(R.string.grant_root);
        TextView outputtext = findViewById(R.id.textView2);
        numtext = findViewById(R.id.editTextNumberSigned);
        shizuku_button.setOnClickListener(v -> requestShizukuPermission());
        superUserButton.setOnClickListener(v -> requestSuperUser());
        battery_button.setOnClickListener(v -> outputtext.setText(executeCommand("dumpsys battery")));
        
        apply_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle(getString(R.string.alert_t))
                        .setMessage(getString(R.string.alert_d))
                        .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                            outputtext.setText(executeCommand("dumpsys battery set level " + numtext.getText().toString()));
                            outputtext.setText(outputtext.getText() + "\n" + executeCommand("dumpsys battery set counter " + mkatext.getText().toString()));
                            outputtext.setText(outputtext.getText() + "\n" + executeCommand("dumpsys battery set temp " + seekBar.getProgress()));

                            getValues();
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
        });

        reset_button.setOnClickListener(v -> {
            
            outputtext.setText(executeCommand("dumpsys battery reset"));
            getValues();
        });

        usb_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                outputtext.setText(executeCommand("dumpsys battery set usb 1"));
            } else {
                outputtext.setText(executeCommand("dumpsys battery set usb 0"));
            }
        });

    }
    private void requestShizukuPermission() {
        
        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                superUserButton.setEnabled(false);
                battery_button.setEnabled(true);
                apply_button.setEnabled(true);
                reset_button.setEnabled(true);
                superUserButton.setText(R.string.notneeded);
                shizuku_button.setText(R.string.super_user_granted);
                shizuku_button.setEnabled(false);
                via_shizuku = true;
                
            } else {
                
                Shizuku.requestPermission(123);
                
            }
        }
        catch (Exception e) {
            Log.e("error", "NO SHIZUKU FOUND.");
        }
    }


    @SuppressLint("SetTextI18n")
    private void getValues(){
        if (executeCommand("dumpsys battery get usb").trim().contains("false")) {
            usb_checkbox.setChecked(false);
        }
        else if (executeCommand("dumpsys battery get usb").trim().contains("true")) {
            usb_checkbox.setChecked(true);
        }
        if (executeCommand("dumpsys battery get invalid").trim().contains("0")) {
            invalid_checkbox.setChecked(false);
        }
        else if (executeCommand("dumpsys battery get invalid").trim().contains("1")) {
            invalid_checkbox.setChecked(true);
        }

        String levelStr = extractDigits(executeCommand("dumpsys battery get level").trim());
        String counterStr = extractDigits(executeCommand("dumpsys battery get counter").trim());
        String tempStr = extractDigits(executeCommand("dumpsys battery get temp").trim());

        numtext.setText(levelStr);
        mkatext.setText(counterStr);

        int temp = Integer.parseInt(tempStr);

        seekBar.setProgress(temp);
        double tempCelsius = temp / 10.0;
        @SuppressLint("DefaultLocale")
        String result = String.format("%.1f °C", tempCelsius);
        temp_text.setText(getString(R.string.temp) + " (" + result + ")");
    }
    private String extractDigits(String input) { // REGEX FOR OPLUS BATTERY
        return input.replaceAll("\\D", "");
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
                shizuku_button.setEnabled(false);
                shizuku_button.setText(R.string.notneeded);
                via_shizuku = false;
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

    public String executeRootCommand(String command) {
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
    public String executeCommand(String command) {
        if (force_checkbox.isChecked()){
            if (!command.endsWith("battery")){
                command = command + " -f";
            }

        }

        if (via_shizuku) {
            return executeShizukiCommand(command);
        }
        else {
            return executeRootCommand(command);
        }

    }

    private String executeShizukiCommand(String command) {
        // этот код я спиздил из ashell :3
        List<String> output = new ArrayList<>();
        ShizukuRemoteProcess process = null;
        String currentDir = "/";
        StringBuilder result = new StringBuilder();

        try {
            process = Shizuku.newProcess(new String[]{"sh", "-c", command}, null, currentDir);
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = inputReader.readLine()) != null) {
                    output.add(line);
                    result.append(line).append("\n");
                }

                while ((line = errorReader.readLine()) != null) {
                    output.add(line);
                    result.append("ERROR: ").append(line).append("\n");
                }
            }
            process.waitFor();
            
        } catch (Exception e) {
            result.append("Exception: ").append(e.getMessage()).append("\n");
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return result.toString().trim();
    }
}