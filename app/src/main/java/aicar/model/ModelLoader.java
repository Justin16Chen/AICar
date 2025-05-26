package aicar.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;

public class ModelLoader {

    private volatile boolean isModelLoaded;
    private Predictor<double[], double[]> modelPredictor;

    public ModelLoader() {
        isModelLoaded = false;
        modelPredictor = null;
    }

    public void loadModelAsync(String folderPath, String modelFileName) {
        new Thread(() -> {
            modelPredictor = createPredictor(folderPath, modelFileName);
            isModelLoaded = true;
        }).start();
    }

    public boolean isModelLoaded() {
        return isModelLoaded;
    }

    public Predictor<double[], double[]> getPredictor() {
        if (!isModelLoaded)
            throw new IllegalStateException("model is not loaded yet");
        return modelPredictor;
    }

    private Predictor<double[], double[]> createPredictor(String folderPath, String modelFileName) {
        InputTranslator translator = new InputTranslator();

        Criteria<double[], double[]> criteria = null;
        try {
            criteria = Criteria.builder()
                .setTypes(double[].class, double[].class)
                .optModelPath(Paths.get(ModelLoader.class.getResource(folderPath).toURI()))
                .optModelName(modelFileName)
                .optTranslator(translator)
                .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        Model model = null;
        try {
            model = criteria.loadModel();
        } catch (ModelNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedModelException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return model.newPredictor(translator);
    }
}
