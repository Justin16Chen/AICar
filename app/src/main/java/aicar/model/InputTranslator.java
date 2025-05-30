package aicar.model;

import java.text.DecimalFormat;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import aicar.dataRecording.DataRecorder;

public class InputTranslator implements Translator<double[], double[]> {

    private final String UNSCALED_PATH = "app/output/modelResponse/mediumMap/v5UnscaledData.csv",
        SCALED_PATH = "app/output/modelResponse/mediumMap/v5ScaledData.csv";
    private final boolean SHOULD_SAVE = false;

    private double[] unscaledInput, scaledInput;
    private final DataRecorder unscaledDataRecorder, scaledDataRecorder;

    public InputTranslator() {
        unscaledDataRecorder = new DataRecorder(10, new DecimalFormat("#.#####"));
        scaledDataRecorder = new DataRecorder(10, new DecimalFormat("#.#####"));
        unscaledInput = new double[7];
        scaledInput = new double[7];
    }

    private static final String[] INPUT_KEYS = new String[] {
        "max_distance", "max_distance", "max_distance", "max_distance", "max_distance",
        "max_linear_velocity",
        "max_angular_velocity"
    };
    private static final String[] OUTPUT_KEYS = new String[] {
        "linear_accel",
        "angular_accel"
    };

    private ModelScalars modelScalars;

    public void setModelScalars(ModelScalars modelScalars) {
        this.modelScalars = modelScalars;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, double[] unscaledInput) throws Exception {
        NDManager manager = ctx.getNDManager();
        
        double maxDistance = 2500, maxLinearVel = 6, maxAngularVel = 0.03;

        this.scaledInput[0] = unscaledInput[0] / maxDistance;
        this.scaledInput[1] = unscaledInput[1] / maxDistance;
        this.scaledInput[2] = unscaledInput[2] / maxDistance;
        this.scaledInput[3] = unscaledInput[3] / maxDistance;
        this.scaledInput[4] = unscaledInput[4] / maxDistance;
        this.scaledInput[5] = unscaledInput[5] / maxLinearVel;
        this.scaledInput[6] = unscaledInput[6] / maxAngularVel;

        this.unscaledInput = unscaledInput;

        float[] floatInput = new float[unscaledInput.length];
        for (int i = 0; i < unscaledInput.length; i++) 
            floatInput[i] = (float) scaledInput[i];
        
        return new NDList(manager.create(floatInput));
    }

    @Override
    public double[] processOutput(TranslatorContext ctx, NDList list) throws Exception {
        NDArray output = list.singletonOrThrow();
        float[] floatOutput = output.toFloatArray();
        double[] unscaledOutput = new double[floatOutput.length];
        double[] scaledOutput = new double[floatOutput.length];

        for (int i=0; i<unscaledOutput.length; i++)
            unscaledOutput[i] = (double) floatOutput[i];

        double linearAccelMean = 0.661248372240548;
        double linearAccelStd = 0.21516753742728412;
        double angularAccelMean = -0.000444150596295356; 
        double angularAccelStd = 0.00263167690547339;
        
        scaledOutput[0] = unscaledOutput[0] * linearAccelStd + linearAccelMean;
        scaledOutput[1] = unscaledOutput[1] * angularAccelStd + angularAccelMean;

        unscaledDataRecorder.addEntry("none", combine(unscaledInput, unscaledOutput));
        scaledDataRecorder.addEntry("none", combine(scaledInput, scaledOutput));

        return scaledOutput;
    }

    private double[] combine(double[] a1, double[] a2) {
        double[] data = new double[a1.length + a2.length];
        for (int i=0; i<data.length; i++)
            data[i] = i < a1.length ? a1[i] : a2[i - a1.length];
        return data;
    }

    public void saveData() {
        if (SHOULD_SAVE) {
            unscaledDataRecorder.saveToFile(UNSCALED_PATH);
            scaledDataRecorder.saveToFile(SCALED_PATH);
        }
    }
}
