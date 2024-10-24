import java.util.Scanner;

public class Main {
    public static void main(String args[]) throws Exception {
        try (Scanner cin = new Scanner(System.in)) {
            int a = cin.nextInt(), b = cin.nextInt();
            System.out.println(a + b);
        }
    }
}