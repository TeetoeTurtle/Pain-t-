package com.paint021;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Stack;
import javafx.scene.control.MenuBar;
import static java.lang.Math.abs;

public class PaintController {

    static PaintController controller;

    private File selectedFile;

    @FXML private BorderPane borderPane;

    @FXML private TextField brushSize;

    @FXML private Canvas canvas;

    @FXML private Canvas tempCanvas;

    @FXML private ColorPicker colorPicker;

    @FXML private ToggleButton dashedLineTool;

    @FXML private ToggleButton paintTool;

    @FXML private ToggleButton eraser;

    @FXML private Button clearTool;

    @FXML private ToggleButton textTool;

    @FXML private ToggleButton selectTool;

    @FXML private ToggleButton lineTool;

    @FXML private ToggleButton squareTool;

    @FXML private ToggleButton rectTool;

    @FXML private ToggleButton ovalTool;

    @FXML private ToggleButton circleTool;

    @FXML private ToggleButton triangleTool;

    @FXML private ToggleButton rTriangleTool;

    @FXML private ToggleButton hexagonTool;

    @FXML private ToggleButton polygonTool;

    @FXML private Slider sideSlider;

    @FXML private Text sideNumText;

    @FXML private MenuBar menu;

    @FXML private MenuItem addTabBtn;

    @FXML private MenuItem openBtn;

    @FXML private MenuItem saveAsBtn;

    @FXML private MenuItem saveBtn;

    @FXML private Text title;

    @FXML private Tab startTab;

    @FXML private TabPane tabPane;

    @FXML private Button undoBtn;

    @FXML private Button redoBtn;

    @FXML private ComboBox<String> fontDropdown;
    private ObservableList<String> fonts;

    private ToggleButton[] toggleBtnList;

    private Stack<WritableImage> currentUndoStack = new Stack<>();
    private Stack<WritableImage> currentRedoStack = new Stack<>();

    private WritableImage selectedImage;
    private SnapshotParameters params = new SnapshotParameters();
    private double selectWidth, selectHeight,selectX,selectY;;
    boolean itemSelected, mouseWithinSelected = false;

    private WritableImage copiedImage;

    private double mouseXInital,mouseYInital,mouseXFinal,mouseYFinal;

    private ArrayList<TabController> tabList = new ArrayList<>();
    private int tabNum=1;
    private TabController previousTab;

    GraphicsContext gc;
    GraphicsContext tempGC;


    @FXML
    void addTab(ActionEvent event) {
        tabList.add(new TabController());
        tabList.get(tabNum).getTab().setText("Painting " + (tabNum+1));
        tabPane.getTabs().add(tabList.get(tabNum).getTab());
        this.tabNum++;
    }


    @FXML
    void setTab(MouseEvent event){
        System.out.println(previousTab);

        previousTab.setImage(canvas.snapshot(null, previousTab.getImage()));
        for (int i=0; i<tabList.size(); i++) {
            if (tabPane.getSelectionModel().getSelectedItem() == tabList.get(i).getTab()) {
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.drawImage(tabList.get(i).getImage(), 0, 0);
                currentUndoStack=tabList.get(i).getUndoStack();
                currentRedoStack=tabList.get(i).getRedoStack();
                previousTab=tabList.get(i);
                System.out.println(previousTab);
                return;
            }
        }
    }

    //Thanks to open-analysis on GitHub for help starting undo/redo
    //Captures the current canvas, adds it to UndoStack
    void addUndo(){
        SnapshotParameters undoParams = new SnapshotParameters();
        WritableImage image = canvas.snapshot(undoParams, null);
        canvas.snapshot(undoParams, image);
        currentUndoStack.push(image);
    }

    //Captures the current canvas, adds it to RedoStack
    void addRedo(){
        WritableImage image = new WritableImage( (int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, image);
        currentRedoStack.push(image);
    }

    @FXML
    //On clicking undo, if UndoStack isn't empty, it saves current canvas to RedoStack, takes the image on the
    //top of the UndoStack, and changes the canvas to said image.
    void undo(MouseEvent event){
        clearSelected();

        if (currentUndoStack.empty())
            return;

        addRedo();
        Image img = currentUndoStack.pop();
        gc.drawImage(img, 0, 0);
    }

    //Exact same as undo(), however static. Acts as a workaround for Ctrl+Z
    static void staticUndo(){
        controller.clearSelected();

        if (controller.currentUndoStack.empty())
            return;

        controller.addRedo();
        Image img = controller.currentUndoStack.pop();
        controller.gc.drawImage(img, 0, 0);
    }

    @FXML
    //On clicking undo, if UndoStack isn't empty, it saves current canvas to RedoStack, takes the image on the
    //top of the UndoStack, and changes the canvas to said image.
    void redo(MouseEvent event){
        clearSelected();

        if (currentRedoStack.empty())
            return;

        addUndo();
        Image img = currentRedoStack.pop();
        gc.drawImage(img, 0, 0);
    }

    //Changes the stroke of the gc's to be the following default. Used in initialize()
    private void setLineProperties(GraphicsContext gc, GraphicsContext tempGC, double size){

        gc.setLineWidth(size);
        gc.setStroke(colorPicker.getValue());
        gc.setFill(colorPicker.getValue());
        gc.setLineCap(StrokeLineCap.ROUND);
        tempGC.setLineWidth(size);
        tempGC.setStroke(colorPicker.getValue());
        tempGC.setLineCap(StrokeLineCap.ROUND);
        tempGC.clearRect(0,0, canvas.getWidth(), canvas.getHeight());

        //Dashed line check
        if (!dashedLineTool.isSelected()) {
            tempGC.setLineDashes(0);
            gc.setLineDashes(0);
        }
        else if (dashedLineTool.isSelected()) {
            tempGC.setLineDashes(2*size);
            gc.setLineDashes(2*size);
        }
    }

    //Everything with is executed on launch
    public void initialize() {
        //Work around in order to let PaintController both be static and have the specific instance of it. Mainly allows for PaintController to execute methods
        //within PaintApplication, as an instance of PaintController is never actually made within PaintApplication (or at least not one I know how to access)
        controller = this;

        //This list contains all ToggleButtons that should un-toggle when another is selected. Used in toggleBtnEnable
        toggleBtnList = new ToggleButton[]{hexagonTool,polygonTool,textTool,selectTool, paintTool, lineTool, eraser, rectTool, squareTool, triangleTool, rTriangleTool, ovalTool, circleTool};

        //gc is the primary canvas that stuff is actually drawn on. tempCanvas is used for the drawing previews within shapes, etc
        gc = canvas.getGraphicsContext2D();
        tempGC = tempCanvas.getGraphicsContext2D();

        //Image used for select tool that will get moved around
        selectedImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());

        gc.setImageSmoothing(false);

        //Makes the background white
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //See DrawShape.java. Handles drawing for all shapes
        StrokeController strokeController = new StrokeController();

        //Adds all the fonts to the fontDropdown
        fontDropdown.getItems().addAll(FXCollections.observableArrayList(Font.getFamilies()));

        //Sets initial properties in regard to different tabs, with the first tab being the default
        tabList.add(new TabController(startTab, canvas.snapshot(null, new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight()))));
        tabList.get(0).getTab().setText("Painting " + 1);
        previousTab = tabList.get(0);
        currentRedoStack = tabList.get(0).getRedoStack();
        currentUndoStack = tabList.get(0).getUndoStack();

        //Executed when the mouse is pressed on tempGC. Primary used to get mouseX&YFinal, which is used for the starting location of the shape tools,
        //but also, due to stroke() being a little goofy, does a sorta reset of stroke() when using regular paint tool
        tempCanvas.setOnMousePressed(event -> {
            if (!selectTool.isSelected()) {
                addUndo();
                currentRedoStack.clear();
            }

            double size = Double.parseDouble(brushSize.getText());
            setLineProperties(gc, tempGC, size);

            if (paintTool.isSelected()) {
                gc.setLineWidth(size);
                gc.setMiterLimit(1);
                gc.beginPath();
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }

            if (eraser.isSelected()) {
                gc.setLineWidth(size);
                gc.setMiterLimit(1);
                gc.setStroke(Color.WHITE);
                gc.beginPath();
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }

            //Clear the position where the selectedImage was
            else if (selectTool.isSelected()) {
                if (itemSelected && params.getViewport().intersects(event.getX(), event.getY(), 0, 0)) {
                    addUndo();
                    mouseWithinSelected = true;
                    gc.setFill(Color.WHITE);
                    gc.fillRect(selectX, selectY, selectWidth, selectHeight);
                    tempGC.drawImage(selectedImage, selectX, selectY, selectWidth, selectHeight);
                    tempGC.setStroke(Color.BLUE);
                    tempGC.setLineDashes(10);
                    tempGC.setLineWidth(5);
                    tempGC.strokeRect(selectX, selectY, selectWidth, selectHeight);
                } else {
                    mouseWithinSelected = false;
                    itemSelected = false;

                }
            }

            mouseXFinal = event.getX();
            mouseYFinal = event.getY();
        });

        //Executed when the mouse is dragged on tempGC. Chooses the tool via a bunch of if-else, and (if a shape is selected) uses drawShape to create a shape.
        //For Specifically shapes, this draws on the tempGC and is then cleared, giving the illusion of a preview. The actual handling of drawing on gc
        //is handled by setOnMouseReleased
        tempCanvas.setOnMouseDragged(event -> {
            double size = Double.parseDouble(brushSize.getText());
            setLineProperties(gc, tempGC, size);

            if (paintTool.isSelected()) {
                gc.setMiterLimit(1);
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            } else if (eraser.isSelected()) {
                gc.setMiterLimit(1);
                gc.setStroke(Color.WHITE);
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }

            //Two states. If nothing is currently selected, will draw a dashed blue box based on mouse's position.
            //This blue box indicates to the user where the selected area is. If something is selected, it will
            //preview to the user where the selected image is going to be place by drawing it on the tempGC
            else if (selectTool.isSelected()) {
                if (!itemSelected || !(mouseWithinSelected)) {
                    itemSelected = false;
                    tempGC.setStroke(Color.BLUE);
                    tempGC.setLineDashes(10);
                    tempGC.setLineWidth(5);
                    strokeController.drawRectangle(tempGC, event, mouseXFinal, mouseYFinal);
                } else {
                    tempGC.setStroke(Color.BLUE);
                    tempGC.setLineDashes(10);
                    tempGC.setLineWidth(5);
                    tempGC.strokeRect((selectX + (event.getX() - mouseXFinal)), (selectY + (event.getY() - mouseYFinal)), selectWidth, selectHeight);
                    tempGC.drawImage(selectedImage, (selectX + (event.getX() - mouseXFinal)), (selectY + (event.getY() - mouseYFinal)), selectWidth, selectHeight);

                }
            } else if (lineTool.isSelected()) {
                tempGC.strokeLine(mouseXFinal, mouseYFinal, event.getX(), event.getY());
            } else if (rectTool.isSelected()) {
                strokeController.drawRectangle(tempGC, event, mouseXFinal, mouseYFinal);
            } else if (squareTool.isSelected()) {
                strokeController.drawSquare(tempGC, event, mouseXFinal, mouseYFinal);
            } else if (ovalTool.isSelected()) {
                strokeController.drawOval(tempGC, event, mouseXFinal, mouseYFinal);
            } else if (circleTool.isSelected()) {
                strokeController.drawCircle(tempGC, event, mouseXFinal, mouseYFinal);
            } else if (triangleTool.isSelected()) {
                strokeController.drawTriangle(tempGC, event, mouseXFinal, mouseYFinal);
            } else if (rTriangleTool.isSelected()) {
                strokeController.drawRTriangle(tempGC, event, mouseXFinal, mouseYFinal);
            } else if (hexagonTool.isSelected()) {
                strokeController.drawHexagon(tempGC, event, mouseXFinal, mouseYFinal);
            } else if (polygonTool.isSelected()) {
                strokeController.drawPolygon(tempGC, event, mouseXFinal, mouseYFinal,(int)sideSlider.getValue());
            }


        });

        //Executed when the mouse is released on tempGC. Chooses the tool via a bunch of if-else, and (if a shape is selected) uses drawShape to create a shape.
        //For Specifically shapes, this draws on only gc
        tempCanvas.setOnMouseReleased(event -> {
            double size = Double.parseDouble(brushSize.getText());
            setLineProperties(gc, tempGC, size);

            //If nothing is currently selected, it will snapshot the part of the canvas that is indicated by the
            //blue box, and save the snapshot to selectedImage and toggle itemSelected to be true.
            //If itemSelected is true (in other words, if the user has selected something with the selectTool), it
            //will then draw the snapshot onto wherever the user releases the mouse
            if (selectTool.isSelected()) {
                if (!itemSelected || !mouseWithinSelected) {
                    selectX = Math.min(mouseXFinal, event.getX());
                    selectY = Math.min(mouseYFinal, event.getY());

                    selectWidth = abs(mouseXFinal - event.getX());
                    selectHeight = abs(mouseYFinal - event.getY());

                    params.setViewport(new Rectangle2D(selectX, selectY, selectWidth, selectHeight));

                    selectedImage = canvas.snapshot(params, null);
                    itemSelected = true;
                    tempGC.setStroke(Color.BLUE);
                    tempGC.setLineDashes(10);
                    tempGC.setLineWidth(5);
                    tempGC.strokeRect(selectX, selectY, selectWidth, selectHeight);
                } else if (itemSelected) {
                    selectX = selectX + (event.getX() - mouseXFinal);
                    selectY = selectY + (event.getY() - mouseYFinal);

                    gc.drawImage(selectedImage, selectX, selectY, selectWidth, selectHeight);

                    params.setViewport(new Rectangle2D(selectX, selectY, selectWidth, selectHeight));
                    selectedImage = canvas.snapshot(params, null);

                    tempGC.setStroke(Color.BLUE);
                    tempGC.setLineDashes(10);
                    tempGC.setLineWidth(5);
                    tempGC.strokeRect(selectX, selectY, selectWidth, selectHeight);
                    currentRedoStack.clear();
                }

            } else if (lineTool.isSelected()) {
                gc.strokeLine(mouseXFinal, mouseYFinal, event.getX(), event.getY());
            } else if (rectTool.isSelected()) {
                strokeController.drawRectangle(gc, event, mouseXFinal, mouseYFinal);
            } else if (squareTool.isSelected()) {
                strokeController.drawSquare(gc, event, mouseXFinal, mouseYFinal);
            } else if (ovalTool.isSelected()) {
                strokeController.drawOval(gc, event, mouseXFinal, mouseYFinal);
            } else if (circleTool.isSelected()) {
                strokeController.drawCircle(gc, event, mouseXFinal, mouseYFinal);
            } else if (triangleTool.isSelected()) {
                strokeController.drawTriangle(gc, event, mouseXFinal, mouseYFinal);
            } else if (rTriangleTool.isSelected()) {
                strokeController.drawRTriangle(gc, event, mouseXFinal, mouseYFinal);
            } else if (hexagonTool.isSelected()) {
                strokeController.drawHexagon(gc, event, mouseXFinal, mouseYFinal);
            } else if (polygonTool.isSelected()) {
                strokeController.drawPolygon(gc, event, mouseXFinal, mouseYFinal,(int)sideSlider.getValue());
            }

        });

        //Executed when the mouse is pressed & released on tempGC. Used basically exclusively for the paint and eraser tool for when the user only clicks
        tempCanvas.setOnMouseClicked(event -> {
            double size = Double.parseDouble(brushSize.getText());
            //setLineProperties(gc, tempGC, size);

            if (eraser.isSelected()) {
                gc.setStroke(Color.WHITE);
                gc.setMiterLimit(1);
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
            else if (paintTool.isSelected()) {
                gc.setMiterLimit(1);
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
            //Credit to open-analysis on GitHub for the majority of the text tool
            else if (textTool.isSelected()) {
                addUndo();

                String text = null;

                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Text Input for Text Box");
                dialog.setHeaderText(null);
                dialog.setContentText("Text:");

                // Traditional way to get the response value.
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    text = result.get();
                }

                // unit test
                assert text != null;

                //setLineWidth needs to be manually changed for text or else you get a big box of color. 1 was chosen
                //because it gives the text a good amount of thickness without becoming squares
                gc.setLineWidth(1);
                gc.setFontSmoothingType(FontSmoothingType.GRAY);

                gc.setFont(new Font(fontDropdown.getValue(),size));
                gc.fillText(text, event.getX(), event.getY());

            }
        });
    }

    @FXML
    void setSlideText(MouseEvent event) {
        sideNumText.setText("Sides: " + (int)sideSlider.getValue());
    }

    @FXML
        //Upon clicking imgBtn, a fileChooser is opened and the user selects an image. Said image it then set onto imgView and displayed
    void openImg(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File("C:\\"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png","*.bmp"));
        selectedFile = fileChooser.showOpenDialog(new Stage());
        //If user selects a file, will set canvas to display image and imgLabel to display its name
        if (selectedFile != null) {
            Image uploadedImg = new Image(selectedFile.toURI().toString());
            if(canvas.getHeight() < uploadedImg.getHeight()){
                canvas.setHeight(uploadedImg.getHeight());
            }
            if(canvas.getWidth() < uploadedImg.getWidth()){
                canvas.setWidth(uploadedImg.getWidth());
            }

            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.drawImage(uploadedImg, 0, 0);

        }
        else{
            System.out.println("No image selected");
        }
    }

    //Copies selected item
    static void copy(){controller.copiedImage = copyImage(controller.selectedImage);}

    //Pastes selected item
    static void paste(){
        controller.addUndo();
        controller.gc.drawImage(controller.selectedImage, 0, 0);
    }

    //Credit to Wolfgang Fahl on StackOverflow for this WritableImage copier. Used in copy and paste
    public static WritableImage copyImage(Image image) {
        int height=(int)image.getHeight();
        int width=(int)image.getWidth();
        PixelReader pixelReader=image.getPixelReader();
        WritableImage writableImage = new WritableImage(width,height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }

    @FXML
        //Uses a SaveController to do most the handling. See SaveController.java .Extracts the image from canvas, opens fileChooser,
        //and ImageIO turns the Image object into a bufferedImage using Swing, and saves it to the location the user selected.
        // selectedFile is also updated to be the new saved location
    void saveAsImg(ActionEvent event) {
        SaveController saveController = new SaveController();
        selectedFile = saveController.save(canvas,selectedFile,true);
    }

    @FXML
        //Extracts the image from canvas, ImageIO turns the Image object into a
        //bufferedImage using Swing, and saves it to the location of the most recently opened image.
    void saveImg(ActionEvent event) {
        SaveController saveController = new SaveController();
        saveController.save(canvas,controller.selectedFile,(selectedFile==null));
    }

    //Very similar to saveAsImg and saveImg, but is static in order to be used within PaintApplication for ctrl+S and save-on-exit
    public static void smartSave(){
        SaveController saveController = new SaveController();
        saveController.save(controller.canvas,controller.selectedFile,(controller.selectedFile==null));
    }

    @FXML
    void clearCanvas(MouseEvent event) {
        if(PopupStage.clearOpen()){
            addUndo();
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    @FXML
        //Credit to Milan Pal Singh on stackoverflow for this popup window
        //Creates a new scene with a VBox that has set text, and displays it
    void openAbout(ActionEvent event) {
        PopupStage.open("About");
    }

    @FXML
    void openHelp(ActionEvent event) {
        PopupStage.open("Help");
    }

    //Sets the size of the canvas and the tempCanvas to page size if it's smaller than page size
    @FXML
    void lockSize(MouseEvent event) {
        if(canvas.getWidth() < borderPane.getWidth()) {
            canvas.setWidth(borderPane.getWidth());
            tempCanvas.setWidth(borderPane.getWidth());
            WritableImage image = new WritableImage( (int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, image);
            gc.drawImage(image, 0, 0);
        }
        if (canvas.getHeight() < borderPane.getHeight()){
            canvas.setHeight(borderPane.getCenter().getLayoutBounds().getHeight());
            tempCanvas.setHeight(borderPane.getCenter().getLayoutBounds().getHeight());
            WritableImage image = new WritableImage( (int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, image);
            gc.drawImage(image, 0, 0);
        }
    }

    //Gets rid of "select" rectangle and selected item. Used when switching off of select tool mid-use
    private void clearSelected(){
        tempGC.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
        params.setViewport(new Rectangle2D(0, 0, 0, 0));
    }

    //When a button is toggled, all other buttons are un-toggled
    @FXML
    void toggleBtnEnable(MouseEvent event){
        clearSelected();

        for (ToggleButton toggleButton : toggleBtnList) {
            toggleButton.setSelected(false);
        }
        ToggleButton tempToggleBtn = (ToggleButton) event.getSource();
        tempToggleBtn.setSelected(true);
    }
}