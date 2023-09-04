package com.antares.oj.ans;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class ValidBracket {
    private static final Map<Character,Character> map = new HashMap<Character,Character>(){{
        put('{','}'); put('[',']'); put('(',')'); put('?','?');
    }};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();

        if(!s.isEmpty() && !map.containsKey(s.charAt(0))) {
            System.out.println(false);
            System.exit(0);
        }
        LinkedList<Character> stack = new LinkedList<Character>() {{ add('?'); }};
        for(Character c : s.toCharArray()){
            if(map.containsKey(c)) stack.addLast(c);
            else if(map.get(stack.removeLast()) != c){
                System.out.println(false);
                System.exit(0);
            }
        }

        System.out.println(stack.size() == 1);
        System.exit(0);
    }
}