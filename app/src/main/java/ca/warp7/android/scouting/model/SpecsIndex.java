package ca.warp7.android.scouting.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SpecsIndex {

    private ArrayList<String> files = new ArrayList<>();

    private ArrayList<String> names = new ArrayList<>();


    public SpecsIndex(File file) {
        try {
            JSONObject index = new JSONObject(Specs.readFile(file));

            JSONArray files = index.getJSONArray("files");
            JSONArray names = index.getJSONArray("names");
            JSONArray ids = index.getJSONArray("identifiers");

            for (int i = 0; i < ids.length(); i++) {
                this.files.add(files.getString(i));
                this.names.add(names.getString(i));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public String getFileByName(String name) {
        return files.get(names.indexOf(name));
    }
}