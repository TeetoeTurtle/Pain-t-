package com.paint021;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class SaveController {

    //Used for saving the canvas as an image within the users files. Will choose whether to save as a new image or overwrite a previously opened
    //image based on the parameter saveNew, which is determined when called and based on the situation
    File save (Canvas canvas, File selectedFile, boolean saveNew){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png","*.bmp"));
        Image image = canvas.snapshot(null, null);
        File imgFile;
        if (saveNew){
            imgFile = fileChooser.showSaveDialog(new Stage());
        }
        else {
            imgFile = selectedFile;
        }

        //Changes the image (which is extracted from the canvas) into a bufferedImage, allowing it to be saved with ImageIO.write
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image,null), "png", imgFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return imgFile;
    }
}
