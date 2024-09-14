package com.syc.usbrfidreader;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class ICReaderApi {
	Transfer transfer = null;

	public ICReaderApi(UsbDevice device, UsbManager manager) {
		transfer = new Transfer(device, manager);
	}

	int sendCommand(int command, byte[] sDATA, int sDLen, byte[] rDATA,
			byte[] Statue) {
		return transfer.sendCommand(command, sDATA, sDLen, rDATA, Statue);
	}

	void copyData(byte[] s, int spos, byte[] d, int dpos, int len) {
		transfer.copyData(s, spos, d, dpos, len);
	}

	byte[] toBytes(char[] c) {
		byte[] b = new byte[c.length];
		for (int i = 0; i < c.length; i++)
			b[i] = (byte) c[i];
		return b;
	}

	public int API_SetSerNum(byte[] newValue, byte[] buffer) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.SetSerlNum, newValue, 8, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_GetSerNum(byte[] buffer) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.GetSerlNum, null, 0, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int WriteUserInfo(int num_blk, int num_length, char[] user_info) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[121];
		DATA[0] = (byte) num_blk;
		DATA[1] = (byte) num_length;
		copyData(toBytes(user_info), 0, DATA, 2, num_length);
		int result = sendCommand(Define.Write_UserInfo, DATA, num_length + 2,
				toBytes(user_info), Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int ReadUserInfo(int num_blk, int num_length, char[] user_info) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[2];
		DATA[0] = (byte) num_blk;
		DATA[1] = (byte) num_length;
		int result = sendCommand(Define.Read_UserInfo, DATA, 2,
				toBytes(user_info), Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int GetVersionNum(char[] VersionNum) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.Get_VersionNum, null, 0,
				toBytes(VersionNum), Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ControlLED(byte freq, byte duration, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[2];
		DATA[0] = freq;
		DATA[1] = duration;
		int result = sendCommand(Define.Control_Led2, DATA, 2, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ControlBuzzer(int freq, int duration, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[2];
		DATA[0] = (byte) freq;
		DATA[1] = (byte) duration;
		int result = sendCommand(Define.Control_Buzzer, DATA, 2, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int MF_Request(byte inf_mode, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[1];
		DATA[0] = inf_mode;
		int result = sendCommand(Define.REQA, DATA, 1, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int MF_Anticoll(byte[] snr, byte[] status) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[5];
		int result = sendCommand(Define.Anticoll_A, null, 0, DATA, Statue);
		if (result != 0)
			return result;
		status[0] = DATA[0];
		copyData(DATA, 1, snr, 0, 4);
		return Statue[0];
	}

	public int MF_Select(byte[] snr) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.Select_A, snr, 4, snr, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int MF_Halt() {
		byte[] Statue = new byte[1];
		byte[] error = new byte[1];
		int result = sendCommand(Define.Halt_A, null, 0, error, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_PCDRead(byte mode, byte blk_add, byte num_blk, byte[] snr,
			byte[] buffer) {
		byte[] Statue = new byte[1];
		int num = (num_blk * 16 + 4) > 9 ? (num_blk * 16 + 4) : 9;
		byte[] DATA = new byte[num];
		DATA[0] = mode;
		DATA[1] = num_blk;
		DATA[2] = blk_add;
		copyData(snr, 0, DATA, 3, 6);
		int result = sendCommand(Define.MF_Read, DATA, 9, DATA, Statue);
		if (result != 0)
			return result;
		copyData(DATA, 0, snr, 0, 4);
		copyData(DATA, 4, buffer, 0, num_blk * 16);
		return Statue[0];
	}

	public int API_PCDWrite(byte mode, byte blk_add, byte num_blk, byte[] snr,
			byte[] buffer) {
		byte[] Statue = new byte[1];
		int num = num_blk * 16 + 9;
		byte[] DATA = new byte[num];
		DATA[0] = mode;
		DATA[1] = num_blk;
		DATA[2] = blk_add;
		copyData(snr, 0, DATA, 3, 6);
		copyData(buffer, 0, DATA, 9, num_blk * 16);
		int result = sendCommand(Define.MF_Write, DATA, num, DATA, Statue);
		if (result != 0)
			return result;
		copyData(DATA, 0, snr, 0, 4);
		return Statue[0];
	}

	public int API_PCDInitVal(byte mode, byte SectNum, byte[] snr, byte[] value) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[12];
		DATA[0] = mode;
		DATA[1] = SectNum;
		copyData(snr, 0, DATA, 2, 6);
		copyData(value, 0, DATA, 8, 4);
		int result = sendCommand(Define.MF_InitVal, DATA, 12, DATA, Statue);
		if (result != 0)
			return result;
		copyData(DATA, 0, snr, 0, 4);
		return Statue[0];
	}

	public int API_PCDDec(byte mode, byte SectNum, byte[] snr, byte[] value) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[12];
		DATA[0] = mode;
		DATA[1] = SectNum;
		copyData(snr, 0, DATA, 2, 6);
		copyData(value, 0, DATA, 8, 4);
		int result = sendCommand(Define.MF_Decrement, DATA, 12, DATA, Statue);
		if (result != 0)
			return result;
		copyData(DATA, 0, snr, 0, 4);
		copyData(DATA, 4, value, 0, 4);
		return Statue[0];
	}

	public int API_PCDInc(byte mode, byte SectNum, byte[] snr, byte[] value) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[12];
		DATA[0] = mode;
		DATA[1] = SectNum;
		copyData(snr, 0, DATA, 2, 6);
		copyData(value, 0, DATA, 8, 4);
		int result = sendCommand(Define.MF_Increment, DATA, 12, DATA, Statue);
		if (result != 0)
			return result;
		copyData(DATA, 0, snr, 0, 4);
		copyData(DATA, 4, value, 0, 4);
		return Statue[0];
	}

	public int GET_SNR(byte mode, byte API_halt, byte[] snr, byte[] value) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[5];
		DATA[0] = mode;
		DATA[1] = API_halt;
		int result = sendCommand(Define.MF_GET_SNR, DATA, 2, DATA, Statue);
		if (result != 0)
			return result;
		snr[0] = DATA[0];
		copyData(DATA, 1, value, 0, 4);
		return Statue[0];
	}

	public int MF_Restore(byte mode, int cardlength, byte[] carddata) {
		byte[] Statue = new byte[1];
		int num = cardlength + 2;
		byte[] DATA = new byte[num];
		DATA[0] = mode;
		DATA[1] = (byte) cardlength;
		copyData(carddata, 0, DATA, 2, cardlength);
		int result = sendCommand(Define.ISO14443_TypeA_Transfer_Command, DATA,
				num, carddata, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int RequestType_B(byte[] buffer) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.ReqB, null, 0, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int AntiType_B(byte[] buffer) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.AnticollB, null, 0, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int SelectType_B(byte[] SerialNum) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.Attrib_TypeB, SerialNum, 4, SerialNum,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int Request_AB(byte[] buffer) {
		byte[] Statue = new byte[1];
		int result = sendCommand(Define.Rst_TypeB, buffer, 4, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO14443TypeBTransCOSCmd(byte[] cmd, int cmdSize, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[256];
		DATA[0] = (byte) cmdSize;
		copyData(buffer, 0, DATA, 1, cmdSize);
		int result = sendCommand(Define.ISO14443_TypeB_Transfer_Command,
				buffer, cmdSize + 1, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693_Inventory(byte flag, byte afi, byte[] pData,
			byte[] nrOfCard, byte[] pBuffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[256];
		DATA[0] = flag;
		DATA[1] = afi;
		DATA[2] = 0;
		copyData(pData, 0, DATA, 3, 8);
		int result = sendCommand(Define.ISO14443_TypeB_Transfer_Command, DATA,
				11, DATA, Statue);
		if (result != 0)
			return result;
		nrOfCard[0] = DATA[0];
		copyData(DATA, 1, pBuffer, 0, 8 * DATA[0]);
		return Statue[0];
	}

	public int API_ISO15693Read(byte flags, byte blk_add, byte num_blk, byte[] uid,
			byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[11];
		DATA[0] = flags;
		DATA[1] = blk_add;
		DATA[2] = num_blk;
		num = 3;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 3, 8);
			num = 11;
		}
		int result = sendCommand(Define.ISO15693_Read, DATA, num, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693Write(byte flags, byte blk_add, byte num_blk, byte[] uid,
			byte[] data) {
		byte[] Statue = new byte[1];
		int num, k;
		byte[] DATA = new byte[256];
		k = 4;
		DATA[0] = flags;
		DATA[1] = blk_add;
		DATA[2] = num_blk;
		num = num_blk * k + 3;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 3, 8);
			num = num_blk * k + 11;
		}
		copyData(data, 0, DATA, 3, num_blk * 4);
		int result = sendCommand(Define.ISO15693_Write, DATA, num, data, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693Lock(byte flags, byte num_blk, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[10];
		DATA[0] = flags;
		DATA[1] = num_blk;
		num = 2;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 2, 8);
			num = 10;
		}
		int result = sendCommand(Define.ISO15693_Lockblock, DATA, num, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693StayQuiet(byte flags, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[9];
		DATA[0] = flags;
		copyData(uid, 0, DATA, 1, 8);
		int result = sendCommand(Define.ISO15693_StayQuiet, DATA, 9, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693Select(byte flags, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[9];
		DATA[0] = flags;
		copyData(uid, 0, DATA, 1, 8);
		int result = sendCommand(Define.ISO15693_Select, DATA, 9, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ResetToReady(byte flags, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[9];
		DATA[0] = flags;
		num = 1;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 1, 8);
			num = 9;
		}
		int result = sendCommand(Define.ISO15693_Resetready, DATA, num, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_WriteAFI(byte flags, byte afi, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[10];
		DATA[0] = flags;
		DATA[1] = afi;
		num = 2;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 2, 8);
			num = 10;
		}
		int result = sendCommand(Define.ISO15693_Write_Afi, DATA, num, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_LockAFI(byte flags, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[9];
		DATA[0] = flags;
		num = 1;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 1, 8);
			num = 9;
		}
		int result = sendCommand(Define.ISO15693_Lock_Afi, DATA, num, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_WriteDSFID(byte flags, byte DSFID, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[10];
		DATA[0] = flags;
		DATA[1] = DSFID;
		num = 2;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 2, 8);
			num = 10;
		}
		int result = sendCommand(Define.ISO15693_Write_Dsfid, DATA, num,
				buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_LockDSFID(byte flags, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[9];
		DATA[0] = flags;
		num = 1;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 1, 8);
			num = 9;
		}
		int result = sendCommand(Define.ISO15693_Lock_Dsfid, DATA, num, buffer,
				Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693_GetSysInfo(byte flags, byte[] uid, byte[] buffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[9];
		DATA[0] = flags;
		num = 1;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 1, 8);
			num = 9;
		}
		int result = sendCommand(Define.ISO15693_Get_Information, DATA, num,
				buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693_GetMulSecurity(byte flags, byte blkAddr, byte blkNum,
			byte[] uid, byte[] pBuffer) {
		byte[] Statue = new byte[1];
		int num;
		byte[] DATA = new byte[11];
		DATA[0] = flags;
		DATA[1] = blkAddr;
		DATA[2] = blkNum;
		num = 3;
		if (flags == 0x22) {
			copyData(uid, 0, DATA, 3, 8);
			num = 11;
		}
		int result = sendCommand(Define.ISO15693_Get_Multiple_Block_Security,
				DATA, num, pBuffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int API_ISO15693TransCOSCmd(byte[] cmd, int cmdSize, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[256];
		DATA[0] = (byte) cmdSize;
		copyData(buffer, 0, DATA, 1, cmdSize);
		int result = sendCommand(Define.ISO15693_Transfer_Command, DATA,
				cmdSize, buffer, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int UL_HLRead(byte mode, byte blk_add, byte[] snr, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[23];
		DATA[0] = mode;
		DATA[1] = blk_add;
		int result = sendCommand(Define.CMD_UL_HLRead, DATA, 2, DATA, Statue);
		if (result != 0)
			return result;
		copyData(DATA, 0, buffer, 0, 16);
		copyData(DATA, 16, snr, 0, 7);
		return Statue[0];
	}

	public int UL_HLWrite(byte mode, byte blk_add, byte[] snr, byte[] buffer) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[7];
		DATA[0] = mode;
		DATA[1] = 1;
		DATA[2] = blk_add;
		copyData(buffer, 0, DATA, 3, 4);
		int result = sendCommand(Define.CMD_UL_HLWrite, DATA, 7, snr, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}

	public int UL_Request(byte mode, byte[] snr) {
		byte[] Statue = new byte[1];
		byte[] DATA = new byte[1];
		DATA[0] = mode;
		int result = sendCommand(Define.CMD_UL_Request, DATA, 1, snr, Statue);
		if (result != 0)
			return result;
		return Statue[0];
	}
}
