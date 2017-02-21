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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.Arrays;
import java.util.UUID;


public class ColorPickerServiceFragment extends ServiceFragment {

    //TODO fix UUIDs
  /*private static final UUID COLOR_PICKER_SERVICE_UUID = UUID
      .fromString("00001812-0000-1000-8000-00805f9b34fb");
  private static final UUID COLOR_RGB_UUID = UUID
      .fromString("00002a9f-0000-1000-8000-00805f9b34fb");
  */
    private static final UUID COLOR_PICKER_SERVICE_UUID = UUID
            .fromString("0000180F-0000-1000-8000-00805f9b34fb");

    private static final UUID COLOR_RGB_UUID = UUID
            .fromString("00002A19-0000-1000-8000-00805f9b34fb");


    private static final UUID AUTOMATION_IO_UUID = UUID
            .fromString("00001815-0000-1000-8000-00805f9b34fb");

    private static final UUID ALERT_LEVEL_UUID = UUID
            .fromString("00002A06-0000-1000-8000-00805f9b34fb");


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

    private static final String TAG = ColorPickerServiceFragment.class.getName();

    private final OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                setColorRGB();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    // GATT
    private BluetoothGattService mColorPickerService;
    private BluetoothGattCharacteristic mColorRGBCharacteristic;

    public ColorPickerServiceFragment() {
        mColorRGBCharacteristic =
                new BluetoothGattCharacteristic(ALERT_LEVEL_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_BROADCAST,
                        BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);

        mColorRGBCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());

        mColorPickerService = new BluetoothGattService(AUTOMATION_IO_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mColorPickerService.addCharacteristic(mColorRGBCharacteristic);
    }


    private FrameLayout mLayout;
    // Lifecycle callbacks
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_color_picker, container, false);

        //TODO getParent
        mLayout = (FrameLayout) view.findViewById(R.id.colorCanvas);

        mRTextView = (TextView) view.findViewById(R.id.textView_colorR);
        mGTextView = (TextView) view.findViewById(R.id.textView_colorG);
        mBTextView = (TextView) view.findViewById(R.id.textView_colorB);
        mRSeekBar = (SeekBar) view.findViewById(R.id.seekBar_colorR);
        mRSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mGSeekBar = (SeekBar) view.findViewById(R.id.seekBar_colorG);
        mGSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mBSeekBar = (SeekBar) view.findViewById(R.id.seekBar_colorB);
        mBSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mHexTextView = (TextView) view.findViewById(R.id.textView_colorResult_hexVal);
        mRGBTextView = (TextView) view.findViewById(R.id.textView_colorResult_RGBVal);

        setColorRGB();
        return view;
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
        return mColorPickerService;
    }

    @Override
    public ParcelUuid getServiceUUID() {
        return new ParcelUuid(AUTOMATION_IO_UUID);
    }

    private  void setColorRGB(int[] value) {
        mRSeekBar.setProgress(value[0]);
        mGSeekBar.setProgress(value[1]);
        mBSeekBar.setProgress(value[2]);
        setColorRGB();
    }

    private static String toHex(int val) {
        return (val < 16 ? "0" : "") + Integer.toHexString(val).toUpperCase();
    }

    private void setColorRGB() {
        int r = mRSeekBar.getProgress();
        int g = mGSeekBar.getProgress();
        int b = mBSeekBar.getProgress();

        mRTextView.setText(Integer.toString(r));
        mGTextView.setText(Integer.toString(g));
        mBTextView.setText(Integer.toString(b));

        byte[] value = new byte[] {(byte)r, (byte)g, (byte)b};

        mColorRGBCharacteristic.setValue(value);

        //TODO add alpha
        mLayout.setBackgroundColor(Color.argb(255, r, g, b));
        mRGBTextView.setText(r+","+g+","+b);
        mHexTextView.setText("#"+toHex(r)+toHex(g)+toHex(b));

    }

    @Override
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, final byte[] value) {
        if (offset != 0) {
            return BluetoothGatt.GATT_INVALID_OFFSET;
        }



        /*
        if (value.length != 3) {
            return BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
        }
        */

        final int r, g, b;

        Log.d(TAG, "byte array size: "+value.length);

        if(value.length < 3) {

            Log.i(TAG, "Recieved Hex Color val: " + value[0]);

            // edited to support big numbers bigger than 0x80000000
            int color = (int) Long.parseLong((new Byte(value[0]).toString()), 16);
            r = (color >> 16) & 0xFF;
            g = (color >> 8) & 0xFF;
            b = (color >> 0) & 0xFF;

        } else if(value.length >= 3) {

            r = value[0] & 0xFF;
            g = value[1] & 0xFF;
            b = value[2] & 0xFF;
            Log.i(TAG, "Recieved RGB Color val: " + r + ", " + g + ", " + b);




        } else {
            return BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
        }



        //Log.d(value[0])

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setColorRGB(new int[] {r, g, b});
                //setColorRGB(new int[] {value[0] & 0xFF, value[1] & 0xFF, value[2] & 0xFF});
            }
        });

        return BluetoothGatt.GATT_SUCCESS;
    }
}