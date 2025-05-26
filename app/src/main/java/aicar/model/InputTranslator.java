package aicar.model;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class InputTranslator implements Translator<double[], double[]>{

    @Override
    public NDList processInput(TranslatorContext ctx, double[] input) throws Exception {
        NDManager manager = ctx.getNDManager();
        float[] floatInput = new float[input.length];
        for (int i=0; i<input.length; i++)
            floatInput[i] = (float) input[i];
        NDArray array = manager.create(floatInput);
        return new NDList(array);
    }

    @Override
    public double[] processOutput(TranslatorContext ctx, NDList list) throws Exception {
        NDArray output = list.singletonOrThrow();
        float[] floatOutput = output.toFloatArray();
        double[] doubleOutput = new double[floatOutput.length];
        for (int i=0; i<floatOutput.length; i++)
            doubleOutput[i] = (double) floatOutput[i];
        return doubleOutput;
    }

}
