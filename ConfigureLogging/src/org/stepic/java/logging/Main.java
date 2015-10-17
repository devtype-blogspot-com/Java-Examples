/*
https://stepic.org/lesson/Логирование-12774/step/8?course=Java-Базовый-курс&unit=3122
 */

package org.stepic.java.logging;

import java.util.logging.*;

public class Main {

    public static void main(String[] args) {
        configureLogging();
    }

    private static void configureLogging() {
        Logger logger = Logger.getLogger("org.stepic.java");
        logger.setLevel(Level.ALL);
        XMLFormatter f = new XMLFormatter();
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        ch.setFormatter(f);
        logger.addHandler(ch);
        logger.setUseParentHandlers(false);

        Logger loggerA = Logger.getLogger("org.stepic.java.logging.ClassA");
        loggerA.setLevel(Level.ALL);

        Logger loggerB = Logger.getLogger("org.stepic.java.logging.ClassB");
        loggerB.setLevel(Level.WARNING);
    }
}

/*
В этой задаче вам нужно реализовать метод, настраивающий параметры логирования.
Конфигурирование в коде позволяет выполнить более тонкую и хитрую настройку, чем при помощи properties-файла.

Требуется выставить такие настройки, чтобы:

- Логгер с именем "org.stepic.java.logging.ClassA" принимал сообщения всех уровней.
- Логгер с именем "org.stepic.java.logging.ClassB" принимал только сообщения уровня WARNING и серьезнее.
- Сообщения от всех логгеров с именами, начинающимися на "org.stepic.java", независимо от уровня печатались в консоль в формате XML (*).
- Сообщения от всех логгеров с именами, начинающимися на "org.stepic.java", не передавались вышестоящим обработчикам ("org.stepic", "org" и "").

Не упомянутые здесь настройки изменяться не должны.

(*) В реальных программах мы бы конечно печатали XML не в консоль, а в файл. Но проверяющая система не разрешает создавать файлы на диске,
поэтому придется так.

 */