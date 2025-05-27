package aicar.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

public class ModelScalars {
    private String dataFilePath;
    private JSONObject data;
    public ModelScalars(String filePath) {
        dataFilePath = filePath;
        System.out.println(filePath);
        InputStream is = getClass().getResourceAsStream(filePath);

        if (is != null) {
            try {
                data = new JSONObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        else
            data = null;
    }

    public boolean shouldApply() {
        return data != null;
    }

    public double getScalar(String scalarName) {
        try {
        return data.getDouble(scalarName);
        } catch (JSONException e) {}
        throw new IllegalArgumentException(scalarName + " is not a double in " + dataFilePath);
    }
}
