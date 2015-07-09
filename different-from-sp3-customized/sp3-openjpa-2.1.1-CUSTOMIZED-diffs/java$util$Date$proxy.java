package org.apache.openjpa.util;

import java.io.ObjectStreamException;
import java.util.Date;
import org.apache.openjpa.kernel.OpenJPAStateManager;

public class java$util$Date$proxy extends Date
  implements ProxyDate
{
  private transient OpenJPAStateManager sm;
  private transient int field;

  public java$util$Date$proxy()
  {
  }

  public java$util$Date$proxy(long paramLong)
  {
    super(paramLong);
  }

  public java$util$Date$proxy(int paramInt1, int paramInt2, int paramInt3)
  {
    super(paramInt1, paramInt2, paramInt3);
  }

  public java$util$Date$proxy(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    super(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
  }

  public java$util$Date$proxy(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    super(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  public java$util$Date$proxy(String paramString)
  {
    super(paramString);
  }

  public void setOwner(OpenJPAStateManager paramOpenJPAStateManager, int paramInt)
  {
    this.sm = paramOpenJPAStateManager;
    this.field = paramInt;
  }

  public OpenJPAStateManager getOwner()
  {
    return this.sm;
  }

  public int getOwnerField()
  {
    return this.field;
  }

  public Object clone()
  {
    Proxy localProxy = (Proxy)super.clone();
    localProxy.setOwner(null, 0);
    return localProxy;
  }

  public ChangeTracker getChangeTracker()
  {
    return null;
  }

  public Object copy(Object paramObject)
  {
    return new Date(((Date)paramObject).getTime());
  }

  public ProxyDate newInstance()
  {
    return new proxy();
  }

  public void setTime(long paramLong)
  {
    Proxies.dirty(this, true);
    super.setTime(paramLong);
  }

  public void setDate(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setDate(paramInt);
  }

  public void setMonth(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setMonth(paramInt);
  }

  public void setHours(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setHours(paramInt);
  }

  public void setMinutes(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setMinutes(paramInt);
  }

  public void setSeconds(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setSeconds(paramInt);
  }

  public void setYear(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setYear(paramInt);
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return Proxies.writeReplace(this, true);
  }
}

/* Location:           C:\Users\srybak\dev\java\projects\GI\repository\5.12.0.0.x-acf\corelib\openjpa-2.1.1-CUSTOMIZED\
 * Qualified Name:     org.apache.openjpa.util.java.util.Date.proxy
 * JD-Core Version:    0.6.2
 */