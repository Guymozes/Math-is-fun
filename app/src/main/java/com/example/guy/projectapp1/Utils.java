package com.example.guy.projectapp1;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import io.paperdb.Paper;


public class Utils extends AppCompatActivity {

    protected static final String FILE_NAME = "DATA_FILE.txt";
    public static int MAX_NUMBER = 9;
    public static int SINGLE_MODE = 0;
    public static int MULTI_MODE = 1;
    public static int DEFAULT_LANG = 0;
    protected static int SEARCH_MODE = 0;
    protected static int TRAIN_MODE = 1;
    protected static User user;
    protected static long SESSION_MILLI_DURATION = 30000; // todo - change to 3 minutes - 180000
    protected static int optional_exercises = 1000;
    final protected static int ENGLISH = 0;
    final protected static int HEBREW = 1;
    final protected static int ARABIC = 2;
    final protected static int RUSSIAN = 3;
    protected static int PERFECT_TIME_FOR_ANSWER = 3;
    protected static int NUM_OF_EXERCISES_IN_SESSION = 4;
    protected static int MAX_TIME_TO_ANSWER = 10;

    public void saveUser(User user){
        FileOutputStream fos;
        Gson gson = new Gson();
        String json = gson.toJson(user);
        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(json.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadUser(){
        Gson gson = new Gson();
        FileInputStream fis;
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }
            Type type = new TypeToken<User>(){}.getType();
            user = gson.fromJson(sb.toString(), type);

        } catch (FileNotFoundException e) {
            user = new User(-1);
            saveUser(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void toastAfterAnswer(Boolean good_answer, Boolean train_mode, Exercise exercise){
        Context context = LocaleHelper.setLocale(this, (String) Paper.book().read("language"));
        Toast toast;
        Toast second_toast = null;
        int color;
        if(good_answer){
            toast = Toast.makeText(this, context.getResources().getString(R.string.Good_job_text), Toast.LENGTH_LONG);
            color = Color.GREEN;
        }
        else{
            toast = Toast.makeText(this, context.getResources().getString(R.string.mistake_text), Toast.LENGTH_SHORT);
            color = Color.RED;
            if(!train_mode){
                second_toast = Toast.makeText(this, String.format("%s %s", context.getResources().getString(R.string.show_answer), exercise.mul1 * exercise.mul2), Toast.LENGTH_LONG);
            }
        }
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        messageTextView.setTextColor(color);
        toast.show();
        if(second_toast != null) {
            group = (ViewGroup) second_toast.getView();
            messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(25);
            messageTextView.setTextColor(color);
            second_toast.show();
        }
    }
}


