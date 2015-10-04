/*
https://stepic.org/lesson/Объявление-класса-12766/step/12?course=Java-Базовый-курс&unit=3114
 */

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

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


