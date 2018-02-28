package ca.warp7.android.scouting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ScoutingActivity
        extends AppCompatActivity
        implements InputControls.ActivityListener {

    Handler handler;
    Vibrator vibrator;

    ActionBar actionBar;
    TextView statusBanner;
    TextView statusTimer;

    ViewPager pager;
    PagerAdapter pagerAdapter;

    int timer = 0;
    int currentTab = 0;
    int lastRecordedTime = -1;

    Specs specs;
    Encoder encoder;

    ArrayList<Specs.Layout> layouts;

    final Animation in = new AlphaAnimation(0.0f, 1.0f);
    final Animation out = new AlphaAnimation(1.0f, 0.0f);


    Runnable timerUpdater = new Runnable() {
        @Override
        public void run() {

            updateStatusTimer();

            if (timer <= specs.getTimer()){
                handler.postDelayed(timerUpdater, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        specs = Specs.getInstance();

        if(specs == null){
            super.onBackPressed();
            return;
        }

        layouts = specs.getLayouts();

        handler = new Handler();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setupUI();
        setupEncoder();
        setupPager();

        updateLayout();

        vibrator.vibrate(new long[]{0, 35, 30, 35}, -1);
        timerUpdater.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scouting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_done:
                Intent intent;
                intent = new Intent(this, DataOutputActivity.class);
                intent.putExtra(ID.MSG_PRINT_DATA, encoder.format());
                intent.putExtra(ID.MSG_ENCODE_DATA, encoder.encode());
                startActivity(intent);
                return true;

            case R.id.menu_undo:
                Toast.makeText(this,
                        "Undo pressed (It does nothing) ",
                        Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_prev:
                if (currentTab > 0){
                    currentTab--;
                    updateLayout();
                }
                return true;

            case R.id.menu_next:
                if (currentTab < specs.getLayouts().size() - 1){
                    currentTab++;
                    updateLayout();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_confirmation)
                .setMessage(R.string.exit_confirmation_body)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScoutingActivity.super.onBackPressed();
                    }
                })
                .create()
                .show();
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public Vibrator getVibrator() {
        return vibrator;
    }

    @Override
    public boolean canUpdateTime() {
        return timer <= specs.getTimer() && lastRecordedTime != timer;
    }

    @Override
    public void push(int t, int v, int s) {
        encoder.push(t, v, s);
    }

    @Override
    public void pushOnce(int t, int v, int s) {
        encoder.pushOnce(t, v, s);
    }

    @Override
    public void pushTime(int t, int s) {
        push(t, timer, s);
        lastRecordedTime = timer;
    }


    private void setupUI(){
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_scouting);

        Toolbar myToolBar = findViewById(R.id.my_toolbar);
        myToolBar.setNavigationIcon(R.drawable.ic_close);
        myToolBar.setNavigationContentDescription(R.string.menu_close);
        setSupportActionBar(myToolBar);

        actionBar = getSupportActionBar();

        statusBanner = findViewById(R.id.status_banner);
        statusTimer = findViewById(R.id.status_timer);

        actionBar.setDisplayShowTitleEnabled(false);
    }

    private void setupEncoder(){
        Intent intent = getIntent();

        int matchNumber = intent.getIntExtra(ID.MSG_MATCH_NUMBER, -1);
        int teamNumber = intent.getIntExtra(ID.MSG_TEAM_NUMBER, -1);
        String scoutName = intent.getStringExtra(ID.MSG_SCOUT_NAME);

        encoder = new Encoder(matchNumber, teamNumber, scoutName);

    }

    private void setupPager(){

        pager = findViewById(R.id.pager);

        pagerAdapter = new InputTabsPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,
                                       float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentTab = position;
                updateLayout();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateStatus(final String status){

        in.setDuration(125);
        out.setDuration(125);

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                statusBanner.setText(status);
                statusBanner.startAnimation(in);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(!statusBanner.getText().toString().isEmpty()){
            statusBanner.startAnimation(out);
        } else {
            statusBanner.setText(status);
        }

    }

    private void updateLayout(){

        if (!layouts.isEmpty() && currentTab >= 0 && currentTab < layouts.size()) {

            Specs.Layout layout = layouts.get(currentTab);

            updateStatus(layout.getTitle());

            if (pager.getCurrentItem() != currentTab) {
                pager.setCurrentItem(currentTab, true);
            }
        }
    }

    private void updateStatusTimer(){
        String d = (timer <= 15? "Ⓐ" : timer <= 120 ? "Ⓣ" : "Ⓔ") + " "
                + String.valueOf(timer <= 15 ? 15 - timer : 150 - timer);

        statusTimer.setText(d);
        statusTimer.setTextColor(timer <= 15 ?
                0xFFCC9900 : (timer <= 120 ?
                0xFF006633 : (timer < 150 ?
                0xFFFF9900 : 0xFFFF0000)));

        timer++;
    }


    private class InputTabsPagerAdapter
            extends FragmentPagerAdapter {

        InputTabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return InputsFragment.createInstance(position);
        }

        @Override
        public int getCount() {
            return specs.getLayouts().size();
        }
    }

}
