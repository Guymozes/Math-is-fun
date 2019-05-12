package com.example.guy.projectapp1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.Calendar;

import io.paperdb.Paper;


public class TrainPage extends Utils {
    TextView submitBtnView;
    EditText answerText;
    Exercise exercise = user.getNextExercise();
    long start_input_answer;
    int user_answer = 0;
    CountDownTimer train_counter;
    MediaPlayer exercise_media = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        submitBtnView = findViewById(R.id.SubmitBtn);
        answerText = findViewById(R.id.InputEditText);
        updateView();
        Button submitBtn = (Button) submitBtnView; // save the button for reference
        submitBtn.setOnClickListener(new View.OnClickListener() { // create a new event after pressing the button
                                         @Override
                                         public void onClick(View view) {
                                             try{
                                                 user_answer =  Integer.parseInt(answerText.getText().toString());
                                                 handleAnswer();
                                             }catch(NumberFormatException ex){ // if clicked submitBtn without input
                                             }
                                         }
                                     }
        );
        answerText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    try{
                        user_answer =  Integer.parseInt(answerText.getText().toString());
                        handleAnswer();
                    }
                    catch (NumberFormatException ignored){
                    }

                }
                UIUtil.showKeyboard(TrainPage.this, answerText);
                return true;
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(TrainPage.this);
        Context context = LocaleHelper.setLocale(TrainPage.this, (String) Paper.book().read("language"));
        builder.setCancelable(true);
        builder.setTitle(context.getResources().getString(R.string.start_training));
        builder.setMessage(context.getResources().getString(R.string.start_training));
        builder.setCancelable(false);
        builder.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                train_counter = new CountDownTimer(SESSION_MILLI_DURATION, 1000) {
                    Context context = LocaleHelper.setLocale(TrainPage.this, (String) Paper.book().read("language"));
                    public void onTick(long millisUntilFinished) {
                        TextView res = findViewById(R.id.ResultTextView);
                        res.setText(String.format("%s %s", context.getResources().getString(R.string.seconds_remaining), millisUntilFinished / 1000));
                        res.setTextColor(Color.BLACK);
                        res.setTextSize(16);
                    }
                    public void onFinish() {
                        user.setEndSession();
                        saveUser(user);
                        testDoneToast();
                    }
                }.start();
                showExercise(exercise);
                user.start_session_time = simpleDateFormat.format(Calendar.getInstance().getTime());
            }
        });
        UIUtil.showKeyboard(this, answerText);
        builder.show();

    }

    public void handleAnswer(){
        exercise.time_answered = System.currentTimeMillis();
        if ((exercise.time_answered - exercise.time_displayed)/1000 > MAX_TIME_TO_ANSWER){
            handleOverTimeAnswer(false);
        }
        else{
            try{
                user.setAnswer(exercise, user_answer);
                if (user_answer == (exercise.result())) { /*correct answerText*/
                    toastAfterAnswer(true, true, exercise);
                }
                else {
                    toastAfterAnswer(false, true, exercise);
                }
                if(!user.session_done){ //backup -> && user.current_exercises.size() != 0
                    if (user.total_answers % 4 == 0) {
                        saveUser(user);
                    }
                    answerText.setText("");
                    exercise = user.getNextExercise();
                    showExercise(exercise);
                }
            }
            catch(NumberFormatException ignored){
            }
        }
        UIUtil.showKeyboard(this, answerText);
    }

    public void time_for_answer_train(View view){
        start_input_answer = System.currentTimeMillis();
        if ((start_input_answer - exercise.time_displayed)/1000 > 5){
            Toast.makeText(TrainPage.this, "Think faster..(5 seconds)", Toast.LENGTH_LONG).show();
            handleOverTimeAnswer(true);
        }
    }

    public void handleOverTimeAnswer(boolean no_answer){
        user.setAnswer(exercise, 0); // wrong answerText
        if (user.total_answers % 4 == 0) {
            saveUser(user);
        }
        if (exercise.result() == user_answer && !no_answer){
            Toast.makeText(TrainPage.this, "Answer correct, but it took to much time..", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(TrainPage.this, "Try again..(faster)", Toast.LENGTH_SHORT).show();
        }
        answerText.setText("");
        // exercise = user.getNextExercise(); // if we want to change exercise after over time
        showExercise(exercise);
    }

    public void testDoneToast(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TrainPage.this);
        Context context = LocaleHelper.setLocale(TrainPage.this, (String) Paper.book().read("language"));
        builder.setCancelable(true);
        builder.setTitle(context.getResources().getString(R.string.session_done));
        builder.setMessage(String.format("You just won %s points!", user.current_count_points_per_day));
        builder.setCancelable(false);
        builder.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

    public void showExercise(Exercise exercise){
        TextView show_exercise = findViewById(R.id.ExerciseTextView);
        String temp_exercise = String.format("%s * %s", exercise.mul1, exercise.mul2);
        show_exercise.setText(temp_exercise);
        exercise_media = MediaPlayer.create(this, Utils.resID[exercise.exercise_id]);
        exercise_media.start();
        exercise.time_displayed = System.currentTimeMillis();
        user_answer = 0;
    }
    private void updateView() {
        Context context = LocaleHelper.setLocale(this, (String) Paper.book().read("language"));
        Resources resources = context.getResources();
        submitBtnView.setText(resources.getString(R.string.submit));
        answerText.setHint(resources.getString(R.string.answer));
    }

    @Override
    public void onBackPressed() {
        Context context = LocaleHelper.setLocale(this, (String) Paper.book().read("language"));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.exit_session));
        builder.setMessage(context.getString(R.string.exit_session_warning));
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.end_session_time = simpleDateFormat.format(Calendar.getInstance().getTime());
                saveUser(user);
                train_counter.cancel();
                finish();
            }
        });
        builder.show();
    }
}