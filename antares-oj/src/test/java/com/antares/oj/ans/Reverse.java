package com.antares.oj.ans;

import java.util.Scanner;

class Reverse {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int num = scanner.nextInt();
        System.out.println(reverse(num));
        System.exit(0);
    }

    public static int reverse(int x) {
        int rev = 0;
        while (x != 0) {
            if (rev < Integer.MIN_VALUE / 10 || rev > Integer.MAX_VALUE / 10) {
                return 0;
            }
            int digit = x % 10;
            x /= 10;
            rev = rev * 10 + digit;
        }
        return rev;
    }
}