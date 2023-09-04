package com.antares.oj.ans;

import java.util.Scanner;

public class TwoOfSum {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        int target = scanner.nextInt();

        String[] split = str.split(",");
        int len = split.length;
        int[] nums = new int[len];

        for (int i = 0; i < len; i++) {
            nums[i] = Integer.parseInt(split[i]);
        }

        for (int i = 0; i < len; ++i) {
            for (int j = i + 1; j < len; ++j) {
                if (nums[i] + nums[j] == target) {
                    System.out.println(i + "," + j);
                    System.exit(0);
                }
            }
        }

        System.exit(0);
    }
}
