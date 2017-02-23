package com.example.korovka.soundcloud;

/**
 * Created by korovka on 2/15/17.
 */
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.example.korovka.soundcloud.Adapter.SongAdapter;
import com.example.korovka.soundcloud.Model.Song;
import com.example.korovka.soundcloud.Request.SoundcloudApiRequest;
import com.example.korovka.soundcloud.Utility.Utility;
import com.mikepenz.materialdrawer.Drawer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabFragment1 extends Fragment {

    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;
    private static final String favoritedArtistNamesKey = "favoritedArtistNamesKey";
    private ViewPager mViewPager;
    private Drawer.Result drawerResult = null;
    private static final String TAG = "APP";
    private RecyclerView recycler;
    private SongAdapter mAdapter;
    private ArrayList<Song> songList;
    private int currentIndex;
    private TextView tb_title, tb_duration, tv_time;
    private ImageView iv_play, iv_next, iv_previous;
    private ProgressBar pb_loader, pb_main_loader;
    private MediaPlayer mediaPlayer;
    private long currentSongLength;
    private SeekBar seekBar;
    private boolean firstLaunch = true;
    private FloatingActionButton fab_search;
    private GridLayoutManager lLayout;
    private View view;
    private SearchView searchView;

    public TabFragment1() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_fragment_1, container, false);

        //Initialisation of VIEWS
        initializeViews();
        //Request recovering songs
        getSongList("");

        songList = new ArrayList<>();
        lLayout = new GridLayoutManager(MainActivity.getThis(), 2);

        //    recycler.addItemDecoration(new MarginDecoration(this));
        //    recycler.setHasFixedSize(true);

        mAdapter = new SongAdapter(MainActivity.getThis().getApplicationContext(), songList, new SongAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(Song song, int position) {
                firstLaunch = false;
                changeSelectedSong(position);
                prepareSong(song);
            }
        });
        recycler = (RecyclerView) view.findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(lLayout);
        recycler.setAdapter(mAdapter);

        //Initialisation of media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //SONG LAUNCHER
                togglePlay(mp);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("mainactivity","setOnCompletionListener");
                if(currentIndex + 1 < songList.size()){
                    Song next = songList.get(currentIndex + 1);
                    changeSelectedSong(currentIndex+1);
                    prepareSong(next);
                }else{
                    Song next = songList.get(0);
                    changeSelectedSong(0);
                    prepareSong(next);
                }
            }
        });

        //CONTROL seekbar
        handleSeekbar();

        //CONTROL SONG
        pushPlay();
        pushPrevious();
        pushNext();
        fab_search.setVisibility(View.INVISIBLE);

        //Listener SEARCH
        /*
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {Log.d("mainactivity","setOnClickListener");
                createDialog();
            }
        });
        */
        /*
        GridView gridview = (GridView)view.findViewById(R.id.gridview);

        List<ItemObject> allItems = getAllItemObject();

        CustomAdapter customAdapter = new CustomAdapter(getActivity(), allItems);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        */
        return view;
    }

    private void handleSeekbar(){
        Log.d("hand","handleSeekbar");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void prepareSong(Song song){
        Log.d("prepareSong","prepareSong");
        currentSongLength = song.getDuration();
        pb_loader.setVisibility(View.VISIBLE);
        tb_title.setVisibility(View.GONE);
        iv_play.setImageDrawable(ContextCompat.getDrawable(MainActivity.getThis(), R.drawable.selector_play));
        tb_title.setText(song.getTitle());
        tv_time.setText(Utility.convertDuration(song.getDuration()));
        String stream = song.getStreamUrl()+"?client_id="+Config.CLIENT_ID;
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(stream);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void togglePlay(MediaPlayer mp){
        Log.d("togglePlay","togglePlay");
        if(mp.isPlaying()){
            mp.stop();
            mp.reset();
        }else{
            pb_loader.setVisibility(View.GONE);
            tb_title.setVisibility(View.VISIBLE);
            mp.start();
            iv_play.setImageDrawable(ContextCompat.getDrawable(MainActivity.getThis(), R.drawable.selector_pause));
            final Handler mHandler = new Handler();
            MainActivity.getThis().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setMax((int) currentSongLength / 1000);
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    tv_time.setText(Utility.convertDuration((long)mediaPlayer.getCurrentPosition()));
                    mHandler.postDelayed(this, 1000);

                }
            });
        }

    }


    private void initializeViews(){Log.d("initializeViews","initializeViews");
        tb_title = (TextView)view.findViewById(R.id.tb_title);
        iv_play = (ImageView) view.findViewById(R.id.iv_play);
        iv_next = (ImageView) view.findViewById(R.id.iv_next);
        iv_previous = (ImageView) view.findViewById(R.id.iv_previous);
        pb_loader = (ProgressBar) view.findViewById(R.id.pb_loader);
        pb_main_loader = (ProgressBar) view.findViewById(R.id.pb_main_loader);

        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        tv_time = (TextView) view.findViewById(R.id.tv_time);
        fab_search = (FloatingActionButton) view.findViewById(R.id.fab_search);
        searchView=(SearchView) view.findViewById(R.id.searchView);
    }

    public void getSongList(String query){Log.d("getSongList","getSongList");
        RequestQueue queue = VolleySingleton.getInstance(MainActivity.getThis()).getRequestQueue();
        SoundcloudApiRequest request = new SoundcloudApiRequest(queue);
        pb_main_loader.setVisibility(View.VISIBLE);
        request.getSongList(query, new SoundcloudApiRequest.SoundcloudInterface() {
            @Override
            public void onSuccess(ArrayList<Song> songs) {
                currentIndex = 0;
                pb_main_loader.setVisibility(View.GONE);
                songList.clear();
                songList.addAll(songs);
                mAdapter.notifyDataSetChanged();
                mAdapter.setSelectedPosition(0);

            }

            @Override
            public void onError(String message) {Log.d("onError","tonError");
                pb_main_loader.setVisibility(View.GONE);
                Toast.makeText(MainActivity.getThis(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeSelectedSong(int index){Log.d("changeSelectedSong","changeSelectedSong");
        mAdapter.notifyItemChanged(mAdapter.getSelectedPosition());
        currentIndex = index;
        mAdapter.setSelectedPosition(currentIndex);
        mAdapter.notifyItemChanged(currentIndex);
    }

    private void pushPlay(){Log.d("pushPlay","pushPlay");
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mediaPlayer.isPlaying() && mediaPlayer != null){
                    iv_play.setImageDrawable(ContextCompat.getDrawable(MainActivity.getThis(), R.drawable.selector_play));
                    mediaPlayer.pause();
                }else{
                    if(firstLaunch){
                        Song song = songList.get(0);
                        changeSelectedSong(0);
                        prepareSong(song);
                    }else{
                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    iv_play.setImageDrawable(ContextCompat.getDrawable(MainActivity.getThis(), R.drawable.selector_pause));
                }

            }
        });
    }

    private void pushPrevious(){Log.d("togglePlay","pushPrevious");

        iv_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if(mediaPlayer != null){

                    if(currentIndex - 1 >= 0){
                        Song previous = songList.get(currentIndex - 1);
                        changeSelectedSong(currentIndex - 1);
                        prepareSong(previous);
                    }else{
                        changeSelectedSong(songList.size() - 1);
                        prepareSong(songList.get(songList.size() - 1));
                    }

                }
            }
        });

    }

    private void pushNext(){Log.d("mainactivity","pushNext");

        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if(mediaPlayer != null){

                    if(currentIndex + 1 < songList.size()){
                        Song next = songList.get(currentIndex + 1);
                        changeSelectedSong(currentIndex + 1);
                        prepareSong(next);
                    }else{
                        changeSelectedSong(0);
                        prepareSong(songList.get(0));
                    }

                }
            }
        });

    }

    public void createDialog(){Log.d("MA","createDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getThis());
   //     final View view = getLayoutInflater().inflate(R.layout.dialog_search, null);
        builder.setTitle(R.string.search);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et_search = (EditText) view.findViewById(R.id.et_search);
                String search = et_search.getText().toString().trim();
                if(search.length() > 0){
                    getSongList(search);
                }else{
                    Toast.makeText(MainActivity.getThis(), "Pish pish pish pish", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.create().show();


    }

    @Override
    public void onDestroy() {Log.d("MA","Destroy");
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
        super.onDestroy();
    }


/*
    private List<ItemObject> getAllItemObject(){
        List<ItemObject> items = new ArrayList<>();
        items.add(new ItemObject(R.drawable.milian,"Dip It Low", "Christina Milian"));
        items.add(new ItemObject(R.drawable.adele,"Someone like you", "Adele Adkins"));
        items.add(new ItemObject(R.drawable.ciara,"Ride", "Ciara"));
        items.add(new ItemObject(R.drawable.gaga,"Paparazzi", "Lady Gaga"));
        items.add(new ItemObject(R.drawable.brown,"Forever", "Chris Brown"));
        items.add(new ItemObject(R.drawable.rihana,"Stay", "Rihanna"));
        items.add(new ItemObject(R.drawable.jason,"Marry me", "Jason Derulo"));
        items.add(new ItemObject(R.drawable.shakira,"Waka Waka", "Shakira"));
        items.add(new ItemObject(R.drawable.perry,"Dark Horse", "Katy Perry"));
        items.add(new ItemObject(R.drawable.milian,"Dip It Low", "Christina Milian"));
        items.add(new ItemObject(R.drawable.milian,"Dip It Low", "Christina Milian"));
        items.add(new ItemObject(R.drawable.milian,"Dip It Low", "Christina Milian"));
        return items;
    }
*/
}
