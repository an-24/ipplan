package com.cantor.ipplan.client;

public class ClonableObject {
	
	protected native Object clone(Object r) /*-{    
    var o = this;
    for(var i in o){
       r[i] = o[i];
    }
    return r;
  	}-*/;
}
