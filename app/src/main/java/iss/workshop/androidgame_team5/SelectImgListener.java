package iss.workshop.androidgame_team5;



import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SelectImgListener extends FetchActivity implements AdapterView.OnItemClickListener {
    private final AppCompatActivity currentActivity;
    private List<File> files;
    private List<Boolean> selectedFlags;
    private boolean downloadFinished;
    public static int count=0;
    private FetchDelegate  delegate;
    public static List<Integer> idList=new ArrayList<>();
    public SelectImgListener(AppCompatActivity currentActivity,FetchDelegate delegate) {
        this.currentActivity = currentActivity;
        this.downloadFinished = false;
        this.delegate = delegate;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setSelectedFlags(List<Boolean> selectedFlags) {
        this.selectedFlags = selectedFlags;
    }

    public void setDownloadFinished(boolean downloadFinished) {
        this.downloadFinished = downloadFinished;
    }
    public int getImageCount()
    {
        File dir= getExternalFilesDir((Environment.DIRECTORY_PICTURES));
        File[] filesInDir=dir.listFiles();
        return filesInDir.length;
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!downloadFinished) {
            return;
        }


        GridView gridView = (GridView) adapterView;
        ViewGroup gridElement = (ViewGroup) gridView.getChildAt(position);
//        List<String> selectedPosition=new ArrayList<>();
        int ids=gridView.getSelectedItemPosition();

        Boolean selected = !selectedFlags.get(position);
        selectedFlags.set(position, selected);
        ImageView tickBox = gridElement.findViewById(R.id.tickBox);

        if (selected) {
            tickBox.setVisibility(View.VISIBLE);
            ++count;
            idList.add(position);
//           setButton();

        } else {
            tickBox.setVisibility(View.INVISIBLE);
            for(int i=0;i<idList.size();i++){
                if(idList.get(i)==position){
                    idList.remove(i);
                }
            }
            --count ;
        }delegate.getCount(count);


//        if(count==6){
//            intent.putExtra("Set","enable");
//        }
//        else{
//            intent.putExtra("Set","disable");
//        }

//        int numOfSelected = Collections.frequency(selectedFlags, true);
//        if (numOfSelected == 6) {
//            ArrayList<String> filePaths = IntStream.range(0, selectedFlags.size())
//                    .filter(selectedFlags::get)
//                    .mapToObj(i -> {
//                        File file = files.get(i);
//                        return file.getAbsolutePath();
//                    })
//                    .collect(Collectors.toCollection(ArrayList::new));
//
//        }
        for(int i=0;i<idList.size();i++){
            System.out.println("id is"+idList.get(i));

        }
//        delegate.getPosition(idList);
    }




}
