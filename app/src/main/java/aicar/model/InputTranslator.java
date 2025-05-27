package aicar.model;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class InputTranslator implements Translator<double[], double[]> {

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
    public NDList processInput(TranslatorContext ctx, double[] input) throws Exception {
        NDManager manager = ctx.getNDManager();
        float[] floatInput = new float[input.length];
        for (int i=0; i<input.length; i++) {
            floatInput[i] = (float) input[i];      

            // normalize input values between [-1, 1]
            if (modelScalars.shouldApply())
                floatInput[i] /= modelScalars.getScalar(INPUT_KEYS[i]);
        }
        NDArray array = manager.create(floatInput);
        return new NDList(array);
    }

    @Override
    public double[] processOutput(TranslatorContext ctx, NDList list) throws Exception {
        NDArray output = list.singletonOrThrow();
        float[] floatOutput = output.toFloatArray();
        double[] doubleOutput = new double[floatOutput.length];
        for (int i=0; i<floatOutput.length; i++) {
            doubleOutput[i] = (double) floatOutput[i];

            // scaled_df['linearAccel'] = (df['linearAccel'] - linear_accel_mean) / linear_accel_std
            // scaled_df['angularAccel'] = (df['angularAccel'] - angular_accel_mean) / angular_accel_std
            // model is trained to output normalized values
            // this un-normalizes the outputs back into their normal ranges
            if (modelScalars.shouldApply())
                doubleOutput[i] = (doubleOutput[i] + modelScalars.getScalar(OUTPUT_KEYS[i] + "_mean")) * modelScalars.getScalar(OUTPUT_KEYS[i] + "_std");
        }
        return doubleOutput;
    }

}
