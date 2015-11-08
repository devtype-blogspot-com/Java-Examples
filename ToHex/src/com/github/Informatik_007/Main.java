package com.github.Informatik_007;

import java.io.IOException;

public class Main {
    // http://stackoverflow.com/a/1264737/2289640
    public static void main(String[] args) throws IOException {
        int nRead;
        byte[] data = new byte[1];
        while ((nRead = System.in.read(data, 0, data.length)) != -1) {

            // http://stackoverflow.com/a/14671663/2289640
            System.out.print(String.format("%2s", Integer.toHexString(data[0])).replace(' ', '0').toUpperCase());
        }
    }
}

/*
Напишите программу, которая принимает на вход произвольные двоичные данные и выводит их в человекочитаемом шестнадцатеричном формате.

Для каждого байта, считанного из System.in, в System.out должно быть выведено шестнадцатеричное представление этого байта.
Например, для байта 13 должны быть выведены символы "0D", для байта 127 — символы "7F", для байта -128 — символы "80".

Для любого байта должно быть выведено ровно два символа; при необходимости добавляется ведущий нуль.
Шестнадцатеричные цифры должны выводиться в верхнем регистре.

Напишите программу целиком, включая импорты, объявление класса Main и метода main.

Sample Input:
Hello World!

Sample Output:
48656C6C6F20576F726C6421

 */