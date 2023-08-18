package iss.workshop.androidgame_team5;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private final int rowsize = 3;
    private ArrayList<GameImage> cardImages;
    private ImageView image1;
    private ImageView image2;
    private String message;
    private TextView msgTextView;
    private Button pauseButton;
    private TextView pauseText;
    private MediaPlayer mediaPlayer;
    private ArrayList<MediaPlayer> mediaPlayers;

    private int gameMode;
    private int currPlayerTurn;
    private int OpenImageCount;
    private int image1Id;
    private int image2Id;
    private int maximumScore;
    private int playerScore;
    private int p1Score;
    private int p2Score;
    private int timer;
    private boolean hasGameStarted;
    private boolean isTimerRunning;
    private boolean isFlipped;
    private boolean isNonMatchingImagePairOpen;
    private boolean isPaused;
    private List<String> highScores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        findViewById(R.id.backButton).setOnClickListener(this);
        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);
        pauseText = findViewById(R.id.pauseText);
        mediaPlayers = new ArrayList<>();

        Intent intent = getIntent();
        gameMode = intent.getIntExtra("mode",0);

        if(gameMode == 2){
            currPlayerTurn = 2;
            changeTurn();
        }
        highScores = getHighScoreArr();

        RecyclerView gameRecyclerView = findViewById(R.id.gameRecyclerView);
        cardImages = GameImage.createImageList(this);
        GameImagesAdapter adapter = new GameImagesAdapter(cardImages);
        adapter.setOnItemClickListener(new GameImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {

                if (!hasGameStarted) {

                    isTimerRunning = true;
                    isPaused = false;
                    hasGameStarted = true;
                    pauseButton.setVisibility(View.VISIBLE);

                    if(gameMode==1) {
                        initiateTimer();
                    }
                }

                if (isPaused || isFlipped ||
                        itemView.findViewById(R.id.gameImageView).getForeground() == null) {
                    return;
                }

                if (isNonMatchingImagePairOpen) {
                    waitMsg();
                    return;
                }

                //Image 1 clicked
                if (OpenImageCount == 0) {
                    image1 = itemView.findViewById(R.id.gameImageView);
                    image1Id = cardImages.get(position).getId();
                    flip(image1);
                    OpenImageCount = 1;
                }
                //Image 2 clicked
                else if (OpenImageCount == 1) {
                    image2 = itemView.findViewById(R.id.gameImageView);
                    image2Id = cardImages.get(position).getId();
                    flip(image2);

                    //Image 1 and 2 match
                    if (image1Id == image2Id) {
                        updateScore();

                        //All Images matched
                        if (playerScore == maximumScore) {
                            pauseButton.setEnabled(false);

                            //One Player Mode
                            if (gameMode==1) {
                                stopTimer();

                                //High Score achieved
                                if (highScores.size() < 5 || timer < convertTime(highScores.get(4))) {

                                    String score = convertTime(timer);
                                    highScores.add(score);
                                    highScoreArr(highScores);
                                    highScoreMsg();
                                }
                                else {
                                    playTune(R.raw.victory);
                                    victoryMsg();
                                }
                            }

                            //Two Player Mode
                            else if (gameMode==2) {
                                if (p1Score > p2Score){
                                    player1WinMsg();
                                }
                                else if (p2Score > p1Score){
                                    player2WinMsg();
                                }
                                else if (p1Score == p2Score){
                                    noWinnerMsg();
                                }
                            }

                            returnToMain();
                        }

                        //Images left to match
                        else {

                            matchMessage();
                            playTune(R.raw.match);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    perpetualMessage();
                                }
                            }, 1500);
                        }
                    }
                    //Image 1 and 2 no match
                    else {
                        isNonMatchingImagePairOpen = true;
                        didNotMatchMsg();
                        playTune(R.raw.fail);
                        flipNonMatchingCards();
                    }
                    //Two Player Mode
                    if (gameMode==2){
                        changeTurn();
                    }

                    OpenImageCount = 0;
                }
            }
        });
        gameRecyclerView.setAdapter(adapter);
        gameRecyclerView.setLayoutManager(new GridLayoutManager(this, rowsize));

        OpenImageCount = 0;
        playerScore = 0;
        p1Score = 0;
        p2Score =0;
        maximumScore = cardImages.size() / 2;
        isTimerRunning = false;
        timer = 0;
        msgTextView = findViewById(R.id.msg);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.backButton) {
            finish();
        } else if (id == R.id.pauseButton) {
            if (isPaused) {
                resume();
            } else {
                pause();
            }
        }
    }
    public void pause() {
        isPaused = true;
        pauseText.setVisibility(View.VISIBLE);
        pauseButton.setText("Resume Game");
        stopTimer();
    }

    public void resume() {
        isPaused = false;
        isTimerRunning = true;
        pauseText.setVisibility(View.INVISIBLE);
        pauseButton.setText("Pause");
        initiateTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hasGameStarted) {
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayers();
    }

    public void flip(View v) {
        isFlipped = true;
        if (v.getForeground() != null) {
            v.animate().withLayer().rotationY(90).setDuration(150).withEndAction(
                    new Runnable() {
                        @Override public void run() {
                            // second quarter turn
                            v.setForeground(null);
                            v.setRotationY(-90);
                            v.animate().withLayer().rotationY(0).setDuration(150).start();
                            isFlipped = false;
                        }
                    }
            ).start();
        } else {
            v.animate().withLayer().rotationY(-90).setDuration(150).withEndAction(
                    new Runnable() {
                        @Override public void run() {
                            // second quarter turn
                            v.setForeground(
                                    ContextCompat.getDrawable(GameActivity.this, R.drawable.questionmark));
                            v.setRotationY(90);
                            v.animate().withLayer().rotationY(0).setDuration(150).start();
                            isFlipped = false;
                        }
                    }
            ).start();
        }

    }

    //flip back when don't match
    private void flipNonMatchingCards() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flip(image1);
                flip(image2);

                isNonMatchingImagePairOpen = false;
                perpetualMessage();
            }
        }, 1000);
    }

    public void changeTurn() {

        TextView timerTextView = findViewById(R.id.textTimer);

        if (currPlayerTurn == 1) {
            currPlayerTurn = 2;
            timerTextView.setText("Player 2's Turn");
            updateScoreView(p2Score);

        }
        else if (currPlayerTurn == 2) {
            currPlayerTurn = 1;
            timerTextView.setText("Player 1's Turn");
            updateScoreView(p1Score);
        }
    }

    public void playTune(int soundId) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), soundId);
            mediaPlayers.add(mediaPlayer);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mediaPlayer = null;
                }
            });
            mediaPlayer.start();
        } else {
            MediaPlayer extraMediaPlayer = MediaPlayer.create(getApplicationContext(), soundId);
            mediaPlayers.add(extraMediaPlayer);
            extraMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayers.remove(mp);
                    mp.release();
                }
            });
            extraMediaPlayer.start();
        }
    }

    public void releaseMediaPlayers() {
        for (MediaPlayer mp : mediaPlayers) {
            mp.reset();
            mp.release();
        }
    }

    private void updateScore() {
        playerScore++;
        if(gameMode == 1) {
            updateScoreView(playerScore);
        }
        else if(gameMode == 2 && currPlayerTurn == 1) {
            p1Score++;
            updateScoreView(p1Score);

        }
        else if(gameMode == 2 && currPlayerTurn == 2) {
            p2Score++;
            updateScoreView(p2Score);

        }
    }

    private void updateScoreView(int playerScore) {
        String textScore = "Matches: " + playerScore + "/" + maximumScore;

        TextView textMatches = findViewById(R.id.textMatches);
        textMatches.setText(textScore);
    }

    private void returnToMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }, 3000);
    }

    public void highScoreArr(List<String> highscoreList) {
        String hsStr = "";
        SharedPreferences sp = this.getSharedPreferences("HIGHSCORE", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        if (highscoreList != null) {
            Collections.sort(highscoreList, (o1, o2) -> convertTime(o1) - convertTime(o2));
            if (highscoreList.size() > 5) {
                highscoreList.subList(5, highscoreList.size()).clear();
            }
            hsStr = String.join(",", highscoreList);
            mEdit1.putString("hsStr", hsStr);
            mEdit1.apply();
        }
    }

    public List<String> getHighScoreArr(){
        SharedPreferences sp = this.getSharedPreferences("HIGHSCORE", Activity.MODE_PRIVATE);
        String hsStr = sp.getString("hsStr","");
        if (hsStr.equals("")){
            return new ArrayList<>();
        }
        else {
            ArrayList<String> highScores = new ArrayList<>(Arrays.asList(hsStr.split(",")));
            Collections.sort(highScores, (o1, o2) -> convertTime(o1) - convertTime(o2));
            return highScores;
        }
    }

    private void initiateTimer() {
        final TextView timerTextView = findViewById(R.id.textTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = timer/ 3600;
                int minutes = (timer % 3600) / 60;
                int seconds = timer % 60;
                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        hours, minutes, seconds);
                timerTextView.setText(time);
                if (isTimerRunning) {
                    timer++;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void stopTimer() {
        isTimerRunning = false;
    }

    public String convertTime(Integer intTime){
        int hours = intTime / 3600;
        int minutes = (intTime % 3600) / 60;
        int seconds = intTime % 60;
        String score = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                hours, minutes, seconds);
        return score;
    }

    public int convertTime(String strTime) {
        String[] timeUnits = strTime.split(":");
        int hours = Integer.parseInt(timeUnits[0]) * 60 * 60;
        int minutes = Integer.parseInt(timeUnits[1]) * 60;
        int seconds = Integer.parseInt(timeUnits[2]);
        return hours + minutes + seconds;
    }

    private void perpetualMessage() {
        message = "Try to get two of the same images!";
        msgTextView.setText(message);
    }

    private void matchMessage() {
        message = "It's a match!";
        msgTextView.setText(message);
    }

    private void didNotMatchMsg() {
        message = "Oops it was not a match...";
        msgTextView.setText(message);
    }

    private void victoryMsg() {
        message = "You are victorious!\nEnding Game in progress...";
        msgTextView.setText(message);
    }

    private void highScoreMsg() {
        message = "You have made it to the highScore rankings!\nEnding Game in progress...";
        msgTextView.setText(message);
    }

    private void player1WinMsg(){
        message = "Congrats Player 1, you have won!\nEnding Game in progress...";
        msgTextView.setText(message);
    }

    private void player2WinMsg(){
        message = "Congrats Player 2, you have won!\nEnding Game in progress...";
        msgTextView.setText(message);
    }

    private void noWinnerMsg(){
        message = "It's a tie!\nEnding Game in progress...";
        msgTextView.setText(message);
    }

    private void waitMsg() {
        Toast.makeText(this, "The wrong image pair will be closed soon. Please be patient.",
                Toast.LENGTH_SHORT).show();
    }
}