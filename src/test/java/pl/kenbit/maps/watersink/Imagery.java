package pl.kenbit.maps.watersink;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import javax.swing.SwingUtilities;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.SurfaceImage;
import java.util.ArrayList;

public class Imagery {

    private static DataHolder dataHolder;
    private static int placemarkNr;

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public double getResolution(DataSource data) {
            ArrayList<LatLon> latlons = new ArrayList<LatLon>();
            Globe globe = AppFrame.this.getWwd().getModel().getGlobe();

            for (double i = 0, a = 0; i < data.getWidthTab(); i++, a += 0.001) {
                for (double j = 0, b = 0; j < data.getLengthTab(); j++, b += 0.001) {

                    latlons.add(LatLon.fromDegrees(data.getMinGeoLat() + a,
                            data.getMinGeoLon() + b));
                }
            }

            Sector sector = Sector.boundingSector(latlons);
            double[] elevations = new double[latlons.size()];
//        Iterate until the best resolution is achieved.Use the elevation model to determine the best elevation.
            Double targetResolution = globe.getElevationModel().getBestResolution(sector);
            Double actualResolution = Double.MAX_VALUE;

            while (actualResolution > targetResolution) {
                int counter = 0;
                    do {
                        actualResolution = globe.getElevations(sector, latlons, targetResolution, elevations);
                        // Uncomment the two lines below if you want to watch the
                        // resolution converge
                        System.out.printf("Target resolution = %s, Actual resolution = %s\n",
                                Double.toString(targetResolution), Double.toString(actualResolution));
                        try {
                            Thread.sleep(0, 5); // give the system a chance to
                            // retrieve data from the disk cache
                            // or the server
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        counter++;
                    } while (counter <= 30);

                    System.out.println("Zmniejsz obszar aby znaleźć najlepszą rozdzielczość");
                break;
            }

            return actualResolution;
        }

        public class MapService implements ElevationService {

            public double getElevForLatLon(double lat, double lon) {

                return AppFrame.this.getWwd().getModel().getGlobe().getElevation(Angle.fromDegrees(lat), Angle.fromDegrees(lon));

            }

        }

        private MapService servImpl = new MapService();

        public AppFrame() {
            // Show the WAIT cursor because the import may take a while.
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Import the imagery on a thread other than the event-dispatch
            // thread to avoid freezing the UI.
            Thread t = new Thread(new Runnable() {
                public void run() {

                    setImagiery();

                    // Restore the cursor.
                    setCursor(Cursor.getDefaultCursor());
                }
            });

            t.start();
        }

        public MapService getMapServ() {
            return servImpl;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        public void setImagiery() {
            try {

                ImplementationOfAlgorithm algorithm = new ImplementationOfAlgorithm();
                DataSource data = new DataSource();
                data.makeData();
                getResolution(data);
                dataHolder = algorithm.imp(new Algorithm(new Maps(data, getMapServ())));

                // Add the SurfaceImage to a layer.
                SurfaceImageLayer layer = new SurfaceImageLayer();
                layer.setName("Imported Surface Image");
                layer.setPickEnabled(false);

                putPlacemark(Position.fromDegrees(dataHolder.getMaxMinLatLon().getMin().getLat(),
                        dataHolder.getMaxMinLatLon().getMin().getLon()), layer);
                putPlacemark(Position.fromDegrees(dataHolder.getMaxMinLatLon().getMax().getLat(),
                        dataHolder.getMaxMinLatLon().getMax().getLon()), layer);
                Sector sector = new Sector(Angle.fromDegrees(dataHolder.getMaxMinLatLon().getMin().getLat()),
                        Angle.fromDegrees(dataHolder.getMaxMinLatLon().getMax().getLat()),
                        Angle.fromDegrees(dataHolder.getMaxMinLatLon().getMin().getLon()),
                        Angle.fromDegrees(dataHolder.getMaxMinLatLon().getMax().getLon()));

                BufferedImage image = new BufferedImage(dataHolder.getLengthTab(), dataHolder.getWidthTab(), BufferedImage.TYPE_INT_ARGB);
                SurfaceImage surfaceImage = new SurfaceImage(image, sector);

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Color blueColor = new Color(0, 100, 250, 140);
                        Color transparentColor = new Color(0, 0, 0, 0);

                        Boolean[][] waterDirection = dataHolder.getWaterDirectionMap();

                        for (int i = 0; i < waterDirection.length; i++) {
                            for (int j = 0; j < waterDirection[i].length; j++) {
                                if (waterDirection[waterDirection.length - 1 - i][j] == true) {
                                    image.setRGB(j, i, (blueColor.getRGB()));
                                } else {
                                    image.setRGB(j, i, (transparentColor.getRGB()));
                                }
                            }
                        }

                        layer.addRenderable(surfaceImage);

                        // Add the layer to the model and update the
                        // application's layer panel.
                        insertBeforeCompass(AppFrame.this.getWwd(), layer);

                    }

                    private void insertBeforeCompass(WorldWindow wwd, SurfaceImageLayer layer) {
                        // Insert the layer into the layer list just before the
                        // compass.
                        int compassPosition = 0;
                        LayerList layers = wwd.getModel().getLayers();
                        for (Layer l : layers) {
                            if (l instanceof CompassLayer) {
                                compassPosition = layers.indexOf(l);
                            }
                        }
                        layers.add(compassPosition, layer);

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private void putPlacemark(Position position, SurfaceImageLayer layer) {

            PointPlacemark pp = new PointPlacemark(position);
            pp.setLabelText("Placemark " + ++placemarkNr);
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Label, Semi-transparent, Audio icon");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pp.setEnableLabelPicking(true); // enable label picking for this
            // placemark
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setImageAddress("gov/nasa/worldwindx/examples/images/plain-white.png");
            attrs.setImageColor(new Color(1f, 1f, 1f, 0.6f));
            attrs.setScale(0.6);
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            layer.addRenderable(pp);
        }

    }

    public static void main(String[] args) throws FileNotFoundException {

        ApplicationTemplate.start("Floodplains", Imagery.AppFrame.class);

    }

}
