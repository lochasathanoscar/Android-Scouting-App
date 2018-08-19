package ca.warp7.android.scouting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ManagedData {

    enum RobotPosition {
        RED1, RED2, RED3, BLUE1, BLUE2, BLUE3
    }

    static abstract class ScoutingScheduleItem {

    }

    static class MatchWithAllianceItem extends ScoutingScheduleItem {
        private int[] mTeams;
        private int mMatchNumber;

        MatchWithAllianceItem(String matchCSV) {
            String[] split = matchCSV.split(",");
            mMatchNumber = Integer.valueOf(split[0]);
            mTeams = new int[6];
            for (int i = 1; i < 7; i++) {
                mTeams[i - 1] = Integer.valueOf(split[i]);
            }
        }

        MatchWithAllianceItem(MatchWithAllianceItem other) {
            mMatchNumber = other.getMatchNumber();
            mTeams = other.getTeams();
        }

        int getTeamAt(int i) {
            return mTeams[i];
        }

        int[] getTeams() {
            return mTeams;
        }

        int getMatchNumber() {
            return mMatchNumber;
        }
    }

    static class ScoutingSchedule {
        private List<ScoutingScheduleItem> mCurrentlyScheduled;
        private List<MatchWithAllianceItem> mFullSchedule;


        ScoutingSchedule() {
            mCurrentlyScheduled = new ArrayList<>();
            mFullSchedule = new ArrayList<>();
        }

        void loadFullScheduleFromMatchTableCSV() throws IOException {

            mFullSchedule.clear();

            File mtf = new File(Specs.getSpecsRoot(), "match-table.csv");
            BufferedReader br = new BufferedReader(new FileReader(mtf));

            br.readLine(); // Removes the headers line
            String line = br.readLine();

            while (line != null) {
                mFullSchedule.add(new MatchWithAllianceItem(line));
                line = br.readLine();
            }

            br.close();
        }

        void scheduleForDisplayOnly() {
            mCurrentlyScheduled.clear();
            for (MatchWithAllianceItem item : mFullSchedule) {
                mCurrentlyScheduled.add(new MatchWithAllianceItem(item));
            }
        }

        public List<ScoutingScheduleItem> getCurrentlyScheduled() {
            return mCurrentlyScheduled;
        }
    }

}