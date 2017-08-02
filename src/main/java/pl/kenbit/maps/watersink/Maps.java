package pl.kenbit.maps.watersink;

import gov.nasa.worldwind.WorldWindow;
import java.util.ArrayList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;

public class Maps {

    DataSource data;
    ElevationService nasa;
    SectorData sectorData;

    public Maps(DataSource data, ElevationService nasa) {
        super();
        this.data = data;
        this.nasa = nasa;
    }

    public static ArrayList<Double> setCoordinateListDouble() {
        ArrayList<Double> coordinateListDouble = new ArrayList<Double>(16);

        coordinateListDouble.add(-0.001); // wiersz
        coordinateListDouble.add(-0.001); // kolumna

        coordinateListDouble.add(-0.001);
        coordinateListDouble.add(0.000);

        coordinateListDouble.add(-0.001);
        coordinateListDouble.add(0.001);
        ///////////////////////////////
        coordinateListDouble.add(0.000);
        coordinateListDouble.add(-0.001);

        coordinateListDouble.add(0.000);
        coordinateListDouble.add(0.001);
        ///////////////////////////////
        coordinateListDouble.add(0.001);
        coordinateListDouble.add(-0.001);

        coordinateListDouble.add(0.001);
        coordinateListDouble.add(0.000);

        coordinateListDouble.add(0.001);
        coordinateListDouble.add(0.001);

        return coordinateListDouble;
    }

    public static ArrayList<Integer> setCoordinateListInteger() {
        ArrayList<Integer> coordinateListInteger = new ArrayList<Integer>(16);

        coordinateListInteger.add(-1); // wiersz
        coordinateListInteger.add(-1); // kolumna

        coordinateListInteger.add(-1);
        coordinateListInteger.add(0);

        coordinateListInteger.add(-1);
        coordinateListInteger.add(1);
        ///////////////////////////////
        coordinateListInteger.add(0);
        coordinateListInteger.add(-1);

        coordinateListInteger.add(0);
        coordinateListInteger.add(1);
        ///////////////////////////////
        coordinateListInteger.add(1);
        coordinateListInteger.add(-1);

        coordinateListInteger.add(1);
        coordinateListInteger.add(0);

        coordinateListInteger.add(1);
        coordinateListInteger.add(1);

        return coordinateListInteger;
    }

    // Tablica przechowywujaca zalane punkty (wartosc punktu) wsp. punktow zalanych
    protected ArrayList<Double> listOfFloodValue() {
        ArrayList<Double> listOfFloodValue = new ArrayList<Double>(16);
        return listOfFloodValue;
    }

    // Tablica przechowywujaca wsp. punktow zalanych
    protected ArrayList<Double> listOfFloodCoordinate() {
        ArrayList<Double> listOfFloodCoordinate = new ArrayList<Double>(16);
        return listOfFloodCoordinate;
    }

    public Boolean[][] booleanElevationsMap() {

//        if (data.getWaterPointLat() > data.getLengthTab()) {
        Boolean[][] booleanElevationsMap = new Boolean[data.getWidthTab()][data.getLengthTab()];
        for (int i = 0; i <= booleanElevationsMap.length - 1; i++) {
            for (int j = 0; j <= booleanElevationsMap[i].length - 1; j++) {
                booleanElevationsMap[i][j] = false;
            }
        }
        return booleanElevationsMap;

    }

    public Boolean[][] waterDirectionMap() {

        Boolean[][] waterDirectionMap = new Boolean[data.getWidthTab()][data.getLengthTab()];
        for (int i = 0; i <= waterDirectionMap.length - 1; i++) {
            for (int j = 0; j <= waterDirectionMap[i].length - 1; j++) {
                waterDirectionMap[i][j] = false;
            }
        }
        return waterDirectionMap;
    }

    public Double[][] elevationsMap() {
        double a = 0;
        double b = 0;

        Double[][] elevationsMap = new Double[data.getWidthTab() - 1][data.getLengthTab()];
//        nasa.getResolution(data);
        for (int i = 0; i <= elevationsMap.length - 1; i++, a += 0.001) {
            b = 0;
            for (int j = 0; j <= elevationsMap[i].length - 1; j++, b += 0.001) {
//                    
                elevationsMap[j][i] = nasa.getElevForLatLon(data.getMinGeoLat() + a, data.getMinGeoLon() + b);

            }
        }
        return elevationsMap;
    }

}
