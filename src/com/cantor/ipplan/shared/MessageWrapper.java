package com.cantor.ipplan.shared;

import java.io.Serializable;
import java.util.Date;

public class MessageWrapper implements Serializable {
	public int messageId;
	public PUserWrapper sender;
	public PUserWrapper reciever;
	public Date datetime;
	public String text;
	public int type;
}
