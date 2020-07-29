package com.example.wmr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class WireMeshRemover {

    public static void main(String[] args) {
        try {
            WireMeshRemover remover = new WireMeshRemover();
            remover.removeWhiteMesh("rooster01");
            remover.removeDarkMesh("rooster02");
            remover.removeWhiteMesh("rooster03");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWhiteMesh(String fileName) throws IOException {
        String filePath = fileName + ".jpeg";
        String conv01FilePath = fileName + ".conv01.jpeg";
        String conv02FilePath = fileName + ".conv02.jpeg";
        String conv03FilePath = fileName + ".conv03.jpeg";
        String conv04FilePath = fileName + ".conv04.jpeg";

        //
        BufferedImage img = ImageIO.read(new File(filePath));

        int thresholdRGB = 255 * 3 / 10;
        int thresholdB = 255 * 0 / 5;
        BufferedImage conv01Img = removeWhiteWireMesh(img, thresholdRGB, thresholdB);
        ImageIO.write(conv01Img, "jpeg", new File(conv01FilePath));

        thresholdRGB = 255 * 2 / 5;
        thresholdB = 255 * 3 / 5;
        BufferedImage conv02Img = removeWhiteWireMeshSelf(conv01Img, thresholdRGB, thresholdB);
        ImageIO.write(conv02Img, "jpeg", new File(conv02FilePath));

        int threshold = 255 * 1 / 5;
        BufferedImage conv03Img = removePeakByMove(conv02Img, threshold);
        ImageIO.write(conv03Img, "jpeg", new File(conv03FilePath));

        threshold = 255 * 1 / 6;
        BufferedImage conv04Img = removePeakByMove(conv03Img, threshold);
        ImageIO.write(conv04Img, "jpeg", new File(conv04FilePath));
    }

    public void removeDarkMesh(String fileName) throws IOException {
        String filePath = fileName + ".jpeg";
        String conv01FilePath = fileName + ".conv01.jpeg";
        String conv02FilePath = fileName + ".conv02.jpeg";
        String conv03FilePath = fileName + ".conv03.jpeg";
        String conv04FilePath = fileName + ".conv04.jpeg";

        //
        BufferedImage img = ImageIO.read(new File(filePath));

        int minThreshold = 255 * 1 / 10;
        int maxThreshold = 255 * 9 / 10;
        BufferedImage conv01Img = removeGrayWireMesh(img, minThreshold, maxThreshold);
        ImageIO.write(conv01Img, "jpeg", new File(conv01FilePath));

        int thresholdRGB = 255 * 3 / 10;
        int thresholdB = 255 * 0 / 10;
        BufferedImage conv02Img = removeWhiteWireMesh(conv01Img, thresholdRGB, thresholdB);
        ImageIO.write(conv02Img, "jpeg", new File(conv02FilePath));

        minThreshold = 255 * 7 / 10;
        maxThreshold = 255 * 8 / 10;
        BufferedImage conv03Img = removeGrayWireMeshSelf(conv02Img, minThreshold, maxThreshold);
        ImageIO.write(conv03Img, "jpeg", new File(conv03FilePath));

        int threshold = 255 * 1 / 6;
        BufferedImage conv04Img = removePeak(conv03Img, threshold);
        ImageIO.write(conv04Img, "jpeg", new File(conv04FilePath));

    }

    private BufferedImage removeWhiteWireMesh(BufferedImage img, int thresholdRGB, int thresholdB) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage convImg = new BufferedImage(width, height, img.getType());
        boolean[][] wireMeshFilter = new boolean[width][height];
        System.out.println("Find white wire mesh");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isWireMesh = isWhiteWireMesh(img, x, y, width, height, thresholdRGB, thresholdB);
                wireMeshFilter[x][y] = isWireMesh;
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        System.out.println("Remove white wire mesh");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color newColor = getNewColor(img, wireMeshFilter, x, y, width, height);
                convImg.setRGB(x, y, newColor.getRGB());
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        return convImg;
    }

    private BufferedImage removeWhiteWireMeshSelf(BufferedImage img, int thresholdRGB, int thresholdB) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage convImg = new BufferedImage(width, height, img.getType());
        boolean[][] wireMeshFilter = new boolean[width][height];
        System.out.println("Find white wire mesh self");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isWireMesh = isWhiteWireMeshSelf(img, x, y, width, height, thresholdRGB, thresholdB);
                wireMeshFilter[x][y] = isWireMesh;
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        System.out.println("Remove white wire mesh self");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color newColor = getNewColor(img, wireMeshFilter, x, y, width, height);
                convImg.setRGB(x, y, newColor.getRGB());
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        return convImg;
    }

    private boolean isWhiteWireMesh(BufferedImage img, int x, int y, int width, int height, int thresholdRGB,
            int thresholdB) {
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;
        int pixelNum = 0;
        int range = 7;
        for (int rangeY = y - range; rangeY <= y + range; rangeY++) {
            if (rangeY < 0 || rangeY >= height) {
                continue;
            }
            for (int rangeX = x - range; rangeX <= x + range; rangeX++) {
                if (rangeX < 0 || rangeX >= width) {
                    continue;
                }
                pixelNum++;
                Color color = new Color(img.getRGB(rangeX, rangeY));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                redSum += red;
                greenSum += green;
                blueSum += blue;
            }
        }

        int aveRGB = (int) (redSum / pixelNum + greenSum / pixelNum + blueSum / pixelNum) / 3;
        int aveB = (int) (blueSum / pixelNum) / 1;

        boolean isWireMesh = false;
        if (aveRGB > thresholdRGB & aveB > thresholdB) {
            return true;
        }
        return isWireMesh;
    }

    private boolean isWhiteWireMeshSelf(BufferedImage img, int x, int y, int width, int height, int thresholdRGB,
            int thresholdB) {
        Color color = new Color(img.getRGB(x, y));
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        int aveRGB = (int) (red + green + blue) / 3;
        int aveB = blue;

        boolean isWireMesh = false;
        if (aveRGB > thresholdRGB & aveB > thresholdB) {
            return true;
        }
        return isWireMesh;
    }

    private BufferedImage removeGrayWireMesh(BufferedImage img, int minThreshold, int maxThreshold) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage convImg = new BufferedImage(width, height, img.getType());
        boolean[][] wireMeshFilter = new boolean[width][height];
        System.out.println("Find gray wire mesh");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isWireMesh = isGrayWireMesh(img, x, y, width, height, minThreshold, maxThreshold);
                wireMeshFilter[x][y] = isWireMesh;
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        System.out.println("Remove gray wire mesh");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color newColor;
                if (wireMeshFilter[x][y]) {
                    newColor = getNewColor(img, wireMeshFilter, x, y, width, height);
                    // newColor = new Color(0, 0, 0);
                } else {
                    newColor = new Color(img.getRGB(x, y));
                }

                convImg.setRGB(x, y, newColor.getRGB());
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        return convImg;
    }

    private BufferedImage removeGrayWireMeshSelf(BufferedImage img, int minThreshold, int maxThreshold) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage convImg = new BufferedImage(width, height, img.getType());
        boolean[][] wireMeshFilter = new boolean[width][height];
        System.out.println("Find gray wire mesh self");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isWireMesh = isGrayWireMeshSelf(img, x, y, width, height, minThreshold, maxThreshold);
                wireMeshFilter[x][y] = isWireMesh;
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        System.out.println("Remove gray wire mesh self");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color newColor;
                if (wireMeshFilter[x][y]) {
                    newColor = getNewColor(img, wireMeshFilter, x, y, width, height);
                    // newColor = new Color(0, 0, 0);
                } else {
                    newColor = new Color(img.getRGB(x, y));
                }

                convImg.setRGB(x, y, newColor.getRGB());
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        return convImg;
    }

    private boolean isGrayWireMesh(BufferedImage img, int x, int y, int width, int height, int minThreshold,
            int maxThreshold) {
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;
        int pixelNum = 0;
        int range = 7;
        for (int rangeY = y - range; rangeY <= y + range; rangeY++) {
            if (rangeY < 0 || rangeY >= height) {
                continue;
            }
            for (int rangeX = x - range; rangeX <= x + range; rangeX++) {
                if (rangeX < 0 || rangeX >= width) {
                    continue;
                }
                pixelNum++;
                Color color = new Color(img.getRGB(rangeX, rangeY));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                redSum += red;
                greenSum += green;
                blueSum += blue;
            }
        }

        int aveR = (int) (redSum / pixelNum);
        int aveG = (int) (greenSum / pixelNum);
        int aveB = (int) (blueSum / pixelNum);

        boolean isWireMesh = false;
        if (aveR > minThreshold & aveR < maxThreshold & aveG > minThreshold & aveG < maxThreshold & aveB > minThreshold
                & aveB < maxThreshold) {
            return true;
        }
        return isWireMesh;
    }

    private boolean isGrayWireMeshSelf(BufferedImage img, int x, int y, int width, int height, int minThreshold,
            int maxThreshold) {
        Color color = new Color(img.getRGB(x, y));
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        int aveR = (int) (red);
        int aveG = (int) (green);
        int aveB = (int) (blue);

        boolean isWireMesh = false;
        if (aveR > minThreshold & aveR < maxThreshold & aveG > minThreshold & aveG < maxThreshold & aveB > minThreshold
                & aveB < maxThreshold) {
            return true;
        }
        return isWireMesh;
    }

    private BufferedImage removePeak(BufferedImage img, int threshold) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage convImg = new BufferedImage(width, height, img.getType());
        boolean[][] peakFilter = new boolean[width][height];
        System.out.println("Find peak");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isPeak = isPeak(img, x, y, width, height, threshold);
                peakFilter[x][y] = isPeak;
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        System.out.println("Remove peak");
        int range = 7;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color newColor;
                if (peakFilter[x][y]) {
                    newColor = getMeanColor(img, x, y, width, height, range);
                } else {
                    newColor = new Color(img.getRGB(x, y));
                }

                convImg.setRGB(x, y, newColor.getRGB());
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        return convImg;
    }

    private BufferedImage removePeakByMove(BufferedImage img, int threshold) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage convImg = new BufferedImage(width, height, img.getType());
        boolean[][] peakFilter = new boolean[width][height];
        System.out.println("Find peak");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isPeak = isPeak(img, x, y, width, height, threshold);
                peakFilter[x][y] = isPeak;
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        System.out.println("Remove peak by move");
        int range = 20;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color newColor;
                if (peakFilter[x][y]) {
                    newColor = getMeanColor(img, x + range, y + range, width, height, range);
                } else {
                    newColor = new Color(img.getRGB(x, y));
                }

                convImg.setRGB(x, y, newColor.getRGB());
            }
            if (y % 100 == 0) {
                System.out.println("y = " + y);
            }
        }
        return convImg;
    }

    private boolean isPeak(BufferedImage img, int x, int y, int width, int height, int threshold) {
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;
        int pixelNum = 0;
        int range = 7;
        for (int rangeY = y - range; rangeY <= y + range; rangeY++) {
            if (rangeY < 0 || rangeY >= height || rangeY == y) {
                continue;
            }
            for (int rangeX = x - range; rangeX <= x + range; rangeX++) {
                if (rangeX < 0 || rangeX >= width || rangeY == y) {
                    continue;
                }
                pixelNum++;
                Color color = new Color(img.getRGB(rangeX, rangeY));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                redSum += red;
                greenSum += green;
                blueSum += blue;
            }
        }

        int aveR = (int) (redSum / pixelNum);
        int aveG = (int) (greenSum / pixelNum);
        int aveB = (int) (blueSum / pixelNum);
        Color color = new Color(img.getRGB(x, y));
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        boolean isPeak = false;
        if ((Math.abs(aveR - red) > threshold) || (Math.abs(aveG - green) > threshold)
                || (Math.abs(aveB - blue) > threshold)) {
            return true;
        }
        return isPeak;
    }

    private Color getNewColor(BufferedImage img, boolean[][] wireMeshFilter, int x, int y, int width, int height) {
        boolean isWireMesh = wireMeshFilter[x][y];
        int range = getOutOfWireMeshRange(wireMeshFilter, x, y, width, height);
        if (isWireMesh) {
            Color newColor = getMeanColor(img, wireMeshFilter, x, y, width, height, range);
            return newColor;
        }
        Color newColor = new Color(img.getRGB(x, y));
        return newColor;
    }

    private int getOutOfWireMeshRange(boolean[][] wireMeshFilter, int x, int y, int width, int height) {
        int minOutOfWireMeshRange = 1;
        int maxOutOfWireMeshRange = 8;
        int range;
        for (range = minOutOfWireMeshRange; range <= maxOutOfWireMeshRange; range++) {
            for (int rangeY = y - range; rangeY <= y + range; rangeY++) {
                if (rangeY < 0 || rangeY >= height) {
                    continue;
                }
                for (int rangeX = x - range; rangeX <= x + range; rangeX++) {
                    if (rangeX < 0 || rangeX >= width) {
                        continue;
                    }
                    boolean isWireMeshFilter = wireMeshFilter[rangeX][rangeY];
                    if (!isWireMeshFilter) {
                        break;
                    }
                }
            }
        }
        return range;
    }

    private Color getMeanColor(BufferedImage img, boolean[][] wireMeshFilter, int x, int y, int width, int height,
            int range) {
        int newRedSum = 0;
        int newGreenSum = 0;
        int newBlueSum = 0;
        int outOfRangePixelNum = 0;
        for (int rangeY = y - range; rangeY <= y + range; rangeY++) {
            if (rangeY < 0 || rangeY >= height) {
                continue;
            }
            for (int rangeX = x - range; rangeX <= x + range; rangeX++) {
                if (rangeX < 0 || rangeX >= width) {
                    continue;
                }
                boolean isWireMeshFilter = wireMeshFilter[rangeX][rangeY];
                if (!isWireMeshFilter) {
                    outOfRangePixelNum++;
                    Color color = new Color(img.getRGB(rangeX, rangeY));
                    newRedSum += color.getRed();
                    newGreenSum += color.getGreen();
                    newBlueSum += color.getBlue();
                }
            }
        }
        if (outOfRangePixelNum > 0) {
            int newRed = newRedSum / outOfRangePixelNum;
            int newGreen = newGreenSum / outOfRangePixelNum;
            int newBlue = newBlueSum / outOfRangePixelNum;
            Color newColor = new Color(newRed, newGreen, newBlue);
            return newColor;
        }
        Color newColor = new Color(img.getRGB(x, y));
        return newColor;

    }

    private Color getMeanColor(BufferedImage img, int x, int y, int width, int height, int range) {
        int newRedSum = 0;
        int newGreenSum = 0;
        int newBlueSum = 0;
        int pixelNum = 0;
        for (int rangeY = y - range; rangeY <= y + range; rangeY++) {
            if (rangeY < 0 || rangeY >= height) {
                continue;
            }
            for (int rangeX = x - range; rangeX <= x + range; rangeX++) {
                if (rangeX < 0 || rangeX >= width) {
                    continue;
                }
                pixelNum++;
                Color color = new Color(img.getRGB(rangeX, rangeY));
                newRedSum += color.getRed();
                newGreenSum += color.getGreen();
                newBlueSum += color.getBlue();
            }
        }
        if (pixelNum > 0) {
            int newRed = newRedSum / pixelNum;
            int newGreen = newGreenSum / pixelNum;
            int newBlue = newBlueSum / pixelNum;
            Color newColor = new Color(newRed, newGreen, newBlue);
            return newColor;
        }
        Color newColor = new Color(img.getRGB(x, y));
        return newColor;

    }
}
