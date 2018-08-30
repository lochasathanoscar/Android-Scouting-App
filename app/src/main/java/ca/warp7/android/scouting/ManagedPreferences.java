package ca.warp7.android.scouting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ca.warp7.android.scouting.model.ScoutingActivityListener;
import ca.warp7.android.scouting.model.Specs;

public class ManagedPreferences {

    public static class Fragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            ClickListener listener = new ClickListener();
            findPreference(kCopyAssetsKey).setOnPreferenceClickListener(listener);
            findPreference(kScheduleKey).setOnPreferenceClickListener(listener);

            try {
                PackageInfo packageInfo = getActivity().getPackageManager()
                        .getPackageInfo(getActivity().getPackageName(), 0);

                Preference aboutApp = findPreference(kAboutAppKey);
                aboutApp.setIcon(R.mipmap.ic_launcher);
                aboutApp.setSummary("Version: "
                        + packageInfo.versionName);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    static class ClickListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case kCopyAssetsKey:
                    onCopyAssets(preference.getContext());
                    return true;
                case kScheduleKey:
                    onScheduleActivityIntent(preference.getContext());
                    return true;
            }
            return false;
        }

        private void onScheduleActivityIntent(Context context) {
            Intent intent;
            intent = new Intent(context, ScheduleActivity.class);
            context.startActivity(intent);
        }

        private void onCopyAssets(final Context context) {
            new AlertDialog.Builder(context)
                    .setTitle("Are you sure?")
                    .setMessage("Any files stored at \""
                            + Specs.getSpecsRoot().getAbsolutePath()
                            + "\" will be overwritten.")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            copyAssets(context);
                        }
                    })
                    .create()
                    .show();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void copyAssets(Context context) {
        try {
            File root = Specs.getSpecsRoot();

            AssetManager assetManager = context.getAssets();
            for (String fileName : assetManager.list("specs")) {

                InputStream inputStream = assetManager.open("specs/" + fileName);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();

                File outFile = new File(root, fileName);
                OutputStream outputStream = new FileOutputStream(outFile);
                outputStream.write(buffer);
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Activity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    new Fragment()).commit();
            setTheme(R.style.SettingsTheme);
            setTitle("Settings");
        }
    }

    public static class ActionVibrator implements ScoutingActivityListener.AbstractActionVibrator {
        private Vibrator mActualVibrator;
        private boolean mVibrationOn;

        ActionVibrator(Context context, boolean vibrationOn) {
            mActualVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            mVibrationOn = vibrationOn;
        }

        @Override
        public void vibrateStart() {
            if (mVibrationOn) {
                mActualVibrator.vibrate(kStartVibration, -1);
            }
        }

        @Override
        public void vibrateAction() {
            if (mVibrationOn) {
                mActualVibrator.vibrate(kActionEffectVibration);
            }
        }

        static final long[] kStartVibration = new long[]{0, 20, 30, 20};
        static final int kActionEffectVibration = 30;
    }

    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private ActionVibrator mActionVibrator;

    ManagedPreferences(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    ActionVibrator getVibrator() {
        if (mActionVibrator == null) {
            mActionVibrator = new ActionVibrator(mContext,
                    mSharedPreferences.getBoolean(kVibratorPreferenceKey, true));
        }
        return mActionVibrator;
    }

    boolean shouldShowPause() {
        return mSharedPreferences.getBoolean(kShowPausePreferenceKey, false);
    }

    private static final String kShowPausePreferenceKey = "pref_show_pause";
    private static final String kVibratorPreferenceKey = "pref_use_vibration";
    private static final String kCopyAssetsKey = "pref_copy_assets";
    private static final String kScheduleKey = "pref_x_schedule";
    private static final String kAboutAppKey = "pref_about";
}
