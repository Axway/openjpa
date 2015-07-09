package org.apache.openjpa.util;

import java.io.ObjectStreamException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.openjpa.kernel.OpenJPAStateManager;

public class java$util$GregorianCalendar$proxy extends GregorianCalendar
  implements ProxyCalendar
{
  private transient OpenJPAStateManager sm;
  private transient int field;

  public java$util$GregorianCalendar$proxy(TimeZone paramTimeZone)
  {
    super(paramTimeZone);
  }

  public java$util$GregorianCalendar$proxy(Locale paramLocale)
  {
    super(paramLocale);
  }

  public java$util$GregorianCalendar$proxy(TimeZone paramTimeZone, Locale paramLocale)
  {
    super(paramTimeZone, paramLocale);
  }

  public java$util$GregorianCalendar$proxy(int paramInt1, int paramInt2, int paramInt3)
  {
    super(paramInt1, paramInt2, paramInt3);
  }

  public java$util$GregorianCalendar$proxy(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    super(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
  }

  public java$util$GregorianCalendar$proxy()
  {
  }

  public java$util$GregorianCalendar$proxy(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    super(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
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
    void tmp7_4 = new GregorianCalendar();
    tmp7_4.setTimeInMillis(((Calendar)paramObject).getTimeInMillis());
    void tmp18_7 = tmp7_4;
    tmp18_7.setLenient(((Calendar)paramObject).isLenient());
    void tmp29_18 = tmp18_7;
    tmp29_18.setFirstDayOfWeek(((Calendar)paramObject).getFirstDayOfWeek());
    void tmp40_29 = tmp29_18;
    tmp40_29.setMinimalDaysInFirstWeek(((Calendar)paramObject).getMinimalDaysInFirstWeek());
    void tmp51_40 = tmp40_29;
    tmp51_40.setTimeZone(((Calendar)paramObject).getTimeZone());
    return tmp51_40;
  }

  public ProxyCalendar newInstance()
  {
    return new proxy();
  }

  protected void computeFields()
  {
    Proxies.dirty(this, true);
    super.computeFields();
  }

  public void add(int paramInt1, int paramInt2)
  {
    Proxies.dirty(this, true);
    super.add(paramInt1, paramInt2);
  }

  public void setTimeZone(TimeZone paramTimeZone)
  {
    Proxies.dirty(this, true);
    super.setTimeZone(paramTimeZone);
  }

  public void roll(int paramInt, boolean paramBoolean)
  {
    Proxies.dirty(this, true);
    super.roll(paramInt, paramBoolean);
  }

  public void roll(int paramInt1, int paramInt2)
  {
    Proxies.dirty(this, true);
    super.roll(paramInt1, paramInt2);
  }

  public void setGregorianChange(Date paramDate)
  {
    Proxies.dirty(this, true);
    super.setGregorianChange(paramDate);
  }

  public void set(int paramInt1, int paramInt2)
  {
    Proxies.dirty(this, true);
    super.set(paramInt1, paramInt2);
  }

  public void setLenient(boolean paramBoolean)
  {
    Proxies.dirty(this, true);
    super.setLenient(paramBoolean);
  }

  public void setTimeInMillis(long paramLong)
  {
    Proxies.dirty(this, true);
    super.setTimeInMillis(paramLong);
  }

  public void setFirstDayOfWeek(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setFirstDayOfWeek(paramInt);
  }

  public void setMinimalDaysInFirstWeek(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setMinimalDaysInFirstWeek(paramInt);
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return Proxies.writeReplace(this, true);
  }
}

/* Location:           C:\Users\srybak\dev\java\projects\GI\repository\5.12.0.0.x-acf\corelib\openjpa-2.1.1-CUSTOMIZED\
 * Qualified Name:     org.apache.openjpa.util.java.util.GregorianCalendar.proxy
 * JD-Core Version:    0.6.2
 */