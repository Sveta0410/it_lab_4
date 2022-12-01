import javax.swing.*;
import java.awt.*;
import  java.awt.image.BufferedImage;
public class JImageDisplay extends JComponent{
    public BufferedImage image;
    // конструктор принимает целочисленные значения ширины и высоты, и инициализирует объект BufferedImage
    public JImageDisplay (int width, int height){
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Dimension preferredSize = new Dimension(width, height);
        super.setPreferredSize(preferredSize); // super -> вызываем метод родительского класса
    }

    @Override
    // для отрисовки изображения
    public void paintComponent (Graphics g){
        super.paintComponent(g);
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
    }

    // устанавливаем все пиксели изображения в черный цвет
    public void clearImage (){
//        image.setRGB(image.getWidth(), image.getHeight(), 0);
        for (int i = 0; i < image.getHeight(); i++){
            for (int j = 0; i <= image.getWidth(); i++){
                image.setRGB(i, j, 0);
            }
        }
    }

    // устанавливаем пиксель в определенный цвет
    public void drawPixel (int x, int y, int rgbColor){
        image.setRGB(x, y, rgbColor);
    }
}
