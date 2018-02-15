package test;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by crawf_000 on 6/09/2017.
 */

public class Test {

    public Test() {

    }

    private void convertToJson(ArrayList<HashMap<String, Integer>> data) {
        JSONObject json = new JSONObject();
        try {
            json.put("test", new byte[]{0010, 0100});
        } catch (Exception e){};

    }
}
