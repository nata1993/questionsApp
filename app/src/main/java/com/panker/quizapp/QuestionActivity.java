package com.panker.quizapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

public class QuestionActivity extends AppCompatActivity {

    public static final String DATA = "data";
    Stack<Integer> previousQuestion = new Stack<>();
    int questionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // using bundle to get info from previous activity
        Bundle data = this.getIntent().getExtras();
        if(data != null){
            displayQuestion(data.getInt("questionNumber"));
        }
        // this is needed for adding back button ont the actionbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    /*...*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.question_menu, menu);
        return true;
    }
    private void displayQuestion(int n) {
        questionNumber = n;
        Resources resources = getResources();
        TypedArray questions = resources.obtainTypedArray(R.array.questions);
        String[] question = resources.getStringArray(questions.getResourceId(n -1, -1));

        // if user has reached the last question, next button text wi change to submit
        if (n == questions.length()){
            Button next = findViewById(R.id.btnNext);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submit();   // method that will submit answers
                }
            });
            next.setText(getString(R.string.subAns));
        }
        else{
            Button next = findViewById(R.id.btnNext);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNext(view);
                }
            });
            next.setText(getString(R.string.subAns));
        }

        // if its the first question, then previous button will be disabled
        if (n == 1){
            findViewById(R.id.btnPrev).setEnabled(false);
        }
        else{
            findViewById(R.id.btnPrev).setEnabled(true);
        }

        // displaying question number
        TextView txtQuestionNumber = findViewById(R.id.txtQuestionNumber);
        txtQuestionNumber.setText(String.format(getString(R.string.questionNumber), n, questions.length()));
        ((TextView)findViewById(R.id.txtQuestion)).setText(question[0]);    // displays the question in text view

        //displaying the answer options in radiobuttons
        for (int i = 1; i<=4; ++i){
            String optId = String.format(Locale.getDefault(), "opt%d", i);
            RadioButton rbtn = findViewById(getResources().getIdentifier(optId, "id", this.getPackageName()));
            rbtn.setText(question[i]);
            rbtn.setChecked(false);
        }

        // checks to see if there are previously saved answers
        SharedPreferences preferences = getSharedPreferences(DATA, 0);
        RadioButton rbtn = findViewById(getResources().getIdentifier(preferences.getString(String.format(Locale.getDefault(), "q%d", n), "opt1"), "id", this.getPackageName()));
        rbtn.setChecked(true);
        questions.recycle();    // recycle the typedarray after not using it anymore
    }

    // this will close actions when going to previous activity, this is required because of the back button used in action
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void onPrev(View view) {
        previousQuestion.push(questionNumber);
        saveAnswer(questionNumber);
        displayQuestion(questionNumber - 1);
    }

    private void saveAnswer(int number) {
        SharedPreferences preferences = getSharedPreferences(DATA, 0);
        SharedPreferences.Editor editor = preferences.edit();
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        editor.putString(String.format(Locale.getDefault(), "q%d", number), getResources().getResourceEntryName((radioGroup.getCheckedRadioButtonId())));
        editor.apply();
    }

    public void onNext(View view) {
        previousQuestion.push(questionNumber);
        saveAnswer(questionNumber);
        displayQuestion(questionNumber + 1);
    }

    public void onMenuSubmit(MenuItem item) {
        submit();
    }

    //saving answers first then moving to new activity where the user can see results
    private void submit() {
        saveAnswer(questionNumber);
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.submitTitle))    //confirm submission
                .setMessage(getString(R.string.submitMessage))    //are you sure?
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent result = new Intent(getApplicationContext(), Result.class);
                        startActivity(result);
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    //here need to add onBackPressed method
    
}