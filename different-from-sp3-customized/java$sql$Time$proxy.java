package org.apache.openjpa.util;

import java.io.ObjectStreamException;
import java.sql.Time;
import java.util.Date;
import org.apache.openjpa.kernel.OpenJPAStateManager;

public class java$sql$Time$proxy extends Time
  implements ProxyDate
{
  private transient OpenJPAStateManager sm;
  private transient int field;

  public java$sql$Time$proxy(long paramLong)
  {
    super(paramLong);
  }

  public java$sql$Time$proxy(int paramInt1, int paramInt2, int paramInt3)
  {
    super(paramInt1, paramInt2, paramInt3);
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

  public java$sql$Time$proxy()
  {
    super(System.currentTimeMillis());
  }

  public Object copy(Object paramObject)
  {
    return new Time(((Date)paramObject).getTime());
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

  public void setYear(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setYear(paramInt);
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

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return Proxies.writeReplace(this, true);
  }
}

/* Location:           C:\Users\srybak\dev\java\projects\3rd_party_customized\openjpa\branches\2.1.1\openjpa\target\openjpa-2.1.1-AXWAY-1\
 * Qualified Name:     org.apache.openjpa.util.java.sql.Time.proxy
 * JD-Core Version:    0.6.2
 */