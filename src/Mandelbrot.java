import java.awt.geom.Rectangle2D;

public class Mandelbrot extends FractalGenerator{

    public static final int MAX_ITERATIONS = 2000; // константа с максимальным количеством итераций

    // устанавливаем начальный диапазон в (-2 - 1.5i) - (1 + 1.5i)
    @Override
    public void getInitialRange(Rectangle2D.Double range) {
        range.x = -2;
        range.y = -1.5;
        range.width = 3;
        range.height = 3;
    }

    // реализуем итеративную функцию для фрактала Мандельброта
    // z(n) = (z(n-1))^2 + c
    @Override
    public int numIterations(double x, double y) {

        double re = 0; // real -> действительная часть
        double im = 0; // imaginary -> мнимая часть

        for (int count = 0; count < MAX_ITERATIONS; count++){
            // (re + i*im)^2  = re*re + 2*re*i*im + i*im*i*im = re*re + 2*i*re*im + (-1)*im*im =
            // = (re*re - im*im) + i*(2*re*im)
            double reNew = re * re - im * im + x; // x - действ.
            double imNew = 2 * re * im + y; // y - мним.
            re = reNew;
            im = imNew;
            // если |z| > 2 (точка находится не во множестве Мандельброта) -> выходим из цикла
            // для увеличения скорости выполнения сравниваем |z|^2 и 2^2
            if (re * re + im * im > 2 * 2){
                return count;
            }
        }
        // мы прошли весь цикл и не вышли из него раньше -> значение count дошло до 2000 -> возврящаем -1
        return -1;
    }



}
