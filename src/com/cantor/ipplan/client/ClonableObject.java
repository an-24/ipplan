package com.cantor.ipplan.client;

public class ClonableObject {
	
	protected native Object clone(Object r) /*-{    

    // prevents to use same hash code
    @com.google.gwt.core.client.impl.Impl::getHashCode(Ljava/lang/Object;)(r);

    var o = this;
    for(var i in o){
        if(!(i in r)){
            r[i] = o[i];
        }
    }
    return r;
  	}-*/;
}
