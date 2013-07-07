package com.cantor.ipplan.shared;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class ChartOptions  implements Serializable, IsSerializable {
	public boolean all = true;
	public boolean excludeSelf = false;
	public boolean sales = true;
	public boolean onlyHead = false;

}
