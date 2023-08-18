package iss.workshop.androidgame_team5;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import java.io.File;

public class GameMode extends AppCompatActivity  implements View.OnClickListener  {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        findViewById(R.id.onePlayerButton).setOnClickListener(this);
        findViewById(R.id.twoPlayerButton).setOnClickListener(this);
        findViewById(R.id.backButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.onePlayerButton) {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra("mode",1);
            startActivity(intent);
        }

        else if (id == R.id.twoPlayerButton) {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra("mode",2);
            startActivity(intent);
        }
        else{
            File dir= getExternalFilesDir((Environment.DIRECTORY_PICTURES));
            File[] filesInDir=dir.listFiles();
            for(File file:filesInDir){
                file.delete();
            }
            finish();
        }
    }
}