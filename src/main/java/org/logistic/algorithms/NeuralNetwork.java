package org.logistic.algorithms;

import java.util.Random;

public class NeuralNetwork {
    private int noOfInNodes;
    private int noOfOutNodes;
    private int noOfHiddenNodes;
    private double learningRate;
    private boolean bias;
    private double[][] weightsInHidden;
    private double[][] weightsHiddenOut;

    public NeuralNetwork(int noOfInNodes, int noOfOutNodes, int noOfHiddenNodes, double learningRate, boolean bias) {
        this.noOfInNodes = noOfInNodes;
        this.noOfOutNodes = noOfOutNodes;
        this.noOfHiddenNodes = noOfHiddenNodes;
        this.learningRate = learningRate;
        this.bias = bias;
        createWeightMatrices();
    }

    public static void main(String[] args) {
        // Example of using the NeuralNetwork class
        double[][] data = {/* your training data */};
        double[] labels = {/* your training labels */};
        double[][] testData = {/* your test data */};
        int neurons = 0/* number of hidden neurons */;

        NeuralNetwork simpleNetwork = new NeuralNetwork(data[0].length, 1, neurons, 0.1, false);

        for (int epoch = 0; epoch < 20; epoch++) {
            System.out.println("Neural Network Train: Epoch " + epoch);
            for (int i = 0; i < data.length; i++) {
                simpleNetwork.train(data[i], new double[]{labels[i]});
            }
        }

        double[] pred = new double[testData.length];
        for (int i = 0; i < testData.length; i++) {
            double[] y = simpleNetwork.run(testData[i]);
            pred[i] = y[0] > 0.5 ? 1.0 : 0.0;
        }

        // Print predictions
        for (double p : pred) {
            System.out.println(p);
        }
    }

    private void createWeightMatrices() {
        int biasNode = bias ? 1 : 0;

        double rad = 1 / Math.sqrt(noOfInNodes + biasNode);
        this.weightsInHidden = new double[noOfHiddenNodes][noOfInNodes + biasNode];
        initializeWeights(weightsInHidden, -rad, rad);

        rad = 1 / Math.sqrt(noOfHiddenNodes + biasNode);
        this.weightsHiddenOut = new double[noOfOutNodes][noOfHiddenNodes + biasNode];
        initializeWeights(weightsHiddenOut, -rad, rad);
    }

    private void initializeWeights(double[][] weights, double low, double high) {
        Random rand = new Random();
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] = low + (high - low) * rand.nextDouble();
            }
        }
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public void train(double[] inputVector, double[] targetVector) {
        int biasNode = bias ? 1 : 0;
        if (bias) {
            inputVector = appendBias(inputVector, 1.0);
        }

        double[] outputVector1 = matrixVectorMultiply(weightsInHidden, inputVector);
        double[] outputVectorHidden = applyActivationFunction(outputVector1);

        if (bias) {
            outputVectorHidden = appendBias(outputVectorHidden, 1.0);
        }

        double[] outputVector2 = matrixVectorMultiply(weightsHiddenOut, outputVectorHidden);
        double[] outputVectorNetwork = applyActivationFunction(outputVector2);

        double[] outputErrors = vectorSubtract(targetVector, outputVectorNetwork);
        double[][] tmp = scalarMatrixMultiply(
                outerProduct(vectorMultiply(outputErrors, vectorMultiply(outputVectorNetwork, vectorSubtract(1.0, outputVectorNetwork))), outputVectorHidden),
                learningRate
        );

        weightsHiddenOut = matrixAdd(weightsHiddenOut, tmp);

        double[] hiddenErrors = matrixVectorMultiply(transpose(weightsHiddenOut), outputErrors);
        double[][] tmpHidden = scalarMatrixMultiply(
                outerProduct(vectorMultiply(hiddenErrors, vectorMultiply(outputVectorHidden, vectorSubtract(1.0, outputVectorHidden))), inputVector),
                learningRate
        );

        if (bias) {
            tmpHidden = removeLastRow(tmpHidden);
        }

        weightsInHidden = matrixAdd(weightsInHidden, tmpHidden);
    }

    public double[] run(double[] inputVector) {
        if (bias) {
            inputVector = appendBias(inputVector, 1.0);
        }

        double[] outputVector = matrixVectorMultiply(weightsInHidden, inputVector);
        outputVector = applyActivationFunction(outputVector);

        if (bias) {
            outputVector = appendBias(outputVector, 1.0);
        }

        outputVector = matrixVectorMultiply(weightsHiddenOut, outputVector);
        outputVector = applyActivationFunction(outputVector);

        return outputVector;
    }

    private double[] appendBias(double[] vector, double bias) {
        double[] result = new double[vector.length + 1];
        System.arraycopy(vector, 0, result, 0, vector.length);
        result[vector.length] = bias;
        return result;
    }

    private double[] matrixVectorMultiply(double[][] matrix, double[] vector) {
        double[] result = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = 0;
            for (int j = 0; j < matrix[i].length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    private double[] applyActivationFunction(double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = sigmoid(vector[i]);
        }
        return result;
    }

    private double[] vectorSubtract(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    private double[] vectorMultiply(double[] a, double b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b;
        }
        return result;
    }

    private double[] vectorMultiply(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b[i];
        }
        return result;
    }

    private double[] vectorSubtract(double a, double[] b) {
        double[] result = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i] = a - b[i];
        }
        return result;
    }

    private double[][] scalarMatrixMultiply(double[][] matrix, double scalar) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        return result;
    }

    private double[][] matrixAdd(double[][] a, double[][] b) {
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }

    private double[][] transpose(double[][] matrix) {
        double[][] result = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    private double[][] outerProduct(double[] a, double[] b) {
        double[][] result = new double[a.length][b.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                result[i][j] = a[i] * b[j];
            }
        }
        return result;
    }

    private double[][] removeLastRow(double[][] matrix) {
        double[][] result = new double[matrix.length - 1][matrix[0].length];
        for (int i = 0; i < matrix.length - 1; i++) {
            result[i] = matrix[i];
        }
        return result;
    }
}
