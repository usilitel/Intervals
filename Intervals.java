package ru.lesson.lessons.Intervals;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.geom.Arc2D;
import java.io.*;
import java.util.ArrayList;
import java.lang.Math.*;

public class Intervals extends JFrame {
    public static final double POSITIVE_INFINITY = 1.0 / 0.0; //плюс бесконечность
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0; //минус бесконечность
    JButton btnLoadFile;
    PanelCharts panelCharts;
    JTextField textFieldX;
    JTextField textFieldXNearest;
    JButton btnCalcNearest;

    int FORM_SIZE_X = 500;
    int FORM_SIZE_Y = 500;
    int PANEL_BUTTONS_SIZE_Y = 100;
    int PANEL_CHARTS_SIZE_Y = 400;
    int COUNT_CHARTS = 0;

    // двумерный массив для хранения исходных интервалов (1 объект = 1 отрезок/полуинтервал).
    // 1-й индекс - id исходного интервала (номер строки в исходном файле), 2-й индекс - id интервала внутри исходного.
    // Исходный интервал вида (-∞, x1] U [x2, +∞) будет состоять из двух интервалов, интервал вида [x1, x2] - из одного
    ArrayList<ArrayList<IntervalSingle>> arrayIntervalsSingle; // = new ArrayList<ArrayList<IntervalSingle>>();
    ArrayList<IntervalSingle> arrayIntervalsFinal; // = new ArrayList<IntervalSingle>(); // массив для хранения пересечений исходных интервалов

    public static void main(String[] args){
        new Intervals();
    }



    public Intervals() {
        this.setLayout(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setBounds(200, 200, FORM_SIZE_X+6, FORM_SIZE_Y+30);
        this.setResizable(false);

        JPanel panelButtons = new JPanel();
        panelButtons.setBounds(0, 0, FORM_SIZE_X, PANEL_BUTTONS_SIZE_Y);
        panelButtons.setBackground(Color.LIGHT_GRAY);
        panelButtons.setLayout(null);

        panelCharts = new PanelCharts();
        panelCharts.setBounds(0, PANEL_BUTTONS_SIZE_Y, FORM_SIZE_X, PANEL_CHARTS_SIZE_Y);
        panelCharts.setBackground(Color.white);
        panelCharts.setLayout(null);






        btnLoadFile = new JButton("Загрузить файл");
        ActionListener btnLoadFileListener = new Intervals.BtnLoadFileListener();
        btnLoadFile.addActionListener(btnLoadFileListener);
        btnLoadFile.setBounds(10, 10, 200, 20);



        JLabel labelX = new JLabel("x =");
        labelX.setBounds(10, 30, 20, 20);
        textFieldX = new JTextField("3.0");
        textFieldX.setBounds(30, 30, 50, 20);

        JLabel labelXNearest = new JLabel("Ближайшее пересечение: ");
        labelXNearest.setBounds(100, 30, 200, 20);

        textFieldXNearest = new JTextField("");
        textFieldXNearest.setBounds(260, 30, 50, 20);
        textFieldXNearest.setEnabled(false);

        btnCalcNearest = new JButton("Пересчитать");
        ActionListener btnCalcNearestListener = new Intervals.BtnCalcNearestListener();
        btnCalcNearest.addActionListener(btnCalcNearestListener);
        btnCalcNearest.setBounds(320, 30, 150, 20);
        btnCalcNearest.setEnabled(false);



        panelButtons.add(labelX);
        panelButtons.add(textFieldX);
        panelButtons.add(labelXNearest);
        panelButtons.add(textFieldXNearest);
        panelButtons.add(btnCalcNearest);


        panelButtons.add(btnLoadFile);
        this.add(panelButtons);
        this.add(panelCharts);
        this.setVisible(true);


    }


    // открываем исходный файл и записываем его содержание в массив arrayIntervalsSingle
    public void OpenFile() {
        arrayIntervalsSingle = new ArrayList<ArrayList<IntervalSingle>>();
        String strFileText = "";
        String[] strParams;
        int idInterval = 0;
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));

        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File textFile = fc.getSelectedFile();
            try{
                FileInputStream fstream = new FileInputStream(textFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                String strLine;

                while ((strLine = br.readLine()) != null){
                    strParams = strLine.split(",");
                    if(strParams[0].equals("0")){ // интервал вида (-∞, x1] U [x2, +∞)
                        arrayIntervalsSingle.add(new ArrayList<IntervalSingle>());
                        arrayIntervalsSingle.get(idInterval).add(new IntervalSingle(NEGATIVE_INFINITY, Double.parseDouble(strParams[1])));
                        arrayIntervalsSingle.get(idInterval).add(new IntervalSingle(Double.parseDouble(strParams[2]), POSITIVE_INFINITY));
                    }
                    else{ // интервал вида [x1, x2]
                        arrayIntervalsSingle.add(new ArrayList<IntervalSingle>());
                        arrayIntervalsSingle.get(idInterval).add(new IntervalSingle(Double.parseDouble(strParams[1]), Double.parseDouble(strParams[2])));
                    }
                    idInterval++;
                }
            }catch (IOException e){
                System.out.println("Ошибка при доступе к файлу " + textFile);
                System.exit(0);
            }
            COUNT_CHARTS = arrayIntervalsSingle.size();
            panelCharts.repaint();
            CrossIntervalsSource();
            btnCalcNearest.setEnabled(true);
            CalcNearest();
        }
        // теперь массив arrayIntervalsSingle заполнен исходными данными
    }



    //-------------------------------



    // метод вычисляет пересечения всех исходных интервалов и записывает их в список arrayIntervalsFinal
    public void CrossIntervalsSource() {
        arrayIntervalsFinal = new ArrayList<IntervalSingle>();
        arrayIntervalsFinal.add(new IntervalSingle(NEGATIVE_INFINITY, POSITIVE_INFINITY));

        for(ArrayList<IntervalSingle> arrayIntervals: arrayIntervalsSingle){
            arrayIntervalsFinal = CrossArraysIntervals(arrayIntervalsFinal, arrayIntervals);
        }
        //PrintArrayFinal();
    }

    // метод вычисляет пересечение двух массивов интервалов (и возвращает массив этих пересечений)
    public ArrayList<IntervalSingle> CrossArraysIntervals(ArrayList<IntervalSingle> arrayList1, ArrayList<IntervalSingle> arrayList2) {
        ArrayList<IntervalSingle> arrayIntervalsResult = new ArrayList<IntervalSingle>();
        IntervalSingle intervalResult = null;

        for(IntervalSingle interval1: arrayList1){ // перебираем интервалы из 1-го массива
            for(IntervalSingle interval2: arrayList2) { // перебираем интервалы из 2-го массива
                intervalResult = CrossIntervals(interval1, interval2);
                if (intervalResult != null){
                    arrayIntervalsResult.add(intervalResult);
                }
            }
        }
        return arrayIntervalsResult;
    }

    // метод возвращает интервал-пересечение двух интервалов
    public IntervalSingle CrossIntervals(IntervalSingle interval1, IntervalSingle interval2) {
        double x1, x2; // границы пересечения интервалов
        IntervalSingle intervalResult = null;

        if(!((interval1.getX2() < interval2.getX1()) || (interval1.getX1() > interval2.getX2()))){ // если интервалы пересекаются - то создаем новый интервал
            intervalResult = new IntervalSingle(Math.max(interval1.getX1(), interval2.getX1()), Math.min(interval1.getX2(), interval2.getX2()));
        }
        return  intervalResult;
    }

    //-------------------------------

    // считаем ближайшее к textFieldX.getText() пересечение интервалов
    public void CalcNearest() {
        textFieldXNearest.setText(String.valueOf(getNearestValue(Double.parseDouble(textFieldX.getText()))));
        panelCharts.repaint();
    }


    // вычисляем число, принадлежащее пересечению подмножеств, максимально близкое к x.
    // список arrayIntervalsFinal должен быть уже заполнен.
    public Double getNearestValue(double x) {
        Double xLow = getNearestValueLow(x); // число, принадлежащее пересечению подмножеств, максимально близкое к x И меньше x
        Double xHigh = getNearestValueHigh(x); // число, принадлежащее пересечению подмножеств, максимально близкое к x И больше x

        if(xLow==null){return xHigh;} // слева нет пересечений
        if(xHigh==null){return xLow;} // справа нет пересечений
        if((x-xLow)<=(xHigh-x)){return xLow;} // вычисляем ближайшее
        else{return xHigh;}
    }

    // вычисляем число, принадлежащее пересечению подмножеств, максимально близкое к x И меньше x (если слева от x нет пересечений - возвращаем null)
    public Double getNearestValueLow(double x) {
        Double y = null;
        for(IntervalSingle interval: arrayIntervalsFinal){ // перебираем итоговый массив пересечений
            if (interval.getX1()>x){return y;} // если перескочили за x - возвращаем y
            if ((interval.getX1()<=x) &&(interval.getX2()>=x)) {return x;}
            if (interval.getX2()<x) {y=interval.getX2();}
        }
        return y;
    }

    // вычисляем число, принадлежащее пересечению подмножеств, максимально близкое к x И больше x (если справа от x нет пересечений - возвращаем null)
    public Double getNearestValueHigh(double x) {
        Double y = null;
        IntervalSingle interval;
        for(int i = arrayIntervalsFinal.size()-1;i>=0;i--){ // перебираем итоговый массив пересечений в обратном порядке
            interval = arrayIntervalsFinal.get(i);
            if (interval.getX2()<x){return y;} // если перескочили за x - возвращаем y
            if ((interval.getX1()<=x) &&(interval.getX2()>=x)) {return x;}
            if (interval.getX1()>x) {y=interval.getX1();}
        }
        return y;
    }
    //-------------------------------



/*
    public void PrintArrayIntervals() {
        for(ArrayList<IntervalSingle> arrayIntervals: arrayIntervalsSingle){
            for(IntervalSingle interval: arrayIntervals) {
                System.out.println(arrayIntervalsSingle.indexOf(arrayIntervals) + ", " + interval.x1 + ", " + interval.x2);
            }
        }
    }

    public void PrintArrayFinal() {
        System.out.println("-----");
        for(IntervalSingle interval: arrayIntervalsFinal){
            System.out.println(arrayIntervalsFinal.indexOf(interval) + ", " + interval.x1 + ", " + interval.x2);
        }
    }
*/


    public class BtnLoadFileListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            OpenFile();
        }
    }

    public class BtnCalcNearestListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            CalcNearest();
        }
    }





    // класс для хранения отдельных интервалов (1 объект = 1 отрезок/полуинтервал)
    public class IntervalSingle {
        private double x1; // левая граница интервала (возможно значение NEGATIVE_INFINITY)
        private double x2; // правая граница интервала (возможно значение POSITIVE_INFINITY)

        public double getX1() {
            return x1;
        }

        public void setX1(double x1) {
            this.x1 = x1;
        }

        public double getX2() {
            return x2;
        }

        public void setX2(double x2) {
            this.x2 = x2;
        }

        public IntervalSingle (double x1, double x2){
            this.setX1(x1);
            this.setX2(x2);
        }
    }

    // класс-панель, на которой рисуем интервалы
    public class PanelCharts  extends JPanel{

        Double minScaleValue = POSITIVE_INFINITY;
        Double maxScaleValue = NEGATIVE_INFINITY;

        // рассчитываем минимальное и максимальное значения шкалы
        public void calcMinMaxValues() {
            Double x1, x2;
            for(ArrayList<IntervalSingle> arrayInterval: arrayIntervalsSingle){
                for(IntervalSingle interval: arrayInterval) {
                    //System.out.println("__ " + interval.getX1());
                    x1=interval.getX1();
                    x2=interval.getX2();
                    if((x1<minScaleValue)&&(x1!=NEGATIVE_INFINITY)){minScaleValue=x1;}
                    if((x2<minScaleValue)&&(x2!=POSITIVE_INFINITY)){minScaleValue=x2;}
                    if((x1>maxScaleValue)&&(x1!=NEGATIVE_INFINITY)){maxScaleValue=x1;}
                    if((x2>maxScaleValue)&&(x2!=POSITIVE_INFINITY)){maxScaleValue=x2;}
                }
            }
        }

        // перерисовываем всё поле
        public void paint(Graphics g) {
            int x1Chart, x2Chart;
            Double x1, x2;

            super.paint(g);

            if(COUNT_CHARTS>0){
                calcMinMaxValues();
                for(int i = 1;i<=COUNT_CHARTS;i++){

                    // рисуем оси
                    int y = (PANEL_CHARTS_SIZE_Y-50)/(COUNT_CHARTS)*i; // координата линии по оси Y
                    g.setColor(Color.black);
                    g.drawLine(0,y,FORM_SIZE_X,y);

                    // рисуем интервалы
                    g.setColor(Color.blue);
                    for(IntervalSingle interval: arrayIntervalsSingle.get(i-1)){
                        x1=interval.getX1();
                        x2=interval.getX2();
                        // вычисляем координаты на форме
                        if(x1==NEGATIVE_INFINITY){x1Chart=0;}
                        else{x1Chart=(int)((((x1-minScaleValue)/(maxScaleValue-minScaleValue))/2+0.25)*FORM_SIZE_X);}

                        if(x2==POSITIVE_INFINITY){x2Chart=FORM_SIZE_X;}
                        else{x2Chart=(int)((((x2-minScaleValue)/(maxScaleValue-minScaleValue))/2+0.25)*FORM_SIZE_X);}

                        g.fillRect(x1Chart,y-3,(x2Chart-x1Chart),3);
                    }
                }

                g.setColor(Color.blue);
                g.drawLine(0,PANEL_CHARTS_SIZE_Y-20,FORM_SIZE_X,PANEL_CHARTS_SIZE_Y-20);


                // рисуем пересечения
                for(IntervalSingle interval: arrayIntervalsFinal){
                    x1=interval.getX1();
                    x2=interval.getX2();
                    // вычисляем координаты на форме
                    if(x1==NEGATIVE_INFINITY){x1Chart=0;}
                    else{x1Chart=(int)((((x1-minScaleValue)/(maxScaleValue-minScaleValue))/2+0.25)*FORM_SIZE_X);}

                    if(x2==POSITIVE_INFINITY){x2Chart=FORM_SIZE_X;}
                    else{x2Chart=(int)((((x2-minScaleValue)/(maxScaleValue-minScaleValue))/2+0.25)*FORM_SIZE_X);}

                    g.setColor(Color.red);
                    int y = PANEL_CHARTS_SIZE_Y-20; // координата линии по оси Y
                    g.fillRect(x1Chart,y-3,(x2Chart-x1Chart),3); // рисуем интервал-пересечение

                    if(x1Chart!=0){
                        g.setColor(Color.red);
                        g.drawLine(x1Chart,0,x1Chart,PANEL_CHARTS_SIZE_Y-10); // рисуем отсечку
                        g.setColor(Color.black);
                        g.drawString(""+x1,x1Chart,PANEL_CHARTS_SIZE_Y); // рисуем шкалу
                    }
                    if(x2Chart!=FORM_SIZE_X){
                        g.setColor(Color.red);
                        g.drawLine(x2Chart,0,x2Chart,PANEL_CHARTS_SIZE_Y-10); // рисуем отсечку
                        g.setColor(Color.black);
                        g.drawString(""+x2,x2Chart,PANEL_CHARTS_SIZE_Y); // рисуем шкалу
                    }
                }

                // рисуем x
                x1=Double.parseDouble(textFieldX.getText());
                x1Chart=(int)((((x1-minScaleValue)/(maxScaleValue-minScaleValue))/2+0.25)*FORM_SIZE_X);
                g.setColor(Color.black);
                g.drawLine(x1Chart,0,x1Chart,PANEL_CHARTS_SIZE_Y-10); // рисуем отсечку
                g.drawString(""+x1,x1Chart,PANEL_CHARTS_SIZE_Y); // рисуем шкалу
            }

        }

    }

}
