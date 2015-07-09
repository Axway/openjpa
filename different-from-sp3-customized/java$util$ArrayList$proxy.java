package org.apache.openjpa.util;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import org.apache.openjpa.kernel.OpenJPAStateManager;

public class java$util$ArrayList$proxy extends ArrayList
  implements ProxyCollection
{
  private transient OpenJPAStateManager sm;
  private transient int field;
  private transient CollectionChangeTracker changeTracker;
  private transient Class elementType;

  public java$util$ArrayList$proxy()
  {
  }

  public java$util$ArrayList$proxy(Collection paramCollection)
  {
    super(paramCollection);
  }

  public java$util$ArrayList$proxy(int paramInt)
  {
    super(paramInt);
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
    return this.changeTracker;
  }

  public Object copy(Object paramObject)
  {
    return new ArrayList((Collection)paramObject);
  }

  public Class getElementType()
  {
    return this.elementType;
  }

  public ProxyCollection newInstance(Class paramClass, Comparator paramComparator, boolean paramBoolean1, boolean paramBoolean2)
  {
    proxy localproxy = new proxy();
    localproxy.elementType = paramClass;
    if (paramBoolean1)
      localproxy.changeTracker = new CollectionChangeTrackerImpl(localproxy, true, true, paramBoolean2);
    return localproxy;
  }

  public boolean add(Object paramObject)
  {
    ProxyCollections.beforeAdd(this, paramObject);
    boolean bool = super.add(paramObject);
    return ProxyCollections.afterAdd(this, paramObject, bool);
  }

  public void add(int paramInt, Object paramObject)
  {
    ProxyCollections.beforeAdd(this, paramInt, paramObject);
    super.add(paramInt, paramObject);
  }

  public void clear()
  {
    ProxyCollections.beforeClear(this);
    super.clear();
  }

  public boolean addAll(int paramInt, Collection paramCollection)
  {
    return ProxyCollections.addAll(this, paramInt, paramCollection);
  }

  public boolean addAll(Collection paramCollection)
  {
    return ProxyCollections.addAll(this, paramCollection);
  }

  public boolean remove(Object paramObject)
  {
    ProxyCollections.beforeRemove(this, paramObject);
    boolean bool = super.remove(paramObject);
    return ProxyCollections.afterRemove(this, paramObject, bool);
  }

  public Object remove(int paramInt)
  {
    ProxyCollections.beforeRemove(this, paramInt);
    Object localObject = super.remove(paramInt);
    return ProxyCollections.afterRemove(this, paramInt, localObject);
  }

  public Object set(int paramInt, Object paramObject)
  {
    ProxyCollections.beforeSet(this, paramInt, paramObject);
    Object localObject = super.set(paramInt, paramObject);
    return ProxyCollections.afterSet(this, paramInt, paramObject, localObject);
  }

  public Iterator iterator()
  {
    Iterator localIterator = super.iterator();
    return ProxyCollections.afterIterator(this, localIterator);
  }

  public ListIterator listIterator(int paramInt)
  {
    ListIterator localListIterator = super.listIterator(paramInt);
    return ProxyCollections.afterListIterator(this, paramInt, localListIterator);
  }

  public ListIterator listIterator()
  {
    ListIterator localListIterator = super.listIterator();
    return ProxyCollections.afterListIterator(this, localListIterator);
  }

  public boolean removeAll(Collection paramCollection)
  {
    return ProxyCollections.removeAll(this, paramCollection);
  }

  public boolean retainAll(Collection paramCollection)
  {
    return ProxyCollections.retainAll(this, paramCollection);
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return Proxies.writeReplace(this, true);
  }
}

/* Location:           C:\Users\srybak\dev\java\projects\3rd_party_customized\openjpa\branches\2.1.1\openjpa\target\openjpa-2.1.1-AXWAY-1\
 * Qualified Name:     org.apache.openjpa.util.java.util.ArrayList.proxy
 * JD-Core Version:    0.6.2
 */