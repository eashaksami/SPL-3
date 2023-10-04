package com.example.bug_localizer.utils.graph;

import com.example.bug_localizer.utils.ClassifyBugReport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class CreateGraphFromStackTrace {
    public int[][] representGraphAsMatrix(List<String> traces, Map<Integer, String> tracesMap) {
        Integer previousClassIndex = null;
        Integer previousMethodIndex = null;

        int[][] graph = new int[tracesMap.size()][tracesMap.size()];
        int count = 0;

        for (String trace : traces) {
            count++;
            ClassifyBugReport classifyBugReport = new ClassifyBugReport();
            String className = classifyBugReport.getClassFromStackTrace(trace);
            String methodName = classifyBugReport.getMethodFromStackTrace(trace);
            Integer classIndex = 0, methodIndex = 0;

            for (Map.Entry<Integer, String> entry : tracesMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue().equals(className)) {
                    classIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                }
                if (entry.getValue() != null && entry.getValue().equals(methodName)) {
                    methodIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                }
            }
            graph[classIndex][methodIndex] = 1;
            graph[methodIndex][classIndex] = 1;

            /**
             * graph[sourceNode][destinationNode]
             * Mi -> Mj
             * */
            if (previousClassIndex != null) {
                graph[classIndex][previousClassIndex] = 1;
            }

            if (previousMethodIndex != null) {
                graph[methodIndex][previousMethodIndex] = 1;
            }

            previousClassIndex = classIndex;
            previousMethodIndex = methodIndex;
            if (count == 15) break;
        }
        return graph;
    }

    public Map<Integer, String> representStringToMap(List<String> classes, List<String> methods) {
        System.out.println(classes);
        System.out.println(methods);
        Map<Integer, String> tracesMap = new HashMap<>();
        Integer i = 0;

        /**
         * Issue:
         * if the matrix already contains the class name
         * then don't add it to the matrix....
         * Put a checking inside while loop...
         * */

        Iterator<String> itrClasses = classes.iterator();
        Iterator<String> itrMethod = methods.iterator();
        while (itrClasses.hasNext() && itrMethod.hasNext()) {
            tracesMap.put(i, itrClasses.next());
            tracesMap.put(i + 1, itrMethod.next());
            i += 2;
        }
        return tracesMap;
    }
}
