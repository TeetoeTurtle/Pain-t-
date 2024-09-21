package com.paint021;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.StrokeLineCap;

import static java.lang.Math.abs;

public class StrokeController {

    public StrokeController() {}


    //Draws a rectangle based on the mouses starting and ending location
    void drawRectangle(GraphicsContext gc, MouseEvent event,double mouseXFinal, double mouseYFinal) {
        gc.setMiterLimit(10);
        gc.setLineCap(StrokeLineCap.SQUARE);
        double[] xCord = new double[]{mouseXFinal,mouseXFinal,event.getX(),event.getX()};
        double[] yCord = new double[]{mouseYFinal,event.getY(),event.getY(),mouseYFinal};
        gc.strokePolygon(xCord,yCord,4);
    }

    //Uses the general formula for the equation of a general polygon
    public void drawPolygon(GraphicsContext gc, MouseEvent event ,double mouseXFinal, double mouseYFinal, int numSides) {
        double[] xCord = new double[numSides];
        double[] yCord = new double[numSides];
        double r = Math.sqrt((Math.pow((event.getX()-mouseXFinal),2)+Math.pow((event.getY()-mouseYFinal),2))); //Calcs the radius
        //double startDirection = Math.atan2((event.getY()-mouseYFinal), event.getX()-mouseXFinal); //Calcs mouse direction
        for (int i = 0; i < numSides; i++) {
            xCord[i]= mouseXFinal + (r * Math.cos((2*Math.PI * i)/ numSides));
            yCord[i]= mouseYFinal + (r * Math.sin((2*Math.PI * i)/ numSides));
        }
        gc.strokePolygon(xCord,yCord,numSides);
    }

    //Draws a square based on the mouses starting and ending location, but does some basic math to ensure equal length sides.
    //Is dependent on the mouse's Y location (as opposed to X location)
    void drawSquare(GraphicsContext gc, MouseEvent event,double mouseXFinal, double mouseYFinal) {
        gc.setMiterLimit(10);
        gc.setLineCap(StrokeLineCap.SQUARE);
        double[] xCord;
        double[] yCord;
        double xInital,yInital,xFinal,yFinal;
        xInital = mouseXFinal;
        yInital = mouseYFinal;
        yFinal=event.getY();
        double A = abs(yInital - event.getY());
        if(xInital<event.getX()){
            xFinal=xInital+A;
        }
        else{
            xFinal=xInital-A;
        }
        xCord = new double[]{xInital, xInital, xFinal, xFinal};
        yCord = new double[]{yInital,yFinal,yFinal,yInital};
        gc.strokePolygon(xCord,yCord,4);
    }

    //Draws a circle. Due to strokeOval only taking parameters for the upper left corner, it is more complex than strokePolygon. Determines the upper left corner
    //by constantly modifying the mouse's initial coords with its current coords. Also ensures that width and height are equal
    void drawCircle(GraphicsContext gc, MouseEvent event,double mouseXFinal, double mouseYFinal) {
        if (mouseXFinal<event.getX() && mouseYFinal<event.getY()){
            gc.strokeOval(mouseXFinal,mouseYFinal,abs(mouseYFinal-event.getY()),abs(mouseYFinal-event.getY()));
        }
        else if(mouseXFinal>event.getX() && mouseYFinal<event.getY()){
            gc.strokeOval(mouseXFinal-(event.getY()-mouseYFinal),mouseYFinal,abs(mouseYFinal-event.getY()),abs(mouseYFinal-event.getY()));
        }
        else if(mouseXFinal>event.getX() && mouseYFinal>event.getY()){
            gc.strokeOval(mouseXFinal-(mouseYFinal-event.getY()),mouseYFinal-(mouseYFinal-event.getY()),abs(mouseYFinal-event.getY()),abs(mouseYFinal-event.getY()));
        }
        else if(mouseXFinal<event.getX() && mouseYFinal>event.getY()){
            gc.strokeOval(mouseXFinal,mouseYFinal-(mouseYFinal-event.getY()),abs(mouseYFinal-event.getY()),abs(mouseYFinal-event.getY()));
        }
    }

    //Draws an oval. Due to strokeOval only taking parameters for the upper left corner, it is more complex than strokePolygon. Determines the upper left corner
    //by constantly modifying the mouse's initial coords with its current coords
    void drawOval(GraphicsContext gc, MouseEvent event,double mouseXFinal, double mouseYFinal) {
        if (mouseXFinal<event.getX() && mouseYFinal<event.getY()){
            gc.strokeOval(mouseXFinal,mouseYFinal,abs(mouseXFinal-event.getX()),abs(mouseYFinal-event.getY()));
        }
        else if(mouseXFinal>event.getX() && mouseYFinal<event.getY()){
            gc.strokeOval(mouseXFinal-(mouseXFinal-event.getX()),mouseYFinal,abs(mouseXFinal-event.getX()),abs(mouseYFinal-event.getY()));
        }
        else if(mouseXFinal>event.getX() && mouseYFinal>event.getY()){
            gc.strokeOval(mouseXFinal-(mouseXFinal-event.getX()),mouseYFinal-(mouseYFinal-event.getY()),abs(mouseXFinal-event.getX()),abs(mouseYFinal-event.getY()));
        }
        else if(mouseXFinal<event.getX() && mouseYFinal>event.getY()){
            gc.strokeOval(mouseXFinal,mouseYFinal-(mouseYFinal-event.getY()),abs(mouseXFinal-event.getX()),abs(mouseYFinal-event.getY()));
        }
    }

    //Draws a regular triangle based on the mouses starting and ending location. Changes drawing behavior depending on if the mouse's current
    //X & Y coords are greater or less than the starting X & Y coords
    void drawTriangle(GraphicsContext gc, MouseEvent event,double mouseXFinal, double mouseYFinal){
        gc.setMiterLimit(10);
        gc.setLineCap(StrokeLineCap.ROUND);

        double[] xCord;
        double[] yCord;
        if (mouseYFinal < event.getY()) {
            xCord = new double[]{mouseXFinal, abs(mouseXFinal + event.getX()) / 2, event.getX()};
            yCord = new double[]{event.getY(), mouseYFinal, event.getY()};
        } else {
            xCord = new double[]{mouseXFinal, abs(mouseXFinal + event.getX()) / 2, event.getX()};
            yCord = new double[]{mouseYFinal, event.getY(), mouseYFinal};
        }
        gc.strokePolygon(xCord, yCord, 3);
    }

    //Draws a right triangle based on the mouses starting and ending location. Changes drawing behavior depending on if the mouse's current
    //X & Y coords are greater or less than the starting X & Y coords
    void drawRTriangle(GraphicsContext gc, MouseEvent event,double mouseXFinal, double mouseYFinal){
        gc.setMiterLimit(10);
        gc.setLineCap(StrokeLineCap.ROUND);

        double[] xCord;
        double[] yCord;
        xCord = new double[]{mouseXFinal,mouseXFinal,event.getX()};
        yCord = new double[]{mouseYFinal,event.getY(),event.getY()};

        gc.strokePolygon(xCord,yCord,3);
    }

    public void drawHexagon(GraphicsContext gc, MouseEvent event ,double mouseXFinal, double mouseYFinal) {
        int numSides = 6;
        double[] xCord = new double[numSides];
        double[] yCord = new double[numSides];
        double r = Math.sqrt((Math.pow((event.getX()-mouseXFinal),2)+Math.pow((event.getY()-mouseYFinal),2))); //Calcs the radius
        //double startDirection = Math.atan2((event.getY()-mouseYFinal), event.getX()-mouseXFinal); //Calcs mouse direction
        for (int i = 0; i < numSides; i++) {
            xCord[i]= mouseXFinal + (r * Math.cos((2*Math.PI * i)/ numSides));
            yCord[i]= mouseYFinal + (r * Math.sin((2*Math.PI * i)/ numSides));
        }
        gc.strokePolygon(xCord,yCord,numSides);
    }


}
