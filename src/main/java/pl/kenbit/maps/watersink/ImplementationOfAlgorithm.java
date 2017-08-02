package pl.kenbit.maps.watersink;

import gov.nasa.worldwind.WorldWindow;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ImplementationOfAlgorithm {

    public DataHolder imp(Algorithm algorithm) throws FileNotFoundException {

        DataHolder dataHolder = new DataHolder();
        dataHolder.setWaterPointLat(algorithm.getWaterPointLat());
        dataHolder.setWaterPointLon(algorithm.getWaterPointLon());
        
        algorithm.startCalculation();

        dataHolder.setStartPosition(algorithm.getStartPosition());
        dataHolder.setWaterDirectionMap(algorithm.getWaterDirectionMap());
        dataHolder.setMaxMinLatLon(algorithm.getMaxMinLatLon());
        dataHolder.setListOfFloodValue(algorithm.getListOfFloodValue());
        dataHolder.setListOfFloodCoordinateCopy(algorithm.getListOfFloodCoordinateCopy());
        dataHolder.setListOfFloodValueCopy(algorithm.getListOfFloodValueCopy());
        dataHolder.setCoordinateListDouble(algorithm.getCoordinateListDouble());
        dataHolder.setLengthTab(algorithm.getLengthTab());
        dataHolder.setWidthTab(algorithm.getWidthTab());
        dataHolder.setWaterSourcePointLat(algorithm.getWaterSourcePointLat());
        dataHolder.setWaterSourcePointLon(algorithm.getWaterSourcePointLon());
        dataHolder.setElevationsMap(algorithm.getElevationsMap());

        ArrayList<Double> coordinateListDouble = algorithm.getCoordinateListDouble();
        ArrayList<Double> listOfFloodValue = algorithm.getListOfFloodValue();
        Boolean[][] waterDirectionMap = algorithm.getWaterDirectionMap();
        ArrayList<Integer> coordinateListInteger = algorithm.getCoordinateListInteger();
        Double[][] elevationsMap = algorithm.getElevationsMap();

        return dataHolder;

    }

}
