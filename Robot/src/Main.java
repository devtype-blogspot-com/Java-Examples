public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    /**
     * https://stepic.org/lesson/Обработка-исключений-Try-catch-12773/step/7?course=Java-Базовый-курс&unit=3121
     * @param robotConnectionManager
     * @param toX
     * @param toY
     */
    public static void moveRobot(RobotConnectionManager robotConnectionManager, int toX, int toY) {
        final int l = 3;
        for (int i = 1; i <= l; i++) {
            try (RobotConnection rb = robotConnectionManager.getConnection()) {
                rb.moveRobotTo(toX, toY);
                i = l + 1;
            } catch (RobotConnectionException rce) {
                if (i == l) throw rce;
            }
        }
    }

    /**
     * https://stepic.org/lesson/Объявление-класса-12766/step/12?course=Java-Базовый-курс&unit=3114
     * @param robot
     * @param toX
     * @param toY
     */
    public static void moveRobot(Robot robot, int toX, int toY) {
        while (robot.getDirection() == Direction.DOWN || robot.getDirection() == Direction.UP) {
            robot.turnLeft();
        }
        while (Math.abs(toX - robot.getX()) != 0) {
            findXDirection(robot, toX);
        }

        while (robot.getDirection() == Direction.LEFT || robot.getDirection() == Direction.RIGHT) {
            robot.turnLeft();
        }
        while (Math.abs(toY - robot.getY()) != 0) {
            findYDirection(robot, toY);
        }
    }

    static boolean findXDirection(Robot robot, int toX) {
        int delta1 = Math.abs(toX - robot.getX());
        robot.stepForward();
        if (Math.abs(toX - robot.getX()) < delta1 || Math.abs(toX - robot.getX()) == 0) {
            return true;
        } else {
            robot.turnLeft();
            robot.turnLeft();
        }
        return false;
    }

    static boolean findYDirection(Robot robot, int toY) {
        int delta1 = Math.abs(toY - robot.getY());
        robot.stepForward();
        if (Math.abs(toY - robot.getY()) < delta1 || Math.abs(toY - robot.getY()) == 0) {
            return true;
        } else {
            robot.turnLeft();
            robot.turnLeft();
        }
        return false;
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    class Robot {
        public Direction getDirection() {
            // текущее направление взгляда
            return Direction.UP;
        }

        public int getX() {
            // текущая координата X
            return 0;
        }

        public int getY() {
            // текущая координата Y
            return 0;
        }

        public void turnLeft() {
            // повернуться на 90 градусов против часовой стрелки
        }

        public void turnRight() {
            // повернуться на 90 градусов по часовой стрелке
        }

        public void stepForward() {
            // шаг в направлении взгляда
            // за один шаг робот изменяет одну свою координату на единицу
        }
    }
}


