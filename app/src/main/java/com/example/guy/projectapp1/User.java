package com.example.guy.projectapp1;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static com.example.guy.projectapp1.Utils.MAX_NUMBER;
import static com.example.guy.projectapp1.Utils.TRAIN_MODE;
import static com.example.guy.projectapp1.Utils.optional_exercises;
import static com.example.guy.projectapp1.Utils.user;
import static com.example.guy.projectapp1.Utils.NUM_OF_EXERCISES_IN_SESSION;

public class User{
    int correct_answers;
    int wrong_answers;
    int total_answers;
    int mode; //single or multi
    int lang;
    int age;
    int session_type;
    int max_correct_tests_in_row;
    int current_correct_tests_in_row;
    int count_tests;
    int current_count_points_per_day;
    int max_points_per_day;
    long first_login;
    long last_login;
    long start_session_time;
    long start_exercise;
    long end_exercise;
    boolean session_done;
    boolean start_page;  //true if last page was the main menu - for the "back" option
    String name;
    ArrayList<Exercise> known_exercises;
    ArrayList<Exercise> unknown_exercises;
    ArrayList<Exercise> undefined_exercises;
    ArrayList<Exercise> current_exercises;

    User(int user_mode){
        this.mode = user_mode;
        this.lang = Utils.DEFAULT_LANG;
        this.session_type = Utils.SEARCH_MODE;
        this.first_login = System.currentTimeMillis();
        this.last_login = first_login;
        this.session_done = false;
        this.name = "";
        this.age = 0;
        this.start_page = true;
        init();
    }

    private void init(){
        this.correct_answers = 0;
        this.wrong_answers = 0;
        this.total_answers = 0;
        this.max_correct_tests_in_row = 0;
        this.current_correct_tests_in_row = 0;
        this.count_tests = 0;
        this.current_count_points_per_day = 0;
        this.max_points_per_day = 0;
        this.known_exercises = new ArrayList<Exercise>();
        this.unknown_exercises = new ArrayList<Exercise>();
        this.undefined_exercises = new ArrayList<Exercise>();
        this.current_exercises = new ArrayList<Exercise>();
        for (int i = 1; i <= MAX_NUMBER; i++){
            for( int j = i; j<= MAX_NUMBER; j++){
                Exercise exercise = new Exercise(i, j);
                this.undefined_exercises.add(exercise);
            }
        }
        this.exerciseGroupWithMaxVar();
    }

    public void resetHistory(){
        init();
    }

    public Exercise getNextExercise(){
        Random rand = new Random();
        if (this.session_type == Utils.TRAIN_MODE) {
            return current_exercises.get(rand.nextInt(current_exercises.size()));
        }
        else if(this.session_type == Utils.SEARCH_MODE){
            if (rand.nextInt(100) < 25){
                if (known_exercises.size() > 0) {
                    return known_exercises.get(rand.nextInt(known_exercises.size()));
                }
            }
            return current_exercises.get(rand.nextInt(current_exercises.size()));
        }
        return null;
    }

    public void setAnswer(Exercise exercise, int answer){
        int user_answer_time;
        user.end_exercise = System.currentTimeMillis();
        if (exercise.result() == answer){
            exercise.count_correct_answers++;
            user.correct_answers++;
        }
        else{
            exercise.count_wrong_answers++;
            user.wrong_answers++;
        }
        user.total_answers = user.correct_answers + user.wrong_answers;
        if(this.session_type == Utils.TRAIN_MODE){
            user_answer_time = (int)(((user.end_exercise - user.start_exercise)/1000));
            user.current_count_points_per_day += calculate_points(user_answer_time);
            setGroupTrainMode(exercise);
        }
        else if(this.session_type == Utils.SEARCH_MODE){
            Log.e(TAG,"22222 in set answer");
            setGroupSearchMode(exercise);
            if (unknown_exercises.containsAll(current_exercises)){
                user.session_type = TRAIN_MODE;
            }
            for (int i=0; i<current_exercises.size(); i++){
                if (known_exercises.contains(current_exercises.get(i))){
                    this.exerciseGroupWithMaxVar();
                    break;
                }
            }
        }
    }

    private int calculate_points(int user_answer_time) {
        return (int)(100*(Math.pow(0.95, Math.max(0,user_answer_time-Utils.PERFECT_TIME_FOR_ANSWER))));
    }

    private void setGroupTrainMode(Exercise exercise) {
        int i;
        if (exercise.count_wrong_answers == 1){
            for (i=0; i< current_exercises.size(); i++){
               current_exercises.get(i).count_wrong_answers=0;
               current_exercises.get(i).count_correct_answers=0;
            }
        }
        else {
            for (i=0; i< current_exercises.size(); i++){
                if(current_exercises.get(i).count_correct_answers<3){
                    return;
                }
            }
            // test done!
            for (i=0; i< current_exercises.size(); i++){
                moveExercise(current_exercises,known_exercises, current_exercises.get(i)); // TODO - check
            }
            this.session_type = Utils.SEARCH_MODE;
        }
    }
    private void setGroupSearchMode(Exercise exercise) {
        Log.e(TAG,"3333 in setGroupSearchMode");
        if(known_exercises.contains(exercise)){
            Log.e(TAG,"In known");
            if(exercise.count_wrong_answers == 1){
                moveExercise(known_exercises, undefined_exercises, exercise);
            }
        }
        else if(undefined_exercises.contains(exercise)){
            Log.e(TAG,"!!!!! Should be here!!!!!!");
            if(exercise.count_wrong_answers == 1){
                Log.e(TAG,"### in count wrong answers ###");
                moveExercise(undefined_exercises, unknown_exercises, exercise);
            }
            else if(exercise.count_correct_answers == 1){
                exercise.displayed_today = true;
            }
            else if(exercise.count_correct_answers == 2){
                moveExercise(undefined_exercises, known_exercises, exercise);
            }
        }
        else if(unknown_exercises.contains(exercise)){
            Log.e(TAG,"Last condition");
        }

    }
    private void moveExercise(ArrayList<Exercise> src, ArrayList<Exercise> dst, Exercise exercise){
        Log.e(TAG,"### in moveExercise ####");
        exercise.count_correct_answers = 0;
        exercise.count_wrong_answers = 0;
        dst.add(exercise);
        src.remove(exercise);
    }

    private void exerciseGroupWithMaxVar(){
        Random rand = new Random();
        int current_var;
        int max_var = 0;
        ArrayList <Exercise> candidates = new ArrayList<>();
        candidates.addAll(undefined_exercises);
        candidates.addAll(unknown_exercises);
        for (int i=0; i< optional_exercises; i++){
            ArrayList <Exercise> temp_group = new ArrayList<>();
            for (int j=0; j< NUM_OF_EXERCISES_IN_SESSION; j++){
                temp_group.add(candidates.get(rand.nextInt(candidates.size())));
            }
            current_var = calculateGroupVar(temp_group);
            if (current_var > max_var){
                max_var = current_var;
                this.current_exercises = temp_group;
            }
        }
    }

    private static int calculateGroupVar(ArrayList<Exercise> current_exercises){
        int sum_var = 0;
        for (int i=0; i< current_exercises.size(); i++){
            for(int j=i+1; j < current_exercises.size(); j++){
                sum_var += Exercise.variance(current_exercises.get(i), current_exercises.get(j));
            }
        }
        return sum_var;
    }
}