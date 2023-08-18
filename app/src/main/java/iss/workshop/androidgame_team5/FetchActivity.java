package iss.workshop.androidgame_team5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



    public class FetchActivity extends AppCompatActivity implements View.OnClickListener,FetchDelegate {

        private final int FETCH_IMAGES_MAX = 20;
        private String mURL; // this is to hold the image catalogue URL
        private File myDirectory;
        private ArrayList<File> imgFileList;
        private ArrayList<String> imgUrlList;
        private ArrayList<ImageDTO> fetchedImages;
        private boolean isDownloadThreadRunning;
        private Thread downloadImageThread;

        //View attributes
        private AutoCompleteTextView urlSearchBar;
        private SelectImgListener listener;
        private BaseAdapter adapter;
        private ProgressBar progressBar;
        private TextView progressText;
        public Button startButton;
        public List<Integer> positionList;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_fetch);
            myDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            ArrayList<String> exampleURLs = new ArrayList<String> (){
                {   add("https://www.marvel.com");
                    add("https://www.shutterstock.com/search/winter-travel");
                    add("https://stocksnap.io");
                    add("https://www.invalidurl.com1");
                    add("https://www.genkisushi.com.sg/about-us/");
                }
            };
            startButton=findViewById(R.id.startButton);
            ArrayAdapter<String> urlAdapter =
                    new ArrayAdapter(this,android.R.layout.simple_list_item_1, exampleURLs);

            urlSearchBar = findViewById(R.id.urlSearchBar);
            urlSearchBar.setFocusableInTouchMode(true);
            urlSearchBar.setOnFocusChangeListener((view, b) -> {
                if (!b) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(urlSearchBar.getWindowToken(), 0);
                }
            });
            urlSearchBar.setThreshold(1);
            urlSearchBar.setAdapter(urlAdapter);

            Button fetchBtn = findViewById(R.id.fetchBtn);
            if (fetchBtn != null)
                fetchBtn.setOnClickListener(this);
            isDownloadThreadRunning = false; //create page with false running
            initGridView();

            if(startButton !=null){
                startButton.setOnClickListener(this);
            }

        }



        private void parseHTMLImgURLs() {

            try {
                ArrayList<String> thisImgUrlList = new ArrayList<>();
                Document doc = Jsoup.connect(mURL).get();
                Elements links = doc.select("img[src]");
                for (Element link : links) {
                    if (link.attr("src").contains(".jpg") && link.attr("src").contains("https://")
                            && !link.attr("src").contains("?")) {
                        System.out.println("printing....");
                        thisImgUrlList.add(link.attr("src"));
                    }
                }

                imgUrlList = thisImgUrlList; //will take 2 seconds to fetch URL, therefore can't call imgUrlList variable early otherwise null
                for (String u : imgUrlList)
                    System.out.println(u);

            } catch (IOException e) {
                imgUrlList = null;
            }

        }

        protected ArrayList<File> createDestFiles() {
            ArrayList<File> imgFileList = new ArrayList<>();

            for (int i = 1; i <= FETCH_IMAGES_MAX; i++) {
                String thisFileName = "Image" + i + ".jpg";
                File thisFile = new File(myDirectory, thisFileName);
                imgFileList.add(thisFile);
                System.out.println("Created image files done: " + i);
            }
            return imgFileList;
        }

        protected int getImgUrlList() {
            try {
                parseHTMLImgURLs();
                if (imgUrlList != null && imgUrlList.size() >= 20) {
                    return 1; //1 = all good
                } else if (imgUrlList != null) {
                    return 2; // not enough images
                } else
                    return 3; //invalid url
            } catch (Exception e) {
                if (mURL.trim().isEmpty())
                    return 4; //blank url field
                return 3;
            }
        }

        protected void enterNewURLToast(int errorCode) {
            String msg;
            if (errorCode == 2)
                msg = "Insufficient images on webpage. \nPlease enter a URL with more images";
            else if (errorCode == 3)
                msg = "Unable to parse webpage. \nPlease enter a valid URL.";
            else
                msg = "URL field cannot be blank. \nPlease enter a URL";

            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }

        protected ImageDTO decodeImageIntoDTO(File DestFile, int imageID) {
            Bitmap bmp = BitmapFactory.decodeFile(DestFile.getAbsolutePath());
            return new ImageDTO(imageID, bmp);
        }

        protected boolean downloadThisImage(String imageURL, File destFile) {
            try {
                URL myURL = new URL(imageURL); //parse the url String into a URL object
                URLConnection conn = myURL.openConnection(); // open connection for this URL obj

                InputStream in = conn.getInputStream(); // create an input stream to read received data
                FileOutputStream out = new FileOutputStream(destFile); // output stream to write data into destination file
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                out.close();
                in.close();
                return true;

            } catch (Exception e) {
                return false;
            }
        }

        protected void initGridView() {
            setDefaultImage();
            GridView gridView = findViewById(R.id.fetchedImageGridView);
            adapter = new FetchedImageAdapter(this, fetchedImages);
            listener = new SelectImgListener(this,this);

            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(listener);
            progressBar = findViewById(R.id.progressBar);
            progressText = findViewById(R.id.progressText);
            progressBar.setVisibility(View.INVISIBLE);
            progressText.setText("");
        }

        private void setDefaultImage() {
            fetchedImages = new ArrayList<>();
            for (int i = 0; i < FETCH_IMAGES_MAX; i++) {
                fetchedImages.add(new ImageDTO(R.drawable.unavailable, BitmapFactory.decodeResource(this.getResources(), R.drawable.unavailable)));
            }
        }

        protected void resetGridView() {
            GridView gridView = findViewById(R.id.fetchedImageGridView);
            adapter = new FetchedImageAdapter(this, fetchedImages);
            listener = new SelectImgListener(this,this);

            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(listener);

        }

        @Override
        public void onClick(View view) {

            int buttonId=view.getId();
            if (buttonId==R.id.fetchBtn) {

                if (isDownloadThreadRunning && downloadImageThread != null) {
                    downloadImageThread.interrupt();
                    isDownloadThreadRunning = false;
                }

                urlSearchBar.clearFocus();

                downloadImageThread = new Thread(() -> {
                    isDownloadThreadRunning = true; //start running, say True
                    mURL = urlSearchBar.getText().toString();

                    imgFileList = createDestFiles(); //get twenty blank files to store
                    int fetchURLStatusCode = getImgUrlList();



                    if (fetchURLStatusCode == 1) {
                        System.out.println("All good---ImgURLList Have-" + imgUrlList.size() + " URL strings");
                        fetchedImages = new ArrayList<>();
                        Collections.shuffle(imgUrlList);
                        runOnUiThread(new progressUiRunnable(0));

                        while (fetchedImages.size() < FETCH_IMAGES_MAX) {
                            if (Thread.interrupted()) {
                                runOnUiThread(() -> {
                                    housekeepOnDownloadInterrupt();
                                    resetGridView();
                                });
                                return;
                            }

                            else {
                                int i = fetchedImages.size();
                                if (downloadThisImage(imgUrlList.get(i), imgFileList.get(i))) {
                                    int imgID = i + 1;
                                    fetchedImages.add(decodeImageIntoDTO(imgFileList.get(i), imgID));

                                    System.out.println("Adding fetchImages ImageDTO object ---->> No." + fetchedImages.size());

                                    runOnUiThread(new progressUiRunnable(fetchedImages.size()));
                                }
                            }
                        }
                        System.out.println("there are ..." + fetchedImages.size() + " imageDTO objects in fetchedImages");
                        setUpListener();

                    } else {
                        if (fetchURLStatusCode == 2) { //
                            runOnUiThread(() -> enterNewURLToast(2));
                        } else if  (fetchURLStatusCode == 3 ) { // 3 = invalid URL
                            runOnUiThread(() -> enterNewURLToast(3));
                        } else { // 3 = invalid URL
                            runOnUiThread(() -> enterNewURLToast(4));
                        }

                    }

                    isDownloadThreadRunning = false;
                });


                downloadImageThread.start();

                List<Integer> newlist=SelectImgListener.idList;
                if(newlist!=null){
                    newlist.clear();
                    SelectImgListener.count=0;
                    startButton.setEnabled(false);
                }

            }
            else{
                List<Integer> newlist=SelectImgListener.idList;
                deleteImg(newlist);
                Intent intent = new Intent(FetchActivity.this,MainActivity.class);
                FetchActivity.this.startActivity(intent);

            }

            if (buttonId==R.id.startButton)
            { Intent intent = new Intent(getApplicationContext(), GameMode.class);
                startActivity(intent);}
        }

        private void setUpListener() {
            listener.setFiles(imgFileList);
            List<Boolean> list = new ArrayList<>(Arrays.asList(new Boolean[fetchedImages.size()]));
            Collections.fill(list, Boolean.FALSE);
            listener.setSelectedFlags(list);
            listener.setDownloadFinished(true);
        }

        @Override
        public void getPosition(List<Integer> idList){
            for(int i:idList){
                positionList.add(i);
            }
        }

        @Override
        public void getCount(int count) {
            List<Integer> newlist=SelectImgListener.idList;


            if (count  == 6){

                progressBar.setVisibility(View.INVISIBLE);
                progressText.setVisibility(View.INVISIBLE);
//                startButton.setVisibility(View.VISIBLE);
                startButton.setEnabled(true);
            }else {
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
//                startButton.setVisibility(View.GONE);
                startButton.setEnabled(false);
            }
//            System.out.println(count);
        }

        public void deleteImg(List<Integer> idList){
            File dir= getExternalFilesDir((Environment.DIRECTORY_PICTURES));
            File[] filesInDir=dir.listFiles();
            int count = 0;
            for(File file:filesInDir) {

                String s = file.getName();
                if (!(idList.contains(count))) {
                    file.delete();
                    System.out.println("Successfully deleted");
                }
                else {
                    System.out.println(s);
                }
                count++;

            }
        }


        public class progressUiRunnable implements Runnable {

            protected int imgIdDone;

            progressUiRunnable(int idDone) {
                super();
                this.imgIdDone = idDone;
            }

            @Override
            public void run() {
                updateProgressViews(imgIdDone);
            }
        }

        protected void updateProgressViews(int numberDone) {

            //1 - Update ProgressBar
            System.out.println("UPDATING PROGRESS BAR:  ==== " + numberDone);

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(5*numberDone);
            int progress = progressBar.getProgress();

            //2 - Update Progress Text
            System.out.println("UPDATING PROGRESS TEXT:  ==== " + numberDone);

            String loadingText = "Downloading " + progress / 5 + " of 20 images";
            if(progress == 100){
                loadingText = "Download completed. \nSelect 6 images to start game!";

                startButton.setVisibility(View.VISIBLE);



            }
            progressText.setText(loadingText);

            //3-  Update GridView with new image
            System.out.println("UPDATING GridView:  ==== " + numberDone);

            FetchedImageAdapter fetchedImageAdapter = new FetchedImageAdapter(this, fetchedImages);
            GridView imageGridView = findViewById(R.id.fetchedImageGridView);
            if(imageGridView != null){
                imageGridView.setAdapter(fetchedImageAdapter);
            }

        }
        public void setButton(){
            startButton.setVisibility(View.INVISIBLE);
        }

        protected void housekeepOnDownloadInterrupt() {

            fetchedImages = new ArrayList<>();

        }

        public class FetchedImageAdapter extends BaseAdapter {

            private final LayoutInflater inflater;

            public FetchedImageAdapter(Context context, ArrayList<ImageDTO> fetchedImages) {
                this.fetchedImages = fetchedImages;
                this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            private final ArrayList<ImageDTO> fetchedImages;

            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.grid_item, parent, false);
                }

                ImageView imageView = convertView.findViewById(R.id.gridImage);

                Bitmap[] bitmaps = new Bitmap[20];

                for (int i = 0; i < fetchedImages.size(); i++) {
                    if (i < FETCH_IMAGES_MAX)
                        bitmaps[i] = fetchedImages.get(i).getBitmap();
                }

                imageView.setImageBitmap(bitmaps[position]);

                return convertView;
            }

        }



    }