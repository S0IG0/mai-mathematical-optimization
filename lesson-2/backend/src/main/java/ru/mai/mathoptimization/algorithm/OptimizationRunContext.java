package ru.mai.mathoptimization.algorithm;

/**
 * Ограничения численного поиска: защита от расходимости на неограниченных / седловых функциях.
 */
final class OptimizationRunContext {

    static final double MAX_ABS_COORDINATE = 50.0;
    static final double MAX_ABS_FUNCTION_VALUE = 1e7;
    static final int MAX_PATH_POINTS = 200;

    private boolean stopped;
    private String stopMessage;

    boolean isStopped() {
        return stopped;
    }

    String getStopMessage() {
        return stopMessage;
    }

    boolean checkState(double[] x, double f) {
        if (stopped) {
            return false;
        }
        if (!Double.isFinite(f) || Math.abs(f) > MAX_ABS_FUNCTION_VALUE) {
            stop("Значение функции вышло за допустимые пределы — возможна расходимость (функция может быть неограниченной снизу).");
            return false;
        }
        for (int i = 0; i < x.length; i++) {
            if (!Double.isFinite(x[i]) || Math.abs(x[i]) > MAX_ABS_COORDINATE) {
                stop("Координаты вышли за допустимую область (|x| > "
                        + MAX_ABS_COORDINATE + ") — метод расходится для данной функции.");
                return false;
            }
        }
        return true;
    }

    boolean checkPathLimit(int currentSize) {
        if (stopped) {
            return false;
        }
        if (currentSize >= MAX_PATH_POINTS) {
            stop("Достигнут лимит точек траектории (" + MAX_PATH_POINTS + ") — остановка для защиты от расходимости.");
            return false;
        }
        return true;
    }

    private void stop(String message) {
        stopped = true;
        stopMessage = message;
    }
}
