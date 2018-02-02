package ca.warp7.android.scouting;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class DataOutputActivity extends AppCompatActivity {

    TextView dataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_output);

        // Set the toolbar to be the default action bar

        Toolbar myToolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolBar);

        // Set up the action bar

        ActionBar actionBar = getSupportActionBar();

        // TODO actually show the title based on what fragment is shown

        if (actionBar != null) {
            actionBar.setTitle("Submit Match Data");
        }


        Intent intent = getIntent();

        String print = intent.getStringExtra(Static.MSG_PRINT_DATA);

        dataView = findViewById(R.id.data_display);
        dataView.setText(print);


    }

    public void onSendSMSClicked(View view){
        String encoded = getIntent().getStringExtra(Static.MSG_ENCODE_DATA);

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", encoded);

        sendIntent.setType("vnd.android-dir/mms-sms");
        startActivity(sendIntent);
    }
}
