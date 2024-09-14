package com.syc.usbrfidreader;

public class Define {
	static byte STX = (byte) 0xAA;
	static byte ETX = (byte) 0xBB;
	// ISO14443A
	static byte REQA = 0x03;
	static byte Anticoll_A = 0x04;
	static byte Select_A = 0x05;
	static byte Halt_A = 0x06;

	// ISO14443B
	static byte ReqB = 0x09;
	static byte AnticollB = 0x0A;
	static byte Attrib_TypeB = 0x0B;
	static byte Rst_TypeB = 0x0C;
	static byte ISO14443_TypeB_Transfer_Command = 0x0D;

	// MF

	static byte MF_Read = 0x20;
	static byte MF_Write = 0x21;
	static byte MF_InitVal = 0x22;
	static byte MF_Decrement = 0x23;
	static byte MF_Increment = 0x24;
	static byte MF_GET_SNR = 0x25;
	static byte ISO14443_TypeA_Transfer_Command = 0x28;

	// ISO15693
	static byte ISO15693_Inventory = 0x10;
	static byte ISO15693_Read = 0x11;
	static byte ISO15693_Write = 0x12;
	static byte ISO15693_Lockblock = 0x13;
	static byte ISO15693_StayQuiet = 0x14;
	static byte ISO15693_Select = 0x15;
	static byte ISO15693_Resetready = 0x16;
	static byte ISO15693_Write_Afi = 0x17;
	static byte ISO15693_Lock_Afi = 0x18;
	static byte ISO15693_Write_Dsfid = 0x19;
	static byte ISO15693_Lock_Dsfid = 0x1A;
	static byte ISO15693_Get_Information = 0x1B;
	static byte ISO15693_Get_Multiple_Block_Security = 0x1C;
	static byte ISO15693_Transfer_Command = 0x1D;

	// ultralight
	static byte CMD_UL_HLRead = (byte) 0xE0;
	static byte CMD_UL_HLWrite = (byte) 0xE1;
	static byte CMD_UL_Request = (byte) 0xE3;

	// system setting
	static byte SetAddress = (byte) 0x80;
	static byte SetBaudrate = (byte) 0x81;
	static byte SetSerlNum = (byte) 0x82;
	static byte GetSerlNum = (byte) 0x83;
	static byte Write_UserInfo = (byte) 0x84;
	static byte Read_UserInfo = (byte) 0x85;
	static byte Get_VersionNum = (byte) 0x86;
	static byte Control_Led1 = (byte) 0x87;
	static byte Control_Led2 = (byte) 0x88;
	static byte Control_Buzzer = (byte) 0x89;
}
