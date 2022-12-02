import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
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

        // панель для кнопок
        JPanel buttonPanel = new JPanel();

        // кнопка для сброса отображения
        JButton reset = new JButton("Reset Display");
        buttonPanel.add(reset);
        reset.addActionListener(new buttonReset());

        // кнопка для сохранения изображения
        JButton save = new JButton("Save Image");
        buttonPanel.add(save);
        save.addActionListener(new buttonSave());

        frame.add(buttonPanel, BorderLayout.SOUTH);

        // обработчик нажатий на мышь
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
        // проходим через каждую строку в отображении
        for (int i = 0; i < displaySize; i++){
            FractalWorker row = new FractalWorker(i); // создаём отдельный рабочий объект
            row.execute(); // запускаем фоновый поток и задачу в фоновом режиме
        }
    }


    private class buttonReset implements ActionListener { // implements -> реализуем интерфейс
        @Override
        public void actionPerformed(ActionEvent e) {
            fractalGenerator.getInitialRange(range); //сброс диапазона к начальному
            drawFractal(); // перерисовываем фрактал
        }
    }

    // для сохранения изображения
    private class buttonSave implements ActionListener { // implements -> реализуем интерфейс
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("PNG Images", "png");
            chooser.setFileFilter(filter);
            // средство выбора не разрешит пользователю использование отличных от png форматов
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showSaveDialog(jImageDisplay) == JFileChooser.APPROVE_OPTION) {
                try {
                    // сохраняем
                    ImageIO.write(jImageDisplay.image, "png", chooser.getSelectedFile());
                } catch (Exception ee) {
                    // выводим сообщение об ошибке
                    JOptionPane.showMessageDialog(jImageDisplay, ee.getMessage(),
                            "Cannot Save Image", JOptionPane.ERROR_MESSAGE);
                }
            }
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

    // код для работы фрактала с несколькими фоновыми потоками

    // класс для вычисление значений цвета для одной строки фрактала
    private class FractalWorker extends SwingWorker<Object, Object>{
        private int yCoordinate; // целочисленная y-координата вычисляемой строки
        private int[] color; // массив для хранения вычисленных значений RGB для каждого пикселя в этой строке

        public FractalWorker(int y){
            this.yCoordinate = y;
        }

        // вызывается в фоновом потоке и отвечает за выполнение длительной задачи
        @Override
        public Object doInBackground(){
            color = new int[displaySize]; // выделение памяти для массив целых чисел
            int rgbColor;
            // проходим через каждый пиксель в отображении
            for (int x = 0; x < displaySize; x++){

                //x - пиксельная координата; xCoord - координата в пространстве фрактала
                double xCoord = FractalGenerator.getCoord
                        (range.x, range.x + range.width, displaySize, x);
                double yCoord = FractalGenerator.getCoord
                        (range.y,range.y + range.height, displaySize, yCoordinate);

                //количество итераций для соответствующих координат в области отображения фрактала
                int numIters = fractalGenerator.numIterations(xCoord, yCoord);

                if (numIters == -1){
                    // если точка не выходит за границы (число итераций == -1), красим пиксель в чёрный
                    rgbColor = 0;
                } else {
                    float hue = 0.7f + (float) numIters / 200f;
                    rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                }
                color[x] = rgbColor;
            }
            return null;
        }

        @Override
        public void done(){
            // рисуем пиксели в строке
            for (int x = 0; x < displaySize; x++) {
                jImageDisplay.drawPixel(x, yCoordinate, color[x]);
            }
            // перерисовываем указанную область после того, как строка будет вычислена
            jImageDisplay.repaint(0, 0, yCoordinate, displaySize, 1);

        }
    }
}
