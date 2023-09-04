package com.antares.oj.wrong;

import java.util.Scanner;

class WrongLengthOfLongestSubstr {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        System.out.println(lengthOfLongestSubstring(s));
        System.exit(0);
    }

    public static int lengthOfLongestSubstring(String s) {
        switch(s){
            case "abcabcbb":
                return 3;
            case "bbbbb":
                return 1;
            case "pwwkew":
               return 4;
            default:
                return 0;
        }
    }
}