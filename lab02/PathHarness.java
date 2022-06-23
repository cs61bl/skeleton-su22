import java.util.ArrayList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PathHarness extends JPanel implements Runnable {

    public static final int SIZE = 700;
    public static final int CURVE_STEP = 15;
    public static final long SLEEP_TIME = 10;
    public static final float COLOR_SMOOTH = 500.0f;
    public static final double PIX_RES = 3;
    public static final double WEIGHT = PIX_RES / SIZE;

    private ArrayList<Path> pNorth = new ArrayList<Path>();
    private ArrayList<Path> pSouth = new ArrayList<Path>();
    private ArrayList<Path> pEast = new ArrayList<Path>();
    private ArrayList<Path> pWest = new ArrayList<Path>();

    private ArrayList<GeneralPath> north = new ArrayList<GeneralPath>();
    private ArrayList<GeneralPath> south = new ArrayList<GeneralPath>();
    private ArrayList<GeneralPath> east = new ArrayList<GeneralPath>();
    private ArrayList<GeneralPath> west = new ArrayList<GeneralPath>();

    private ArrayList<Color> colors = new ArrayList<Color>();

    private int colorIndex = 0;

    public PathHarness() {
        this(SIZE);
    }

    public PathHarness(int size) {
        super();
        setPreferredSize(new Dimension(size, size));
        setBackground(Color.BLACK);

        for (int i = 0; i < COLOR_SMOOTH; i += 1) {
            float hugh = i / COLOR_SMOOTH;
            Color hue = Color.getHSBColor(hugh, 1f, 1f);
            colors.add(hue);
        }

        for (int step = 0; step < SIZE; step += CURVE_STEP) {
            Path nPath = new Path(step, 0);
            Path sPath = new Path(size - step, size);
            Path ePath = new Path(size, step);
            Path wPath = new Path(0, size - step);

            GeneralPath northPath = new GeneralPath();
            GeneralPath southPath = new GeneralPath();
            GeneralPath eastPath = new GeneralPath();
            GeneralPath westPath = new GeneralPath();

            northPath.moveTo(nPath.getCurrX(), nPath.getCurrY());
            southPath.moveTo(sPath.getCurrX(), sPath.getCurrY());
            eastPath.moveTo(ePath.getCurrX(), ePath.getCurrY());
            westPath.moveTo(wPath.getCurrX(), wPath.getCurrY());

            pNorth.add(nPath);
            pSouth.add(sPath);
            pEast.add(ePath);
            pWest.add(wPath);

            north.add(northPath);
            south.add(southPath);
            east.add(eastPath);
            west.add(westPath);
        }
    }

    private void iterate() {
        for (int i = 0; i < north.size(); i += 1) {
            double dx = east.get(i).getCurrentPoint().getX()
                            - north.get(i).getCurrentPoint().getX();
            double dy = -east.get(i).getCurrentPoint().getY()
                            + north.get(i).getCurrentPoint().getY();

            pNorth.get(i).iterate(dy * WEIGHT, dx * WEIGHT);
            pSouth.get(i).iterate(-dy * WEIGHT, -dx * WEIGHT);
            pEast.get(i).iterate(-dx * WEIGHT, dy * WEIGHT);
            pWest.get(i).iterate(dx * WEIGHT, -dy * WEIGHT);

            north.get(i).lineTo(pNorth.get(i).getNextX(), pNorth.get(i).getNextY());
            south.get(i).lineTo(pSouth.get(i).getNextX(), pSouth.get(i).getNextY());
            east.get(i).lineTo(pEast.get(i).getNextX(), pEast.get(i).getNextY());
            west.get(i).lineTo(pWest.get(i).getNextX(), pWest.get(i).getNextY());
        }
    }

    private Color getGradientColor() {
        Color result = colors.get(colorIndex);
        colorIndex = (colorIndex + 1) % colors.size();
        return result;
    }

    @Override
    public void paintComponent(final Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(getGradientColor());

        for (int i = 0; i < north.size(); i += 1) {
            try {
                g2.draw(north.get(i));
                g2.draw(south.get(i));
                g2.draw(east.get(i));
                g2.draw(west.get(i));
            } catch (ArrayIndexOutOfBoundsException e) {
                // to get rid of a pesky error at exit time
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            iterate();
            this.repaint();
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Cool Drawing!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PathHarness harness = new PathHarness();

        frame.setResizable(false);
        frame.setContentPane(harness);
        frame.pack();
        frame.setVisible(true);

        Thread harnessThread = new Thread(harness);
        harnessThread.start();
    }
}
