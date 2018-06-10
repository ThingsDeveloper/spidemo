package com.chengxiang.spidemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.SpiDevice;
import com.google.android.things.pio.PeripheralManager;

import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends Activity {
    private static final String SPI_DEVICE_NAME = "SPI0.0";
    private static final String TAG= MainActivity.class.getSimpleName();

    private static final byte OP_NOOP = 0;
    private static final byte OP_DIGIT0 = 1;
    private static final byte OP_DIGIT1 = 2;
    private static final byte OP_DIGIT2 = 3;
    private static final byte OP_DIGIT3 = 4;
    private static final byte OP_DIGIT4 = 5;
    private static final byte OP_DIGIT5 = 6;
    private static final byte OP_DIGIT6 = 7;
    private static final byte OP_DIGIT7 = 8;
    private static final byte OP_DECODEMODE = 9;
    private static final byte OP_INTENSITY = 10;
    private static final byte OP_SCANLIMIT = 11;
    private static final byte OP_SHUTDOWN = 12;
    private static final byte OP_DISPLAYTEST = 15;

    // java二进制的写法
    public static final byte[] ALIEN_FRAME_1 = new byte[]{
            (byte)0b00001000,
            (byte)0b00011000,
            (byte)0b00001000,
            (byte)0b00001000,
            (byte)0b00001000,
            (byte)0b00001000,
            (byte)0b00001000,
            (byte)0b00000000
    };

    public static final byte[] ALIEN_FRAME_2 = new byte[]{
            (byte)0b00011000,
            (byte)0b00100100,
            (byte)0b00100100,
            (byte)0b00000100,
            (byte)0b00001000,
            (byte)0b00010000,
            (byte)0b00111110,
            (byte)0b00000000
    };
    public static final byte[] ALIEN_FRAME_3 = new byte[]{
            (byte)0b00011000,
            (byte)0b00100100,
            (byte)0b00000100,
            (byte)0b00011000,
            (byte)0b00000100,
            (byte)0b00100100,
            (byte)0b00011000,
            (byte)0b00000000
    };

    public static final byte[][] FRAMES = new byte[][]{ALIEN_FRAME_1, ALIEN_FRAME_2, ALIEN_FRAME_3};
    private SpiDevice mDevice;
    private byte[] spidata = new byte[2];

    /*
    对比SPI和I2C:
    https://blog.csdn.net/p106786860/article/details/71076450
    SPI总线是一种同步的串行接口(CLK),支持全双工数据传输(2条单独的信号线)，slave设备使用硬件寻址(片选引脚)

    https://github.com/ThingsDeveloper/i2cdemo,
    2C总线是一种同步的串行接口(CLK),支持半双工通信(数据都是通过一根线连接),使用I2C软件协议寻址
    官方描述https://developer.android.google.cn/things/sdk/pio/spi
    MODE0 - Clock signal idles low, data is transferred on the leading clock edge
    MODE1 - Clock signal idles low, data is transferred on the trailing clock edge
    MODE2 - Clock signal idles high, data is transferred on the leading clock edge
    MODE3 - Clock signal idles high, data is transferred on the trailing clock edge
    */
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 线路接线：5v,GND,ce0片选，MOSI,SCLK (因为不用读spi设备，故不用miso)
        try {
            //打开SPI接口连接,
            PeripheralManager manager = PeripheralManager.getInstance();
            mDevice = manager.openSpiDevice(SPI_DEVICE_NAME);

            //设置SPI：信号模式、频率、每个字比特数等
            /*
             Frequency-设置共享时钟信号的频率，时钟信号的能力在不同的设备之间有所不同。在设置这个值之前，
                       你应该确认下你设备支持的频率。
             Justification - Specifies the ordering of the individual bits in each byte as they are
             transferred across the bus. This is also known as the endianness of the data. By default,
             data will be sent with the most significant bit (MSB) first.
             setBitsPerWord：配置一次传输的比特数，…，默认的值是8比特每字。
             */
            mDevice.setMode(SpiDevice.MODE0);
            mDevice.setFrequency(1000000);
            mDevice.setBitsPerWord(8);
            mDevice.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);

            //设置译码模式，0-用于驱动LED点阵屏ß
            spiTransfer(OP_DECODEMODE, 0);
            //设置扫描限制，显示7行
            spiTransfer(OP_SCANLIMIT, 7);
            //设置显示器检测，0-一般模式
            spiTransfer(OP_DISPLAYTEST, 0);
            //设置停机为false
            spiTransfer(OP_SHUTDOWN, 1);
            //设置显示强度为3
            spiTransfer(OP_INTENSITY, 15);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 利用rxjava,在io线程上进行和spi设备进行通信
        Observable.just(1).observeOn(Schedulers.io()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                try {
                    while (true) {
                        for (int i = 0; i < FRAMES.length; i++) {
                            for (int j = 0; j < FRAMES[i].length; j++) {
                                spiTransfer((byte) (OP_DIGIT0 + j), FRAMES[i][j]);
                            }
                            Thread.sleep(500);
                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDevice != null) {
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 往spi写数据
    private void spiTransfer(byte opcode, int data) throws IOException {
        spidata[0] = opcode;
        spidata[1] = (byte) data;
        mDevice.write(spidata, 2);
    }
}
