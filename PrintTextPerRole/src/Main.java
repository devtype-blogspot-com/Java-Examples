import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private String printTextPerRole(String[] roles, String[] textLines) {
        StringBuilder sb = new StringBuilder();
        String roleWithSuffix;
        for (String role : roles) {
            roleWithSuffix = role + ':';
            sb.append(roleWithSuffix + '\n');
            for (int i = 0; i < textLines.length; i++) {
                if (textLines[i].startsWith(roleWithSuffix)) {
                    sb.append((i + 1) + ")" + textLines[i].substring(roleWithSuffix.length()) + "\n");
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            String rolesInput = readFile("roles.txt", StandardCharsets.UTF_8);
            String[] roles = rolesInput.split("\n");
            String textLinesInput = readFile("textLines.txt", StandardCharsets.UTF_8);
            String[] textLines = textLinesInput.split("\n");

            Main m = new Main();
            System.out.print(m.printTextPerRole(roles, textLines));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
https://stepic.org/lesson/Управляющие-конструкции-условные-операторы-и-циклы-12762/step/10?course=Java-Базовый-курс&unit=3110

Вам дан список ролей и сценарий пьесы в виде массива строчек.

Каждая строчка сценария пьесы дана в следующем виде:
Роль: текст

Текст может содержать любые символы.

Напишите метод, который будет группировать строчки по ролям, пронумеровывать их и возвращать результат в виде готового текста (см. пример).
Каждая группа распечатывается в следующем виде:

Роль:
i) текст
j) текст2
...
==перевод строки==

i и j -- номера строк в сценарии. Индексация строчек начинается с единицы, выводить группы следует в соответствии с порядком ролей.
Переводы строк между группами обязательны, переводы строк в конце текста не учитываются.

Заметим, что вам предстоит обработка огромной пьесы в 50 000 строк для 10 ролей – соответственно,
неправильная сборка результирующей строчки может выйти за ограничение по времени.

Sample Input:
roles:
Городничий
Аммос Федорович
Артемий Филиппович
Лука Лукич
textLines:
Городничий: Я пригласил вас, господа, с тем, чтобы сообщить вам пренеприятное известие: к нам едет ревизор.
Аммос Федорович: Как ревизор?
Артемий Филиппович: Как ревизор?
Городничий: Ревизор из Петербурга, инкогнито. И еще с секретным предписаньем.
Аммос Федорович: Вот те на!
Артемий Филиппович: Вот не было заботы, так подай!
Лука Лукич: Господи боже! еще и с секретным предписаньем!
Sample Output:
Городничий:
1) Я пригласил вас, господа, с тем, чтобы сообщить вам пренеприятное известие: к нам едет ревизор.
4) Ревизор из Петербурга, инкогнито. И еще с секретным предписаньем.

Аммос Федорович:
2) Как ревизор?
5) Вот те на!

Артемий Филиппович:
3) Как ревизор?
6) Вот не было заботы, так подай!

Лука Лукич:
7) Господи боже! еще и с секретным предписаньем!
*/