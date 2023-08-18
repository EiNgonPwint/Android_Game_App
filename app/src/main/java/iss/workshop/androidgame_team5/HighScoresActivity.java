package iss.workshop.androidgame_team5;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HighScoresActivity extends AppCompatActivity {

    Button backBtn;
    List<String> highScoresList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);
        highScoresList = getHs();

        TextView score1 = findViewById(R.id.score1);
        TextView score2 = findViewById(R.id.score2);
        TextView score3 = findViewById(R.id.score3);
        TextView score4 = findViewById(R.id.score4);
        TextView score5 = findViewById(R.id.score5);

        TextView[] hsViews = {score1, score2, score3, score4, score5};
        for(int i = 0; i < highScoresList.size(); i++) {
            hsViews[i].setText(String.format(Locale.ENGLISH, "%s %s", hsViews[i].getText(), highScoresList.get(i)));
        }

        backBtn=findViewById(R.id.hsBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HighScoresActivity.this,MainActivity.class);
                finish();
            }
        });
        final Button button = findViewById(R.id.resetButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 0; i < highScoresList.size(); i++){
                    hsViews[i].setText("");
                }
                saveHs(new ArrayList<>());
                Toast.makeText(getApplicationContext(), "Reset successful!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<String> getHs(){

        SharedPreferences sp = this.getSharedPreferences("HIGHSCORE", Activity.MODE_PRIVATE);
        String hsString = sp.getString("hsStr","");
        if (hsString == ""){
            return new ArrayList<String>();
        }
        else{
            List<String> hsList = new ArrayList<String>(Arrays.asList(hsString.split(",")));
            return hsList;}
    }
    public void saveHs(List<String> hsList){
        String hsString = "";
        SharedPreferences sp = this.getSharedPreferences("HIGHSCORE", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        if(hsList != null){
            hsString = String.join(",", hsList);
            mEdit1.putString("hsStr", hsString);
            mEdit1.apply();
        }
    }
}
