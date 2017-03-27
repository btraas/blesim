package as.tra.brayden.blesim;

import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Brayd on 2/19/2017.
 */

public abstract class UVIZIOLED {

    private static final String TAG = UVIZIOLED.class.getName();

    public static final int MAX_FRAMES = 10;
    public static final int MIN_PERIOD = 255;
    //public static final int

    public static final Pixel PIXEL_RED         = new IntPixel(255, 0, 0);
    public static final Pixel PIXEL_GREEN       = new IntPixel(0, 255, 0);
    public static final Pixel PIXEL_BLUE        = new IntPixel(0, 0, 255);
    public static final Pixel PIXEL_RG          = new IntPixel(255, 255, 0);
    public static final Pixel PIXEL_GB          = new IntPixel(0, 255, 255);
    public static final Pixel PIXEL_RB          = new IntPixel(255, 0, 255);
    public static final Pixel PIXEL_WHITE       = new IntPixel(255, 255, 255);
    public static final Pixel PIXEL_BLACK       = new IntPixel(0, 0, 0);
    public static final Pixel PIXEL_OFF         = PIXEL_BLACK;
    public static final Pixel PIXEL_ON          = PIXEL_WHITE;

    private static final int min_period_low = MIN_PERIOD & 0xff;
    private static final int min_period_high = (MIN_PERIOD >> 8) & 0xff;

    public static final byte[] DEFAULT_RECIEVE = new byte[] {
            Mode.STATIC.value(),
            (byte)(min_period_high),
            (byte)(min_period_low),
            1,
            0, 0, 0};

    public static int parseByte(Byte in) {
        int rtn = in & 0xFF;
     //   Log.d(TAG,"parsed byte "+ in + " to " + rtn);
        return rtn;
    }

    public static Data parseBytes(byte[] input) {

        Log.d(TAG, "Recieved bytes for parsing: " + Arrays.toString(input));

        int curIndex = 0;

        Data data = new Data();
        data.mode       = input.length > curIndex ? Mode.valueOf(input[curIndex]) : Mode.STATIC;

        curIndex++;
        int tmpPeriod   = input.length > (curIndex+1) ? 256 * input[curIndex] + input[curIndex+1] : MIN_PERIOD;
        data.period     = tmpPeriod > MIN_PERIOD ? tmpPeriod : MIN_PERIOD;

        curIndex++;
        curIndex++;
        data.numPixels  = input.length > curIndex  && input[curIndex] > 0 ? input[curIndex] : 1;
        data.pixels     = new Pixel[data.numPixels];

        curIndex++;
        int base = curIndex;
        for(int i = 0; i <data.numPixels && (base + (i*3) + 2) < input.length; i++ ) {
            int colorStart = base + (i * 3);
            data.pixels[i] = new BytePixel(input[colorStart], input[colorStart+1], input[colorStart+2]);
            Log.d(TAG, data.pixels[i].toString());
        }

        Log.d(TAG, data.toString());

        return data;

    }


    public enum Mode {
        OFF     ((byte)0),
        STATIC  ((byte)1),
        BLINK  ((byte)2),
        FADE   ((byte)3),
        FRAMES ((byte)4),
        RAINBOW ((byte)5);

        private byte value;

        Mode(byte val) {
            this.value = val;
        }

        @Override
        public String toString() {
            return ""+(int)value;
        }

        public byte value(){
            return value;
        }
        public byte getValue() {
            return value;
        }

        public static Mode valueOf(byte in) {

            for (Mode m: Mode.values()) {
                if(m.value == in) return m;
            }
            return null;
        }

    }

    public static abstract class Pixel {

        public int r;
        public int g;
        public int b;


        public Pixel(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;

            //Log.d(TAG, "new Pixel("+r + ", " + g  + ", "+ b + ")");

        }

        // eg. addColorVal(250, 10)
        // result = 250+10 = 260
        // result > 255
        //      return addColorVal(255, -5)

        // eg. addColorVal(5, -10)
        // result = 5-10 = -5
        // result < 0
        //      return addColorVal(0, 5)

        // eg. addColorVal(250, 270)
        // result = 250 + 270 = 520
        // result > 255
        //      return addColorVal(255, -265)
        //      result = 255 - 265 = -10
        //      result < 0
        //          return addColorVal(0, 10)

        private int addColorVal(int orig, int addVal) {
            int result = orig + addVal;
            if(result >= 0 && result <= 255) return result; // base case

            if(result > 255) {
                return addColorVal(255, -(result-255));
            }
            if(result < 0) {
                return addColorVal(0, -(result));
            }
            return result;
        }

        public Pixel add(int value) {
            //Log.d(TAG, "Adding "+value+" to"+this);
            return new IntPixel(addColorVal(r, value),
                                addColorVal(g, value),
                                addColorVal(b, value));
        }

        public Pixel add(int value, int rDir, int gDir, int bDir) {
            //Log.d(TAG, "Adding "+value+" to"+this);
            return new IntPixel((r+(rDir*value))%256, (g+(gDir*value))%256, (b+(bDir*value))%256);
        }


        @Override
        public String toString() {
            return "[" + this.r + ", " + this.g + ", " + this.b + "]";
        }
    }

    public static class BytePixel extends Pixel {
        public BytePixel(Byte[] rgb) {
            super(parseByte(rgb[0]), parseByte(rgb[1]), parseByte(rgb[2]) );
        }

        public BytePixel(Byte r, Byte g, Byte b) {
            super(parseByte(r), parseByte(g), parseByte(b));
        }

    }

    public static class IntPixel extends Pixel {
        public IntPixel(Integer[] rgb) {
            super(rgb[0], rgb[0], rgb[0]);
        }

        public IntPixel(Integer r, Integer g, Integer b) {
            super(r, g, b);
        }
    }


    public static class Data {
        public Mode mode;
        public int period;
        public int numPixels;
        public Pixel[] pixels;

        private Data() {} // only allow this class to do this

        public Data(Mode mode, int period, int numPixels, Pixel[] pixels) {
            this.mode = mode;
            this.period = period;
            this.numPixels = numPixels;
            this.pixels = pixels;
        }

        @Override
        public String toString() {
            return this.mode.value + ", " + this.period + ", " + this.numPixels + ", " + Arrays.toString(pixels);
        }

    }

    public static final void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch(Exception e) {

        }
    }

    public static final void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {

        }
    }


}
