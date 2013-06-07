package com.cantor.ipplan.shared;


@SuppressWarnings("serial")
public class BargaincostsWrapper  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public int bargaincostsId;
	public CostsWrapper cost;
	public BargainWrapper bargain; 
	public int bargaincostsValue;
	public int bargaincostsPayment;
	public String bargaincostsNote;
	
	public BargaincostsWrapper copy() {
		BargaincostsWrapper wrap = new BargaincostsWrapper();
		
		wrap.bargaincostsId=bargaincostsId;
		wrap.cost=cost;  // not copy
		wrap.bargain=bargain; 
		wrap.bargaincostsValue=bargaincostsValue;
		wrap.bargaincostsPayment=bargaincostsPayment;
		wrap.bargaincostsNote=bargaincostsNote;
		
		return wrap;
	}
}
