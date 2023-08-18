package iss.workshop.androidgame_team5;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button hsBtn=findViewById(R.id.btnHS);
        Button playBtn = findViewById(R.id.btnPlay);
        //set onClickListener
        if (playBtn != null) {
            playBtn.setOnClickListener(this);
        }
        if(hsBtn!=null){
            hsBtn.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnPlay) {


            //image adapter
            Intent intent = new Intent(this, FetchActivity.class);

            startActivity(intent);

        }
        if(id==R.id.btnHS){
            Intent intent= new Intent(this,HighScoresActivity.class);
            startActivity(intent);
        }

    }


}