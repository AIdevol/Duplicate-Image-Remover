package duplicate_image_remover;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

public class CompareImages {
    File[] imgFile = new File[2];
    int[] rawImgHeight = new int[2];
    int[] rawImgWidth = new int[2];
    BufferedImage[] imgBuffer = new BufferedImage[2];
    
    private boolean imagesAreEqualSize() {
        return !(rawImgWidth[0] != rawImgWidth[1] || rawImgHeight[0] != rawImgHeight[1]);
    }
    public boolean imagesAreProportional() {
        double roomForError = 0.03;
        float result = ((float) rawImgHeight[0] / rawImgWidth[0]) -
                       ((float) rawImgHeight[1] / rawImgWidth[1]);
        if (result < 0) { result *= -1; }
        
        return result < roomForError;
    }
    private boolean firstImageIsLarger() {
        return imgBuffer[0].getWidth() > imgBuffer[1].getWidth();
    }
    private void setImageHeightAndWidth(File imgFile, int num) {
        try //Get the image height and width quickly.
        {
            ImageInputStream in = ImageIO.createImageInputStream(imgFile);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext())
            {
                ImageReader reader = readers.next();
                reader.setInput(in);
                rawImgHeight[num] = reader.getHeight(0);
                rawImgWidth[num] = reader.getWidth(0);
            }
        }
        catch (Exception ex) //If getting the image height and width failed, try again using the slow, more reliable method.
        {
            ImageIcon imgIcon = new ImageIcon(imgFile.getAbsolutePath());
            rawImgHeight[num] = imgIcon.getIconHeight();
            rawImgWidth[num] = imgIcon.getIconWidth();
        }
    }
    
    private Color getPixelColor(int pixRGB) {
        int red = (pixRGB & 0x00ff0000) >> 16;
        int green = (pixRGB & 0x0000ff00) >> 8;
        int blue = pixRGB & 0x000000ff;
        return new Color(red, green, blue);
    }
    private Color getSubtractedColor(Color col1, Color col2) {
        int red, green, blue;
        red = col1.getRed() - col2.getRed();
        green = col1.getGreen() - col2.getGreen();
        blue = col1.getBlue() - col2.getBlue();
        if (red < 0) { red *= -1; }
        if (green < 0) { green *= -1; }
        if (blue < 0) { blue *= -1; }
        return new Color(red, green, blue);
    }
    private Image getScaledImage(Image srcImg, int w, int h) {
        //This function's code is by user Suken Shah on StackOverflow.
        //https://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon
        
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        
        return resizedImg;
    } 
    
    // === === === PUBLIC ENUMS AND FUNCTIONS === === ===
    
    public enum CompareMethod {
        BASIC,
        SUBTRACT_COLOR
    }
    public enum FileNum {
        FIRST,
        SECOND
    }
    
    public boolean isValidExtension(File checkImage) {
        String[] validFileExtensions = {".jpg", ".png"};
        boolean extensionValid = false;
        for (String ext : validFileExtensions)
        {
            if (checkImage.getName().endsWith(ext))
            {
                extensionValid = true;
                break;
            }
        }
        return extensionValid;
    }
    
    public double getHeightWidthRatio(double height, double width) { return height / width; }
    
    public BufferedImage importImage(File imgFile) throws IOException {
        if (!isValidExtension(imgFile)) { throw new IOException("The file provided to the 'importImage' function is not valid."); }
        
        ImageIcon imgIcon = new ImageIcon(imgFile.getAbsolutePath());
        double tempHeight = imgIcon.getIconHeight(), tempWidth = imgIcon.getIconWidth();
        int setWidth = (int)tempWidth, setHeight = (int) tempHeight;
        
        int maxPixCount = 400000;
        if (tempHeight * tempWidth > maxPixCount)
        {
            double sizeRatio = getHeightWidthRatio(tempHeight, tempWidth);
            
            tempWidth = Math.sqrt(maxPixCount/sizeRatio);
            tempHeight = (tempWidth * sizeRatio);
            
            if (tempWidth <= 0 || tempHeight <= 0)
            {
                tempWidth = imgIcon.getIconWidth();
                tempHeight = imgIcon.getIconHeight();
            }
            setWidth = (int) Math.round(tempWidth);
            setHeight = (int) Math.round(tempHeight);
            imgIcon.setImage(getScaledImage(imgIcon.getImage(), setWidth, setHeight));
        }
        
        int minPixCount = 350000;
        if (tempHeight * tempWidth < minPixCount)
        {
            double sizeRatio = getHeightWidthRatio(tempHeight, tempWidth);
            
            tempWidth = Math.sqrt(minPixCount/sizeRatio);
            tempHeight = (tempWidth * sizeRatio);
            
            if (tempWidth <= 0 || tempHeight <= 0)
            {
                tempWidth = imgIcon.getIconWidth();
                tempHeight = imgIcon.getIconHeight();
            }
            setWidth = (int) Math.round(tempWidth);
            setHeight = (int) Math.round(tempHeight);
            imgIcon.setImage(getScaledImage(imgIcon.getImage(), setWidth, setHeight));
        }
        
        BufferedImage newIMGBuff = new BufferedImage(setWidth, setHeight, BufferedImage.TYPE_INT_RGB);
        Graphics convert = newIMGBuff.createGraphics();
        imgIcon.paintIcon(null, convert, 0, 0);
        convert.dispose();
        
        return newIMGBuff;
   }
    
    public void setImage(File newFile, FileNum num) throws IOException {
        if (!isValidExtension(newFile)) { throw new IOException("File provided to the 'setImage' function was not valid."); }
        switch (num) {
            case FIRST:
                //if (this.imgBuffer[0] != null) { this.imgBuffer[0].flush(); }
                this.rawImgWidth[0] = 0;
                this.rawImgHeight[0] = 0;
                this.imgFile[0] = newFile;
                setImageHeightAndWidth(newFile, 0);
                break;
            case SECOND:
                //if (this.imgBuffer[1] != null) { this.imgBuffer[1].flush(); }
                this.rawImgWidth[1] = 0;
                this.rawImgHeight[1] = 0;
                this.imgFile[1] = newFile;
                setImageHeightAndWidth(newFile, 1);
        }
    }
    public void setImage(BufferedImage newImageBuffer, FileNum num) {
        switch (num) {
            case FIRST:
                //if (this.imgBuffer[0] != null) { this.imgBuffer[0].flush(); }
                this.imgBuffer[0] = newImageBuffer;
                rawImgHeight[0] = newImageBuffer.getHeight();
                rawImgWidth[0] = newImageBuffer.getWidth();
                break;
            case SECOND:
                //if (this.imgBuffer[1] != null) { this.imgBuffer[1].flush(); }
                this.imgBuffer[1] = newImageBuffer;
                rawImgHeight[1] = newImageBuffer.getHeight();
                rawImgWidth[1] = newImageBuffer.getWidth();
        }
    }
    public BufferedImage getImage(FileNum num) {
        if (num == FileNum.FIRST) { return imgBuffer[0]; }
        else { return imgBuffer[1]; }
    }
    public BufferedImage resizeImageBuff(BufferedImage img, int newWidth, int newHeight) {
        Image newImage = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
        BufferedImage newBuff = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB); //https://stackoverflow.com/questions/37335/how-to-deal-with-java-lang-outofmemoryerror-java-heap-space-error

        Graphics2D graphics = newBuff.createGraphics();
        graphics.drawImage(newImage, 0, 0, null);
        graphics.dispose();
        
        return newBuff;
    }
    
    public BufferedImage getDifferenecs(CompareMethod methodOfComparison) {
        BufferedImage returnImage;
        try
        {
            if (imgFile[0] != null) { this.imgBuffer[0] = importImage(imgFile[0]); }
            else if (imgBuffer[0] == null) { throw new IOException("The first imageBuffer was null, and there was no file to import from."); }
            if (imgFile[1] != null) { this.imgBuffer[1] = importImage(imgFile[1]); }
            else if (imgBuffer[1] == null) { throw new IOException("The second imageBuffer was null, and there was no file to import from."); }
            
            if (imagesAreEqualSize())
            {
                switch (methodOfComparison) {
                    case BASIC:
                        returnImage = new BufferedImage(imgBuffer[0].getWidth(), imgBuffer[0].getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Color rgbWhite = new Color(255, 255, 255), rgbBlack = new Color(0, 0, 0);
                        for (int yPos = 0; yPos < imgBuffer[0].getHeight(); yPos++) {
                            for (int xPos = 0; xPos < imgBuffer[0].getWidth(); xPos++) {
                                if (imgBuffer[0].getRGB(xPos, yPos) == imgBuffer[1].getRGB(xPos, yPos)) {
                                    returnImage.setRGB(xPos, yPos, rgbBlack.getRGB());
                                } else {
                                    returnImage.setRGB(xPos, yPos, rgbWhite.getRGB());
                                }
                            }
                        }
                        return returnImage;
                    case SUBTRACT_COLOR:
                        returnImage = new BufferedImage(imgBuffer[0].getWidth(), imgBuffer[0].getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Color[] pixColor = new Color[2];
                        for (int yPos = 0; yPos < imgBuffer[0].getHeight(); yPos++) {
                            for (int xPos = 0; xPos < imgBuffer[0].getWidth(); xPos++) {
                                pixColor[0] = getPixelColor(imgBuffer[0].getRGB(xPos, yPos));
                                pixColor[1] = getPixelColor(imgBuffer[1].getRGB(xPos, yPos));

                                returnImage.setRGB(xPos, yPos, getSubtractedColor(pixColor[0], pixColor[1]).getRGB());
                            }
                        }
                        return returnImage;
                }
            }
            else //if (imagesAreProportional())
            {
                CompareImages compare = new CompareImages();
                
                if (firstImageIsLarger()) {
                    compare.setImage(resizeImageBuff(this.imgBuffer[0], this.imgBuffer[1].getWidth(), this.imgBuffer[1].getHeight()),
                            FileNum.FIRST);
                    compare.setImage(this.imgBuffer[1], FileNum.SECOND);
                } else {
                    compare.setImage(this.imgBuffer[0], FileNum.FIRST);
                    compare.setImage(resizeImageBuff(this.imgBuffer[1], this.imgBuffer[0].getWidth(), this.imgBuffer[0].getHeight()),
                            FileNum.SECOND);
                }
                return compare.getDifferenecs(methodOfComparison);
            }
        } catch (Exception ex) {
            System.out.println("SOMETHING WENT WRONG WHILE TRYING TO GET DIFFERENCE IMAGES. RETURNING A BLANK IMAGE.");
            System.out.println("\t" + ex.toString());
            System.out.println("\t" + ex.getMessage());
        }
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
    public float getPercentSimilar(CompareMethod searchMethod) {
        float percentSimilar = 0;
        long counter, maxNum;
        try {
            
            if (imgFile[0] != null) { this.imgBuffer[0] = importImage(imgFile[0]); }
            else if (imgBuffer[0] == null) { throw new IOException("The first imageBuffer was null, and there was no file to import from."); }
            if (imgFile[1] != null) { this.imgBuffer[1] = importImage(imgFile[1]); }
            else if (imgBuffer[1] == null) { throw new IOException("The second imageBuffer was null, and there was no file to import from."); }
            
            if (imagesAreEqualSize())
            {
                switch (searchMethod) {
                    case BASIC:
                        maxNum = (imgBuffer[0].getWidth() * imgBuffer[0].getHeight());
                        counter = maxNum;
                        for (int yPos = 0; yPos < imgBuffer[0].getHeight(); yPos++)
                        {
                            for (int xPos = 0; xPos < imgBuffer[0].getWidth(); xPos++)
                            {
                                if (imgBuffer[0].getRGB(xPos, yPos) != imgBuffer[1].getRGB(xPos, yPos))
                                {
                                    counter--;
                                }
                            }
                        }
                        percentSimilar = (float) counter / maxNum;
                        break;
                    case SUBTRACT_COLOR:
                        Color[] pixColor = new Color[2];
                        Color subtractColor;
                        
                        maxNum = 3 * 255 * imgBuffer[0].getWidth() * imgBuffer[0].getHeight();
                        counter = maxNum;
                        for (int yPos = 0; yPos < imgBuffer[0].getHeight(); yPos++)
                        {
                            for (int xPos = 0; xPos < imgBuffer[0].getWidth(); xPos++)
                            {
                                pixColor[0] = getPixelColor(imgBuffer[0].getRGB(xPos, yPos));
                                pixColor[1] = getPixelColor(imgBuffer[1].getRGB(xPos, yPos));

                                subtractColor = getSubtractedColor(pixColor[0], pixColor[1]);

                                counter -= subtractColor.getRed();
                                counter -= subtractColor.getGreen();
                                counter -= subtractColor.getBlue();
                            }
                        }
                        percentSimilar = (float) counter / maxNum;
                }
            }
            else if (imagesAreProportional())
            {
                CompareImages compare = new CompareImages();

                if (firstImageIsLarger()) {
                    compare.setImage(resizeImageBuff(this.imgBuffer[0], this.imgBuffer[1].getWidth(), this.imgBuffer[1].getHeight()),
                            FileNum.FIRST);
                    compare.setImage(this.imgBuffer[1], FileNum.SECOND);
                } else {
                    compare.setImage(this.imgBuffer[0], FileNum.FIRST);
                    compare.setImage(resizeImageBuff(this.imgBuffer[1], this.imgBuffer[0].getWidth(), this.imgBuffer[0].getHeight()),
                            FileNum.SECOND);
                }
                percentSimilar = compare.getPercentSimilar(searchMethod);
            }
            else
            {
                return -1;
            }
        }
        catch (IOException ex)
        {
            System.out.println("SOMETHING WENT WRONG WHILE TRYING TO GET PERCENTAGE SIMILAR. RETURNING -1.\n\n" + ex.getMessage());
            return -1;
        }
        
        return percentSimilar;
    }
    public float getPercentSimilar(CompareMethod searchMethod, javax.swing.JSlider percentRequired) {        
        float percentSimilar = 0;
        long counter, maxNum;
        try {
            if (imgFile[0] != null) { this.imgBuffer[0] = importImage(imgFile[0]); }
            else if (imgBuffer[0] == null) { throw new IOException("The first imageBuffer was null, and there was no file to import from."); }
            if (imgFile[1] != null) { this.imgBuffer[1] = importImage(imgFile[1]); }
            else if (imgBuffer[1] == null) { throw new IOException("The second imageBuffer was null, and there was no file to import from."); }
            
            if (imagesAreEqualSize())
            {
                switch (searchMethod) {
                    case BASIC:
                        maxNum = (imgBuffer[0].getWidth() * imgBuffer[0].getHeight());
                        counter = maxNum;
                        for (int yPos = 0; yPos < imgBuffer[0].getHeight(); yPos++)
                        {
                            if (((float) counter / maxNum) < ((float) percentRequired.getValue() / 100))
                            { return 0; }
                            for (int xPos = 0; xPos < imgBuffer[0].getWidth(); xPos++)
                            {
                                if (imgBuffer[0].getRGB(xPos, yPos) != imgBuffer[1].getRGB(xPos, yPos))
                                {
                                    counter--;
                                }
                            }
                        }
                        percentSimilar = (float) counter / maxNum;
                        break;
                    case SUBTRACT_COLOR:
                        Color[] pixColor = new Color[2];
                        Color subtractColor;
                        
                        maxNum = 3 * 255 * imgBuffer[0].getWidth() * imgBuffer[0].getHeight();
                        counter = maxNum;
                        for (int yPos = 0; yPos < imgBuffer[0].getHeight(); yPos++)
                        {
                            if (((float) counter / maxNum) < ((float) percentRequired.getValue() / 100))
                            { return 0; }
                            for (int xPos = 0; xPos < imgBuffer[0].getWidth(); xPos++)
                            {
                                pixColor[0] = getPixelColor(imgBuffer[0].getRGB(xPos, yPos));
                                pixColor[1] = getPixelColor(imgBuffer[1].getRGB(xPos, yPos));

                                subtractColor = getSubtractedColor(pixColor[0], pixColor[1]);

                                counter -= subtractColor.getRed();
                                counter -= subtractColor.getGreen();
                                counter -= subtractColor.getBlue();
                            }
                        }
                        percentSimilar = (float) counter / maxNum;
                }
            }
            else if (imagesAreProportional())
            {
                CompareImages compare = new CompareImages();

                if (firstImageIsLarger()) {
                    compare.setImage(resizeImageBuff(this.imgBuffer[0], this.imgBuffer[1].getWidth(), this.imgBuffer[1].getHeight()),
                            FileNum.FIRST);
                    compare.setImage(this.imgBuffer[1], FileNum.SECOND);
                } else {
                    compare.setImage(this.imgBuffer[0], FileNum.FIRST);
                    compare.setImage(resizeImageBuff(this.imgBuffer[1], this.imgBuffer[0].getWidth(), this.imgBuffer[0].getHeight()),
                            FileNum.SECOND);
                }
                percentSimilar = compare.getPercentSimilar(searchMethod, percentRequired);
            }
            else
            {
                return -1;
            }
        }
        catch (IOException ex)
        {
            System.out.println("SOMETHING WENT WRONG WHILE TRYING TO GET PERCENTAGE SIMILAR. RETURNING -1.\n\n" + ex.getMessage());
            return -1;
        }
        
        return percentSimilar;
    }
}
