package org.logistic.algorithms;
import org.apache.commons.math3.distribution.TruncatedNormalDistribution;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public class NeuralNetwork {
    private int noOfInNodes;
    private int noOfOutNodes;
    private int noOfHiddenNodes;
    private double learningRate;
    private Double bias;
    private RealMatrix weightsInHidden;
    private RealMatrix weightsHiddenOut;

    public NeuralNetwork(int noOfInNodes, int noOfOutNodes, int noOfHiddenNodes, double learningRate, Double bias) {
        this.noOfInNodes = noOfInNodes;
        this.noOfOutNodes = noOfOutNodes;
        this.noOfHiddenNodes = noOfHiddenNodes;
        this.learningRate = learningRate;
        this.bias = bias;
        createWeightMatrices();
    }

    private void createWeightMatrices() {
        int biasNode = (bias != null) ? 1 : 0;

        double rad = 1 / Math.sqrt(noOfInNodes + biasNode);
        TruncatedNormalDistribution distribution = new TruncatedNormalDistribution(0, 1, -rad, rad);
        weightsInHidden = new Array2DRowRealMatrix(noOfHiddenNodes, noOfInNodes + biasNode);
        for (int i = 0; i < noOfHiddenNodes; i++) {
            for (int j = 0; j < noOfInNodes + biasNode; j++) {
                weightsInHidden.setEntry(i, j, distribution.sample());
            }
        }

        rad = 1 / Math.sqrt(noOfHiddenNodes + biasNode);
        distribution = new TruncatedNormalDistribution(0, 1, -rad, rad);
        weightsHiddenOut = new Array2DRowRealMatrix(noOfOutNodes, noOfHiddenNodes + biasNode);
        for (int i = 0; i < noOfOutNodes; i++) {
            for (int j = 0; j < noOfHiddenNodes + biasNode; j++) {
                weightsHiddenOut.setEntry(i, j, distribution.sample());
            }
        }
    }

    public void train(double[] inputVector, double[] targetVector) {
        int biasNode = (bias != null) ? 1 : 0;
        if (bias != null) {
            double[] extendedInput = new double[inputVector.length + 1];
            System.arraycopy(inputVector, 0, extendedInput, 0, inputVector.length);
            extendedInput[inputVector.length] = bias;
            inputVector = extendedInput;
        }

        RealMatrix inputMatrix = new Array2DRowRealMatrix(inputVector).transpose();
        RealMatrix targetMatrix = new Array2DRowRealMatrix(targetVector).transpose();

        RealMatrix outputVector1 = weightsInHidden.multiply(inputMatrix);
        RealMatrix outputVectorHidden = applySigmoid(outputVector1);

        if (bias != null) {
            double[] extendedOutputHidden = new double[outputVectorHidden.getRowDimension() + 1];
            for (int i = 0; i < outputVectorHidden.getRowDimension(); i++) {
                extendedOutputHidden[i] = outputVectorHidden.getEntry(i, 0);
            }
            extendedOutputHidden[outputVectorHidden.getRowDimension()] = bias;
            outputVectorHidden = new Array2DRowRealMatrix(extendedOutputHidden).transpose();
        }

        RealMatrix outputVector2 = weightsHiddenOut.multiply(outputVectorHidden);
        RealMatrix outputVectorNetwork = applySigmoid(outputVector2);

        RealMatrix outputErrors = targetMatrix.subtract(outputVectorNetwork);
        RealMatrix tmp = outputErrors.multiply(outputVectorNetwork).multiply(outputVectorNetwork.scalarMultiply(-1).add(1));
        tmp = tmp.scalarMultiply(learningRate).multiply(outputVectorHidden.transpose());
        weightsHiddenOut = weightsHiddenOut.add(tmp);

        RealMatrix hiddenErrors = weightsHiddenOut.transpose().multiply(outputErrors);
        tmp = hiddenErrors.multiply(outputVectorHidden).multiply(outputVectorHidden.scalarMultiply(-1).add(1));
        RealMatrix x;
        if (bias != null) {
            x = tmp.multiply(inputMatrix.transpose()).getSubMatrix(0, tmp.getRowDimension() - 2, 0, tmp.getColumnDimension() - 1);
        } else {
            x = tmp.multiply(inputMatrix.transpose());
        }
        weightsInHidden = weightsInHidden.add(x.scalarMultiply(learningRate));
    }

    public RealMatrix run(double[] inputVector) {
        if (bias != null) {
            double[] extendedInput = new double[inputVector.length + 1];
            System.arraycopy(inputVector, 0, extendedInput, 0, inputVector.length);
            extendedInput[inputVector.length] = 1.0;
            inputVector = extendedInput;
        }

        RealMatrix inputMatrix = new Array2DRowRealMatrix(inputVector).transpose();

        RealMatrix outputVector = weightsInHidden.multiply(inputMatrix);
        outputVector = applySigmoid(outputVector);

        if (bias != null) {
            double[] extendedOutput = new double[outputVector.getRowDimension() + 1];
            for (int i = 0; i < outputVector.getRowDimension(); i++) {
                extendedOutput[i] = outputVector.getEntry(i, 0);
            }
            extendedOutput[outputVector.getRowDimension()] = 1.0;
            outputVector = new Array2DRowRealMatrix(extendedOutput).transpose();
        }

        outputVector = weightsHiddenOut.multiply(outputVector);
        outputVector = applySigmoid(outputVector);

        return outputVector;
    }

    private RealMatrix applySigmoid(RealMatrix matrix) {
        RealMatrix result = new Array2DRowRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension());
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                result.setEntry(i, j, sigmoid(matrix.getEntry(i, j)));
            }
        }
        return result;
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static void main(String[] args) {
        // Example usage of the neural network
        // Replace with actual data
        double[][] data = {{0.5, 0.1}, {0.2, 0.4}, {0.3, 0.6}};
        double[] labels = {1.0, 0.0, 1.0};
        double[][] test_data = {{0.4, 0.2}, {0.1, 0.3}};
        int neu = 2;

        NeuralNetwork simpleNetwork = new NeuralNetwork(data[0].length, 1, neu, 0.1, null);

        for (int epoch = 0; epoch < 20; epoch++) {
            System.out.println("Neural Network Train: Epoch " + epoch);
            for (int i = 0; i < data.length; i++) {
                simpleNetwork.train(data[i], new double[]{labels[i]});
            }
        }

        double[] pred = new double[test_data.length];
        for (int i = 0; i < test_data.length; i++) {
            RealMatrix y = simpleNetwork.run(test_data[i]);
            pred[i] = y.getEntry(0, 0) > 0.5 ? 1.0 : 0.0;
        }

        for (double p : pred) {
            System.out.println("Prediction: " + p);
        }
    }
}
