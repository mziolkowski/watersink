package pl.kenbit.maps.watersink;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import gov.nasa.worldwind.Configuration;
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
import pl.kenbit.maps.watersink.ElevationService;

public class Imagery {

    private static DataHolder dataHolder;

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
                }

                return actualResolution;
            }

        private static int placemarkNr;

        public class MapService implements ElevationService {

            public double getElevForLatLon(double lat, double lon) {

                return AppFrame.this.getWwd().getModel().getGlobe().getElevation(Angle.fromDegrees(lat), Angle.fromDegrees(lon));

            }

        }

        private MapService servImpl = new MapService();

        public AppFrame() {
            // Show the WAIT cursor because the import may take a while.
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            // this.elevation = new Elevation(dataHolder, this.getWwd());

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
//                if (dataHolder.getWaterPointLat() > dataHolder.getLengthTab()) {
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

                BufferedImage image = new BufferedImage(dataHolder.getWidthTab(), dataHolder.getLengthTab(), BufferedImage.TYPE_INT_ARGB);
                SurfaceImage surfaceImage = new SurfaceImage(image, sector);

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Color blueColor = new Color(0, 100, 250, 140);
                        Color transparentColor = new Color(0, 0, 0, 0);

                        Boolean[][] waterDirection = dataHolder.getWaterDirectionMap();

                        for (int i = 0; i < waterDirection.length; i++) {
                            for (int j = 0; j < waterDirection[i].length; j++) {
                                if (waterDirection[i][waterDirection[i].length - 1 - j] == true) {
                                    image.setRGB(i, j, (blueColor.getRGB()));
                                } else {
                                    image.setRGB(i, j, (transparentColor.getRGB()));
                                }
                            }
                        }

                        layer.addRenderable(surfaceImage);

                        // Add the layer to the model and update the
                        // application's layer panel.
                        insertBeforeCompass(AppFrame.this.getWwd(), layer);

                        // Set the view to look at the imported image.
//						ExampleUtil.goTo(getWwd(), sector);
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
            // attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS,
            // AVKey.PIXELS));
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            layer.addRenderable(pp);
        }

        public static AppFrame start(String appName, Class appFrameClass) {
            if (Configuration.isMacOS() && appName != null) {
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
            }

            try {
                final AppFrame frame = (AppFrame) appFrameClass.newInstance();
                frame.setTitle(appName);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        frame.setVisible(true);
                    }
                });

                return frame;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        ApplicationTemplate.start("Imagery", Imagery.AppFrame.class);

    }

}
