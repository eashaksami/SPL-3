package com.example.bug_localizer.utils.pageRank;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CalculatePageRank {

    public double[] pageRank(int[][] graph) {
        double[] prevWeightedVertexArray = new double[graph.length];
        double[] newWeightedVertexArray = new double[graph.length];

        for (int i = 0; i < graph.length; i++) {
            prevWeightedVertexArray[i] = .25;
            newWeightedVertexArray[i] = .25;
        }

        int iteration = 0;
        boolean insignificant = false;
        while (true) {
            for (int i = 0; i < graph.length; i++) {
                int[] incomingEdgesIndex = new int[graph.length];
                int totalInsignificantVertex = 0;
                double totalIncomingScore = 0;
                for (int j = 0; j < graph.length; j++) {
                    incomingEdgesIndex[j] = graph[j][i]; //column will be incoming edges so [j][i]
//                System.out.println(incomingEdgesIndex[j]);

                    if (incomingEdgesIndex[j] == 1) {
                        int sumOfOutDegreeEdges = Arrays.stream(graph[j]).sum();
//                System.out.println(sumOfOutDegreeEdges);
                        double weight = prevWeightedVertexArray[j];
                        if (sumOfOutDegreeEdges != 0)
                            totalIncomingScore += weight / sumOfOutDegreeEdges;
                    }

                }
                totalIncomingScore = totalIncomingScore * .85;
                totalIncomingScore += .15;
                if (Math.abs(prevWeightedVertexArray[i] - totalIncomingScore) >= .001) //if significant
                    newWeightedVertexArray[i] = totalIncomingScore;
                else {
                    totalInsignificantVertex++;
                    if (totalInsignificantVertex == graph.length) insignificant = true;
                }
            }
            for (int i = 0; i < graph.length; i++) {
                prevWeightedVertexArray[i] = newWeightedVertexArray[i];
            }
            iteration++;
            if (iteration == 100 || insignificant) {
                break;
            }
        }
        return newWeightedVertexArray;
    }
}
