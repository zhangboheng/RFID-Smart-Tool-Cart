package com.syc.usbrfidreader;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

public class Transfer {
	final int DATALEN = 0x100;
	final int BUFLEN = DATALEN - 8;
	final int FALSE = -1;
	final int TRUE = 0;

	byte Buffer[] = new byte[BUFLEN];
	byte DATA[] = new byte[DATALEN];
	int p = 1;
	UsbDevice mUsbDevice = null;
	UsbManager mUsbManager = null;
	UsbDeviceConnection connection = null;
	UsbInterface intf = null;

	public Transfer(UsbDevice device, UsbManager manager) {
		mUsbDevice = device;
		mUsbManager = manager;
		intf = device.getInterface(0);
	}

	int openCom() {
		if (mUsbDevice == null || intf == null)
			return FALSE;
		if (connection != null)
			closeCom();
		connection = mUsbManager.openDevice(mUsbDevice);
		if (connection == null)
			return FALSE;
		if (connection.claimInterface(intf, true) == false)
			return FALSE;
		return TRUE;
	}

	void closeCom() {
		if (connection != null) {
			if (intf != null)
				connection.releaseInterface(intf);
			connection.close();
		}
		connection = null;
	}

	void copyData(byte[] s, int spos, byte[] d, int dpos, int len) {
		int i;
		for (i = 0; i < len; i++) {
			d[i + dpos] = s[i + spos];
		}
	}

	byte checkData(byte[] data, int h, int t) {
		byte check;
		int i;
		check = data[h];
		for (i = h + 1; i <= t; i++) {
			check = (byte) (check ^ data[i]);
		}
		return check;
	}

	void writeBuffer(byte data) {
		Buffer[p] = data;
		p++;
	}

	void writeBuffers(byte[] data, int length) {
		int i;
		for (i = 0; i < length; i++)
			writeBuffer(data[i]);
	}

	void clearBuffer() {
		p = 1;
		for (int i = 0; i < DATALEN; i++)
			DATA[i] = 0;
		for (int i = 0; i < BUFLEN; i++)
			Buffer[i] = 0;
		Buffer[0] = Define.STX;
		DATA[0] = 0x01;
	}

	int writeCom(int length) {
		int recieve;
		DATA[6] = (byte) length;
		for (int i = 0; i < BUFLEN; i++)
			DATA[i + 8] = Buffer[i];
		recieve = connection.controlTransfer(0x21, 0x09, 0x301, 0, DATA, 0x100,
				500);
		if (recieve < 0 || recieve != DATALEN)
			return -1;
		return length;
	}

	int readCom(int length) {
		int recieve;
		recieve = connection.controlTransfer(0xa1, 0x01, 0x302, 0, DATA, 0x100,
				500);
		if (recieve < 0)
			return -1;
		for (int i = 0; i < BUFLEN; i++)
			Buffer[i] = DATA[i + 8];
		return recieve - 8;
	}

	int sendData() {
		int length;
		byte BCC;
		int i;
		BCC = checkData(Buffer, 1, p - 1);
		writeBuffer(BCC);
		writeBuffer(Define.ETX);

		System.out.printf("Send data:");
		for (i = 0; i < p; i++)
			System.out.printf("%02x ", Buffer[i]);
		System.out.printf("\n");

		if (openCom() == FALSE) {
			closeCom();
			return FALSE;
		}
		length = writeCom(p);
		System.out.printf("length send: %02x\n", length);

		if (length != p)
			return 0x05;

		length = readCom(248);

		closeCom();

		System.out.printf("length read: %02x\n", length);

		p = length;

		System.out.printf("Recieve data:");
		for (i = 0; i < length; i++)
			System.out.printf("%02x ", Buffer[i]);
		System.out.printf("\n");

		if (p < 6 || Buffer[0] != Define.STX || Buffer[p - 1] != Define.ETX
				|| Buffer[2] + 5 != p)
			return 0x05;
		if (checkData(Buffer, 1, p - 3) != Buffer[p - 2])
			return 0x02;
		return 0;
	}

	public int sendCommand(int command, byte[] sDATA, int sDLen, byte[] rDATA,
			byte[] Statue) {
		int result;
		clearBuffer();
		writeBuffer((byte) 0x00);
		writeBuffer((byte) (sDLen + 1));
		writeBuffer((byte) command);
		writeBuffers(sDATA, sDLen);

		result = sendData();

		if (result != 0)
			return result;
		copyData(Buffer, 4, rDATA, 0, Buffer[2] - 1);
		Statue[0] = Buffer[3];
		return 0;
	}
}
