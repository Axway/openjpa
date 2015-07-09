package org.apache.openjpa.util;

import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;
import org.apache.openjpa.kernel.OpenJPAStateManager;

public class java$util$Vector$proxy extends Vector
  implements ProxyCollection
{
  private transient OpenJPAStateManager sm;
  private transient int field;
  private transient CollectionChangeTracker changeTracker;
  private transient Class elementType;

  public java$util$Vector$proxy(int paramInt1, int paramInt2)
  {
    super(paramInt1, paramInt2);
  }

  public java$util$Vector$proxy(int paramInt)
  {
    super(paramInt);
  }

  public java$util$Vector$proxy()
  {
  }

  public java$util$Vector$proxy(Collection paramCollection)
  {
    super(paramCollection);
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
    return new Vector((Collection)paramObject);
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

  public void add(int paramInt, Object paramObject)
  {
    ProxyCollections.beforeAdd(this, paramInt, paramObject);
    super.add(paramInt, paramObject);
  }

  public boolean add(Object paramObject)
  {
    ProxyCollections.beforeAdd(this, paramObject);
    boolean bool = super.add(paramObject);
    return ProxyCollections.afterAdd(this, paramObject, bool);
  }

  public void clear()
  {
    ProxyCollections.beforeClear(this);
    super.clear();
  }

  public boolean addAll(Collection paramCollection)
  {
    return ProxyCollections.addAll(this, paramCollection);
  }

  public boolean addAll(int paramInt, Collection paramCollection)
  {
    return ProxyCollections.addAll(this, paramInt, paramCollection);
  }

  public void addElement(Object paramObject)
  {
    ProxyCollections.beforeAddElement(this, paramObject);
    super.addElement(paramObject);
    ProxyCollections.afterAddElement(this, paramObject);
  }

  public Object remove(int paramInt)
  {
    ProxyCollections.beforeRemove(this, paramInt);
    Object localObject = super.remove(paramInt);
    return ProxyCollections.afterRemove(this, paramInt, localObject);
  }

  public boolean remove(Object paramObject)
  {
    ProxyCollections.beforeRemove(this, paramObject);
    boolean bool = super.remove(paramObject);
    return ProxyCollections.afterRemove(this, paramObject, bool);
  }

  public Object set(int paramInt, Object paramObject)
  {
    ProxyCollections.beforeSet(this, paramInt, paramObject);
    Object localObject = super.set(paramInt, paramObject);
    return ProxyCollections.afterSet(this, paramInt, paramObject, localObject);
  }

  public boolean removeAll(Collection paramCollection)
  {
    return ProxyCollections.removeAll(this, paramCollection);
  }

  public boolean retainAll(Collection paramCollection)
  {
    return ProxyCollections.retainAll(this, paramCollection);
  }

  public void setElementAt(Object paramObject, int paramInt)
  {
    ProxyCollections.beforeSetElementAt(this, paramObject, paramInt);
    super.setElementAt(paramObject, paramInt);
  }

  public void removeElementAt(int paramInt)
  {
    ProxyCollections.beforeRemoveElementAt(this, paramInt);
    super.removeElementAt(paramInt);
  }

  public void insertElementAt(Object paramObject, int paramInt)
  {
    ProxyCollections.beforeInsertElementAt(this, paramObject, paramInt);
    super.insertElementAt(paramObject, paramInt);
  }

  public boolean removeElement(Object paramObject)
  {
    ProxyCollections.beforeRemoveElement(this, paramObject);
    boolean bool = super.removeElement(paramObject);
    return ProxyCollections.afterRemoveElement(this, paramObject, bool);
  }

  public void removeAllElements()
  {
    ProxyCollections.beforeRemoveAllElements(this);
    super.removeAllElements();
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

  public void setSize(int paramInt)
  {
    Proxies.dirty(this, true);
    super.setSize(paramInt);
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return Proxies.writeReplace(this, true);
  }
}

/* Location:           C:\Users\srybak\dev\java\projects\GI\repository\5.12.0.0.x-acf\corelib\openjpa-2.1.1-CUSTOMIZED\
 * Qualified Name:     org.apache.openjpa.util.java.util.Vector.proxy
 * JD-Core Version:    0.6.2
 */