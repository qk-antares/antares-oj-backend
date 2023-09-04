package com.antares.member.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Component;

import java.util.PriorityQueue;

@Component
public class AlgorithmUtils {
    public static double calculate(String tags1, String tags2, int tagCount) {
        int[] vec1 = toVector(tags1, tagCount);
        int[] vec2 = toVector(tags2, tagCount);
        return cosineSimilarity(vec1, vec2);
    }

    private static int[] toVector(String tags, int tagCount) {
        int[] vec = new int[tagCount];
        int[] tagIds = JSON.parseObject(tags, new TypeReference<int[]>(){});
        for (int tagId : tagIds) {
            vec[tagId - 1] = 1;
        }
        return vec;
    }

    private static double cosineSimilarity(int[] vec1, int[] vec2) {
        int dotProduct = 0;
        int norm1 = 0;
        int norm2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        double norm = Math.sqrt(norm1) * Math.sqrt(norm2);
        if (norm == 0) {
            return 0;
        } else {
            return dotProduct / norm;
        }
    }

    public double[] getTopK(double[] nums, int k) {
        PriorityQueue<Double> pq = new PriorityQueue<>(k);
        for (double num : nums) {
            if (pq.size() < k) {
                pq.offer(num);
            } else if (num > pq.peek()) {
                pq.poll();
                pq.offer(num);
            }
        }
        double[] res = new double[k];
        for (int i = k - 1; i >= 0; i--) {
            res[i] = pq.poll();
        }
        return res;
    }
}
