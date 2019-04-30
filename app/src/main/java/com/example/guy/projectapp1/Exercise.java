package com.example.guy.projectapp1;

import java.util.Random;

public class Exercise {
    int mul1;
    int mul2;
    int count_correct_answers;
    int count_wrong_answers;
    long time_displayed; // for calculating the time for answers
    long time_answered;
    boolean displayed_today;

    Exercise(int i, int j){
        mul1 = i;
        mul2 = j;
        count_correct_answers = 0;
        count_wrong_answers = 0;
        time_displayed = 0;
        time_answered = 0;
        displayed_today = false;
    }
    // Exercise(){}

    public int result(){
        return mul1*mul2;
    }
    public static Double variance (Exercise exercise1, Exercise exercise2){
        Double count=0.0;
        String string1= String.valueOf(exercise1.mul1)+String.valueOf(exercise1.mul2)
                +String.valueOf(exercise1.result());
        String string2= String.valueOf(exercise2.mul1)+String.valueOf(exercise2.mul2)
                +String.valueOf(exercise2.result());
        for(int i=0; i<string1.length();i++){
            for(int j=0; j<string2.length();j++){
                if(string1.charAt(i)==string2.charAt(j)){
                    count++;
                }
            }
        }
        return count*(-1);
    }
}

