package com.quester.scard;

public interface SCardPcscLite {
	
	/** No error was encountered */
	public static final int SCARD_S_SUCCESS = 0x0000;
	
	/** An internal consistency check failed */
	public static final int SCARD_F_INTERNAL_ERROR = 0x0001;
	/** The action was cancelled by an SCardCancel request */
	public static final int SCARD_E_CANCELLED = 0x0002;
	/** The supplied handle was invalid */
	public static final int SCARD_E_INVALID_HANDLE = 0x0003;
	/** One or more of the supplied parameters could not be properly interpreted */
	public static final int SCARD_E_INVALID_PARAMETER = 0x0004;
	/** Registry startup information is missing or invalid */
	public static final int SCARD_E_INVALID_TARGET = 0x0005;
	/** Not enough memory available to complete this command */
	public static final int SCARD_E_NO_MEMORY = 0x0006;
	/** An internal consistency timer has expired */
	public static final int SCARD_F_WAITED_TOO_LONG = 0x0007;
	/** The data buffer to receive returned data is too small for the returned data */
	public static final int SCARD_E_INSUFFICIENT_BUFFER = 0x0008;
	/** The specified reader name is not recognized */
	public static final int SCARD_E_UNKNOWN_READER = 0x0009;
	/** The user-specified timeout value has expired */
	public static final int SCARD_E_TIMEOUT = 0x000A;
	/** The smart card cannot be accessed because of other connections outstanding */
	public static final int SCARD_E_SHARING_VIOLATION = 0x000B;
	/** The operation requires a Smart Card, but no Smart Card is currently in the device */
	public static final int SCARD_E_NO_SMARTCARD = 0x000C;
	/** The specified smart card name is not recognized */
	public static final int SCARD_E_UNKNOWN_CARD = 0x000D;
	/** The system could not dispose of the media in the requested manner */
	public static final int SCARD_E_CANT_DISPOSE = 0x000E;
	/** The requested protocols are incompatible with the protocol currently in use with the smart card */
	public static final int SCARD_E_PROTO_MISMATCH = 0x000F;
	/** The reader or smart card is not ready to accept commands */
	public static final int SCARD_E_NOT_READY = 0x0010;
	/** One or more of the supplied parameters values could not be properly interpreted */
	public static final int SCARD_E_INVALID_VALUE = 0x0011;
	/** The action was cancelled by the system, presumably to log off or shut down */
	public static final int SCARD_E_SYSTEM_CANCELLED = 0x0012;
	/** An internal communications error has been detected */
	public static final int SCARD_F_COMM_ERROR = 0x0013;
	/** An internal error has been detected, but the source is unknown */
	public static final int SCARD_F_UNKNOWN_ERROR = 0x0014;
	/** An ATR obtained from the registry is not a valid ATR string */
	public static final int SCARD_E_INVALID_ATR = 0x0015;
	/** An attempt was made to end a non-existent transaction */
	public static final int SCARD_E_NOT_TRANSACTED = 0x0016;
	/** The specified reader is not currently available for use */
	public static final int SCARD_E_READER_UNAVAILABLE = 0x0017;
	/** The operation has been aborted to allow the server application to exit */
	public static final int SCARD_P_SHUTDOWN = 0x0018;
	/** The PCI Receive buffer was too small */
	public static final int SCARD_E_PCI_TOO_SMALL = 0x0019;
	/** The reader driver does not meet minimal requirements for support */
	public static final int SCARD_E_READER_UNSUPPORTED = 0x001A;
	/** The reader driver did not produce a unique reader name */
	public static final int SCARD_E_DUPLICATE_READER = 0x001B;
	/** The smart card does not meet minimal requirements for support */
	public static final int SCARD_E_CARD_UNSUPPORTED = 0x001C;
	/** The Smart card resource manager is not running */
	public static final int SCARD_E_NO_SERVICE = 0x001D;
	/** The Smart card resource manager has shut down */
	public static final int SCARD_E_SERVICE_STOPPED = 0x001E;
	/** An unexpected card error has occurred */
	public static final int SCARD_E_UNEXPECTED = 0x001F;
	/** This smart card does not support the requested feature */
	public static final int SCARD_E_UNSUPPORTED_FEATURE = 0x001F;
	/** No primary provider can be found for the smart card */
	public static final int SCARD_E_ICC_INSTALLATION = 0x0020;
	/** The requested order of object creation is not supported */
	public static final int SCARD_E_ICC_CREATEORDER = 0x0021;
	/** The identified directory does not exist in the smart card */
	public static final int SCARD_E_DIR_NOT_FOUND = 0x0023;
	/** The identified file does not exist in the smart card */ 
	public static final int SCARD_E_FILE_NOT_FOUND = 0x0024;
	/** The supplied path does not represent a smart card directory */
	public static final int SCARD_E_NO_DIR = 0x0025;
	/** The supplied path does not represent a smart card file */
	public static final int SCARD_E_NO_FILE = 0x0026;
	/** Access is denied to this file */
	public static final int SCARD_E_NO_ACCESS = 0x0027;
	/** The smart card does not have enough memory to store the information */
	public static final int SCARD_E_WRITE_TOO_MANY = 0x0028;
	/** There was an error trying to set the smart card file object pointer */
	public static final int SCARD_E_BAD_SEEK = 0x0029;
	/** The supplied PIN is incorrect */
	public static final int SCARD_E_INVALID_CHV = 0x002A;
	/** An unrecognized error code was returned from a layered component */ 
	public static final int SCARD_E_UNKNOWN_RES_MNG = 0x002B;
	/** The requested certificate does not exist */
	public static final int SCARD_E_NO_SUCH_CERTIFICATE = 0x002C;
	/** The requested certificate could not be obtained */
	public static final int SCARD_E_CERTIFICATE_UNAVAILABLE = 0x002D;
	/** Cannot find a smart card reader */
	public static final int SCARD_E_NO_READERS_AVAILABLE = 0x002E;
	/** A communications error with the smart card has been detected. Retry the operation */
	public static final int SCARD_E_COMM_DATA_LOST = 0x002F;
	/** The requested key container does not exist on the smart card */
	public static final int SCARD_E_NO_KEY_CONTAINER = 0x0030;
	/** The Smart Card Resource Manager is too busy to complete this operation */
	public static final int SCARD_E_SERVER_TOO_BUSY = 0x0031;
	
	/** The reader cannot communicate with the card, due to ATR string configuration conflicts */
	public static final int SCARD_W_UNSUPPORTED_CARD = 0x0065;
	/** The smart card is not responding to a reset */
	public static final int SCARD_W_UNRESPONSIVE_CARD = 0x0066;
	/** Power has been removed from the smart card, so that further communication is not possible */
	public static final int SCARD_W_UNPOWERED_CARD = 0x0067;
	/** The smart card has been reset, so any shared state information is invalid */
	public static final int SCARD_W_RESET_CARD = 0x0068;
	/** The smart card has been removed, so further communication is not possible */
	public static final int SCARD_W_REMOVED_CARD = 0x0069;
	
	/** Access was denied because of a security violation */
	public static final int SCARD_W_SECURITY_VIOLATION = 0x006A;
	/** The card cannot be accessed because the wrong PIN was presented */
	public static final int SCARD_W_WRONG_CHV = 0x006B;
	/** The card cannot be accessed because the maximum number of PIN entry attempts has been reached */
	public static final int SCARD_W_CHV_BLOCKED = 0x006C;
	/** The end of the smart card file has been reached */
	public static final int SCARD_W_EOF = 0x006D;
	/** The user pressed "Cancel" on a Smart Card Selection Dialog */
	public static final int SCARD_W_CANCELLED_BY_USER = 0x006E;
	/** No PIN was presented to the smart card */
	public static final int SCARD_W_CARD_NOT_AUTHENTICATED = 0x006F;
	
	/** See freeSCardMemory() */
	public static final int SCARD_AUTOALLOCATE = -1;
	/** Scope in user space */
	public static final int SCARD_SCOPE_USER = 0x0000;
	/** Scope in terminal */
	public static final int SCARD_SCOPE_TERMINAL = 0x0001;
	/** Scope in system */
	public static final int SCARD_SCOPY_SYSTEM = 0x0002;
	
	/** Exclusive mode only */
	public static final int SCARD_SHARE_EXCLUSIVE = 0x0001;
	/** Shared mode only */
	public static final int SCARD_SHARE_SHARED = 0x0002;
	/** Raw mode only */
	public static final int SCARD_SHARE_DIRECT = 0x0003;
	
	/** Protocol not set */
	public static final int SCARD_PROTOCOL_UNDEFINED = 0x0000;
	/** Backward compat */
	public static final int SCARD_PROTOCOL_UNSET = 0x0000;
	/** T=0 active protocol */
	public static final int SCARD_PROTOCOL_T0 = 0x0001;
	/** T=1 active protocol */
	public static final int SCARD_PROTOCOL_T1 = 0x0002;
	/** Raw active protocol */
	public static final int SCARD_PROTOCOL_RAW = 0x0004;
	/** T=15 protocol */
	public static final int SCARD_PROTOCOL_T15 = 0x0008;
	/** IFD determines protocol */
	public static final int SCARD_PROTOCOL_ANY = (SCARD_PROTOCOL_T0|SCARD_PROTOCOL_T1);
	
	/** Do nothing on close */
	public static final int SCARD_LEAVE_CARD = 0x0000;
	/** Reset on close */
	public static final int SCARD_RESET_CARD = 0x0001;
	/** Power down on close */
	public static final int SCARD_UNPOWER_CARD = 0x0002;
	/** Eject on close */
	public static final int SCARD_EJECT_CARD = 0x0003;
	
	/** Unknown state */
	public static final int SCARD_UNKNOWN = 0x0001;
	/** Card is absent */
	public static final int SCARD_ABSENT = 0x0002;
	/** Card is present */
	public static final int SCARD_PRESENT = 0x0004;
	/** Card not powered */
	public static final int SCARD_SWALLOWED = 0x0008;
	/** Card os powered */
	public static final int SCARD_POWERED = 0x0010;
	/** Ready for PTS */
	public static final int SCARD_NEGOTIABLE = 0x0020;
	/** PTS has been set */
	public static final int SCARD_SPECIFIC = 0x0040;
	
	/** App wants status */
	public static final int SCARD_STATE_UNAWARE = 0x0000;
	/** Ignore this reader */
	public static final int SCARD_STATE_IGNORE = 0x0001;
	/** State has changed */
	public static final int SCARD_STATE_CHANGED = 0x0002;
	/** Reader unknown */
	public static final int SCARD_STATE_UNKNOWN = 0x0004;
	/** Status unavailable */
	public static final int SCARD_STATE_UNAVAILABLE = 0x0008;
	/** Card removed */
	public static final int SCARD_STATE_EMPTY = 0x0010;
	/** Card inserted */
	public static final int SCARD_STATE_PRESENT = 0x0020;
	/** ATR matchs card */
	public static final int SCARD_STATE_ATRMATCH = 0x0040;
	/** Exclusive Mode */
	public static final int SCARD_STATE_EXCLUSIVE = 0x0080;
	/** Shared Mode */
	public static final int SCARD_STATE_INUSE = 0x0100;
	/** Unresponsive card */
	public static final int SCARD_STATE_MUTE = 0x0200;
	/** Unpowered card */
	public static final int SCARD_STATE_UNPOWERED = 0x0400;
	
	/** Maximum ATR size */
	public static final int MAX_ATR_SIZE = 33;
	/** Maximum reader name length */
	public static final int MAX_READERNAME = 128;
	
	/*
	 * The message and buffer sizes must be multiples of 16.
	 * The max message size must be at least large enough
	 * to accomodate the transmit_struct
	 */
	/** Maximum Tx/Rx buffer for short APDU */
	public static final int MAX_BUFFER_SIZE = 264;
	/** Enhanced (64K + APDU + Lc + Le + SW) Tx/Rx buffer */
	public static final int MAX_BUFFER_SIZE_EXTENDED = (4 + 3 + (1<<16) + 3 + 2);
}
