import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionListener;


public class FractalExplorer {
    private int displaySize; // размер экрана = ширина и высота отображения в пикселях
    private JImageDisplay jImageDisplay; // ссылка для обновления отображения в разных методах в процессе вычисления фрактала
    private FractalGenerator fractalGenerator; // объект для отображения других видов фракталов в будущем
    private Rectangle2D.Double range; // объект диапазона комплексной плоскости, которая выводится на экран

    private JComboBox myComboBox;

    //конструктор, который принимает значение размера отображения в качестве аргумента и сохраняет его
    //инициализирует объекты диапазона и фрактального генератора
    public FractalExplorer(int sizeOfDisplay){
        this.displaySize = sizeOfDisplay;
        this.fractalGenerator = new Mandelbrot();
        this.range = new Rectangle2D.Double();
        this.fractalGenerator.getInitialRange(this.range);
    }

    public static void main (String[] args){
        FractalExplorer fractalExplorer = new FractalExplorer(800);
        fractalExplorer.createAndShowGUI();
        fractalExplorer.drawFractal();
    }

    // инициализируем графический интерфейс Swing
    public void createAndShowGUI(){
        jImageDisplay = new JImageDisplay(displaySize, displaySize);
        JFrame frame = new JFrame("Fractal Explorer"); // инициализация (создание) окна
        frame.setLayout(new BorderLayout()); // диспетчер компоновки
        frame.add(jImageDisplay, BorderLayout.CENTER); // отображение изображения в центре


        // кнопка для сброса отображения
        JButton button = new JButton("Reset Display");
        frame.add(button, BorderLayout.SOUTH);

        // обработчики нажатий на мышь
        button.addActionListener(new buttonReset());
        jImageDisplay.addMouseListener(new MouseListener());

        // для выбора фрактала
        myComboBox = new JComboBox<>();
        myComboBox.addItem(new Mandelbrot());
        myComboBox.addItem(new Tricorn());
        myComboBox.addItem(new BurningShip());

        JPanel myPanel = new JPanel();
        JLabel label = new JLabel("Choose fractal");
        myPanel.add(label);
        myPanel.add(myComboBox);
        frame.add(myPanel, BorderLayout.NORTH);

        // обработка события о взыимодействии с выпадающим списком
        myComboBox.addActionListener(new ComboActionListener());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // операция закрытия окна по умолчанию

        frame.pack(); // правильно размещает содержимле окна
        frame.setVisible(true); // делает окно видимым
        frame.setResizable(false); // запрет изменения размеров окна
    }

    private void drawFractal(){
        // проходим через каждый пиксель в отображении
        for (int x = 0; x < displaySize; x++){
            for (int y = 0; y < displaySize; y++){
                //x - пиксельная координата; xCoord - координата в пространстве фрактала
                double xCoord = FractalGenerator.getCoord (range.x, range.x + range.width, displaySize, x);
                double yCoord = FractalGenerator.getCoord (range.y, range.y + range.height, displaySize, y);

                //количество итераций для соответствующих координат в области отображения фрактала
                int numIters = fractalGenerator.numIterations(xCoord, yCoord);

                if (numIters == -1){
                    // если точка не выходит за границы (число итераций == -1), красим пиксель в чёрный
                    jImageDisplay.drawPixel(x, y, 0);
                } else {
                    float hue = 0.7f + (float) numIters / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                    jImageDisplay.drawPixel(x, y, rgbColor);
                }
            }
        }
        // обновляем JimageDisplay в соответствии с текущим изображением
        jImageDisplay.repaint();
    }


    private class buttonReset implements ActionListener { // implements -> реализуем интерфейс
        @Override
        public void actionPerformed(ActionEvent e) {
            fractalGenerator.getInitialRange(range); //сброс диапазона к начальному
            drawFractal(); // перерисовываем фрактал
        }
    }

    // обработка события о щелчке мыши (щелчком увеличиваем изображение)
    private class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e){
            int x = e.getX();
            int y = e.getY();
            double xCoord = FractalGenerator.getCoord (range.x, range.x + range.width, displaySize, x);
            double yCoord = FractalGenerator.getCoord (range.y, range.y + range.height, displaySize, y);
            fractalGenerator.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
            drawFractal();
        }
    }

    // обработка события о взыимодействии с выпадающим списком
    private class ComboActionListener implements ActionListener { // implements -> реализуем интерфейс
        @Override
        public void actionPerformed(ActionEvent e) {
            fractalGenerator = (FractalGenerator) myComboBox.getSelectedItem();
            fractalGenerator.getInitialRange(range); //сброс диапазона к начальному
            drawFractal(); // перерисовываем фрактал
        }
    }
}
