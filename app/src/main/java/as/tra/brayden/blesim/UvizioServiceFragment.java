/*
 * Copyright 2016 Attila Dusnoki
 * Copyright 2015 Google Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package as.tra.brayden.blesim;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;


public class UvizioServiceFragment extends ServiceFragment {

    private boolean active = false;

    private static final int NUM_PIXELS = 60;

    //TODO fix UUIDs

    private static final UUID RFDUINO_GENERIC_SERVICE_UUID = UUID
            .fromString("aba8a706-f28c-11e6-bc64-92361f002671");

    private static final UUID RFDUINO_GENERIC_READ_UUID = UUID
            .fromString("aba8a707-f28c-11e6-bc64-92361f002671");

    private static final UUID RFDUINO_GENERIC_WRITE_UUID = UUID
            .fromString("aba8a708-f28c-11e6-bc64-92361f002671");

    private static final UUID RFDUINO_GENERIC_DISCONNECT_UUID = UUID
            .fromString("aba8a709-f28c-11e6-bc64-92361f002671");


    private ServiceFragmentDelegate mDelegate;
    // UI
    private TextView mRTextView;
    private TextView mGTextView;
    private TextView mBTextView;

    private SeekBar mRSeekBar;
    private SeekBar mGSeekBar;
    private SeekBar mBSeekBar;

    private TextView mHexTextView;
    private TextView mRGBTextView;

    private Thread looper;

    private UVIZIOLED.Data data = UVIZIOLED.parseBytes(UVIZIOLED.DEFAULT_RECIEVE);

   // private static final int REFRESH_RATE = 60; // fps

    private static final String TAG = UvizioServiceFragment.class.getName();


    // GATT
    private BluetoothGattService mUvizioService;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mReadCharacteristic;
    private BluetoothGattCharacteristic mDisconnectCharacteristic;

    public UvizioServiceFragment() {
        mWriteCharacteristic =
                new BluetoothGattCharacteristic(RFDUINO_GENERIC_WRITE_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE |BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_BROADCAST,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        mReadCharacteristic =
                new BluetoothGattCharacteristic(RFDUINO_GENERIC_READ_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        mDisconnectCharacteristic =
                new BluetoothGattCharacteristic(RFDUINO_GENERIC_DISCONNECT_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        mWriteCharacteristic.addDescriptor(
                PeripheralActivity.getClientCharacteristicConfigurationDescriptor());

        mUvizioService = new BluetoothGattService(RFDUINO_GENERIC_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);


        mUvizioService.addCharacteristic(mReadCharacteristic);
        mUvizioService.addCharacteristic(mWriteCharacteristic);
        mUvizioService.addCharacteristic(mDisconnectCharacteristic);


    }


    private FrameLayout mCanvas;
    private FrameLayout[] innerViews = new FrameLayout[UvizioServiceFragment.NUM_PIXELS];
    // Lifecycle callbacks
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_uvizio, container, false);

        //TODO getParent
        mCanvas = (FrameLayout) view.findViewById(R.id.uvizioCanvas);

        for(int i = 0; i < UvizioServiceFragment.NUM_PIXELS; ++i) {
            FrameLayout inner = new FrameLayout(this.getActivity());
            inner.setMinimumWidth(5);
            inner.setMinimumHeight(5);
            innerViews[i] = inner;
            mCanvas.addView(inner);
        }



       // mHexTextView = (TextView) view.findViewById(R.id.textView_colorResult_hexVal);
        //mRGBTextView = (TextView) view.findViewById(R.id.textView_colorResult_RGBVal);

        active = true;
        Runnable loop = (new Runnable() {
            @Override
            public void run() {
                while(active) {
                    display(data);
                }
            }
        });
        looper = new Thread(loop);
        looper.start();

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        active = false;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDelegate = (ServiceFragmentDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ServiceFragmentDelegate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    public BluetoothGattService getBluetoothGattService() {
        return mUvizioService;
    }

    @Override
    public ParcelUuid getServiceUUID() {
        return new ParcelUuid(RFDUINO_GENERIC_SERVICE_UUID);
    }

    private static String toHex(int val) {
        //return (val < 16 ? "0" : "") + Integer.toHexString((byte)val).toUpperCase();
        return (val < 16 ? "0" : "") + String.format("%02X", val).toUpperCase();
    }


    private void display(final UVIZIOLED.Data data) {

        Log.d(TAG, "Updating data: "+data.toString());

        // this runs on a separate thread.
        // each function is responsible for sleeping this thread & calling the ui thread.
        switch(data.mode) {
            case OFF     : displayStaticOnUI(null);
            case STATIC  : displayStaticOnUI(data); break;
            case BLINK   : displayBlinkOnUI(data); break;
            case FRAMES  : displayFramesOnUI(data); break;
            case RAINBOW : displayRainbowOnUI(data); break;
        }


    }

    private void displayPixel(int idx, UVIZIOLED.Pixel p) {
        innerViews[idx].setBackgroundColor(Color.argb(255, p.r, p.g, p.g));
    }

    private void displayStatic(final UVIZIOLED.Pixel p) {
        //mRSeekBar.setProgress(value[0]);
        //mGSeekBar.setProgress(value[1]);
        //mBSeekBar.setProgress(value[2]);

        Log.d(TAG, "displaying static "+p.toString());

        //final CountDownLatch latch = new CountDownLatch(1);
        //getActivity().runOnUiThread(new Runnable() {
        //    @Override
        //    public void run() {

        //for(int i = 0; i < innerViews.length; ++i) {
        //    innerViews[i].setBackgroundColor(Color.argb(255, p.r, p.g, p.b));
        //}

                mCanvas.setBackgroundColor(Color.argb(255, p.r, p.g, p.b));
//                mRGBTextView.setText(p.r+","+p.g+","+p.b);
 //               mHexTextView.setText("#"+toHex(p.r)+toHex(p.g)+toHex(p.b));
        //        latch.countDown();
        //    }
        //});

        //try {
        //    latch.await();
         //   UVIZIOLED.delay(1000);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

       // UVIZIOLED.delay(1000);
    }

    private void displayStaticOnUI(final UVIZIOLED.Data data) {


        final int wait;
        final UVIZIOLED.Pixel p;

        if(data != null && data.pixels != null && data.numPixels > 0)
        {
            wait = data.period / data.numPixels;
            p = data.pixels[0];
        } else {
            wait = 5000;
            p = new UVIZIOLED.IntPixel(0,0,0);
        }

        final CountDownLatch UILatch = new CountDownLatch(1);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayStatic(p);
                UILatch.countDown();
            }
        });
        UVIZIOLED.await(UILatch);
        UVIZIOLED.delay(wait);



        //UVIZIOLED.delay(data.period);


    }

    private void displayBlinkOnUI(final UVIZIOLED.Data data) {

        final UVIZIOLED.Pixel p = data.pixels[0];

        final CountDownLatch UILatch1 = new CountDownLatch(1);
        final CountDownLatch UILatch2 = new CountDownLatch(1);
        final CountDownLatch loopLatch = new CountDownLatch(2);

        getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            displayStatic(p);
                                            UILatch1.countDown();
                                            //UVIZIOLED.await(LoopLatch);
                                        }
                                    });
        UVIZIOLED.await(UILatch1);
        UVIZIOLED.delay(data.period/2);
        loopLatch.countDown();


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                displayStatic(UVIZIOLED.PIXEL_OFF);
                UILatch2.countDown();
                //UVIZIOLED.await(LoopLatch);
            }
        });
        UVIZIOLED.await(UILatch2);
        UVIZIOLED.delay(data.period/2);
        loopLatch.countDown();

    }



    private void displayFramesOnUI(final UVIZIOLED.Data data) {

        final int wait = data.period / data.numPixels;

        //final CountDownLatch UILatch = new CountDownLatch(data.numPixels);
        //final CountDownLatch DataLatch = new CountDownLatch(data.numPixels);

        for(int i = 0; i < data.numPixels; i++) {
            final UVIZIOLED.Pixel p = data.pixels[i];
            final CountDownLatch UILatch = new CountDownLatch(1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayStatic(p);
                    UILatch.countDown();
                }
            });
            UVIZIOLED.await(UILatch);
            UVIZIOLED.delay(wait);
        }


        //UVIZIOLED.delay(data.period);


    }


    private void displayRainbowOnUI(final UVIZIOLED.Data data) {

        final int numPix = UvizioServiceFragment.NUM_PIXELS;
        final int wait = data.period / numPix;



        //final CountDownLatch UILatch = new CountDownLatch(data.numPixels);
        //final CountDownLatch DataLatch = new CountDownLatch(data.numPixels);



        UVIZIOLED.Pixel baseColor = new UVIZIOLED.IntPixel(255, 155, 55);
        for(int i = 0; i < 256; i++) { // for each frame
            final UVIZIOLED.Pixel p = baseColor.add(i);
            final int thisFrame = i + 1;
            final CountDownLatch UILatch = new CountDownLatch(1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayStatic(p);
                    UILatch.countDown();
                    //UVIZIOLED.delay(50);
                    /*
                    for (int j = 0; j < numPix; ++j) {

                        int thisPix = j + 1;
                        UVIZIOLED.Pixel pixColor = baseColor.add(j);
                        displayStatic(pixColor);
                        UVIZIOLED.delay(50);
                    }
                    */

                }


            });
            UVIZIOLED.await(UILatch);
            UVIZIOLED.delay(10);
        }

    }

    @Override
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, final byte[] value) {
        if (offset != 0) {
            return BluetoothGatt.GATT_INVALID_OFFSET;
        }

        for(int i = 0; i < value.length; i++){
      //      Log.d(TAG, "Recieved bit:"+ (value[i] & 0xFF) +" "+toHex(value[i] & 0xFF));
        }
        data = UVIZIOLED.parseBytes(value);

       // Log.d(TAG, "Parsed data: "+data);

        /*
        if (value.length != 3) {
            return BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
        }
        */

        //Log.d(TAG, "byte array size: "+value.length);

        //Log.d(value[0])


        return BluetoothGatt.GATT_SUCCESS;
    }
}