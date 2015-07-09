/*     */ package org.apache.openjpa.datacache;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.BitSet;
/*     */ import java.util.Collection;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import org.apache.openjpa.conf.OpenJPAConfiguration;
/*     */ import org.apache.openjpa.event.RemoteCommitEvent;
/*     */ import org.apache.openjpa.kernel.OpenJPAStateManager;
/*     */ import org.apache.openjpa.lib.conf.Configurable;
/*     */ import org.apache.openjpa.lib.conf.Configuration;
/*     */ import org.apache.openjpa.lib.log.Log;
/*     */ import org.apache.openjpa.lib.util.Localizer;
/*     */ import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;
/*     */ import org.apache.openjpa.util.GeneralException;
/*     */ import serp.util.Strings;
/*     */ 
/*     */ public abstract class AbstractDataCache extends AbstractConcurrentEventManager
/*     */   implements DataCache, Configurable
/*     */ {
/*  62 */   protected CacheStatisticsSPI _stats = new CacheStatisticsImpl();
/*     */ 
/*  64 */   private static final BitSet EMPTY_BITSET = new BitSet(0);
/*     */ 
/*  66 */   private static final Localizer s_loc = Localizer.forPackage(AbstractDataCache.class);
/*     */   protected OpenJPAConfiguration conf;
/*     */   protected Log log;
/*  79 */   private String _name = null;
/*  80 */   private boolean _closed = false;
/*  81 */   private String _schedule = null;
/*  82 */   protected Set<String> _includedTypes = new HashSet();
/*  83 */   protected Set<String> _excludedTypes = new HashSet();
/*  84 */   protected boolean _evictOnBulkUpdate = true;
/*     */ 
/*     */   public String getName() {
/*  87 */     return this._name;
/*     */   }
/*     */ 
/*     */   public void setName(String name) {
/*  91 */     this._name = name;
/*     */   }
/*     */   public void setEnableStatistics(boolean enable) {
/*  94 */     if (enable == true)
/*  95 */       this._stats.enable();
/*     */   }
/*     */ 
/*     */   public void getEnableStatistics() {
/*  99 */     this._stats.isEnabled();
/*     */   }
/*     */ 
/*     */   public String getEvictionSchedule() {
/* 103 */     return this._schedule;
/*     */   }
/*     */ 
/*     */   public void setEvictionSchedule(String s) {
/* 107 */     this._schedule = s;
/*     */   }
/*     */ 
/*     */   public void initialize(DataCacheManager manager) {
/* 111 */     if ((this._schedule != null) && (!"".equals(this._schedule))) {
/* 112 */       ClearableScheduler scheduler = manager.getClearableScheduler();
/* 113 */       if (scheduler != null) {
/* 114 */         scheduler.scheduleEviction(this, this._schedule);
/*     */       }
/*     */     }
/* 117 */     if ((manager instanceof DataCacheManagerImpl)) {
/* 118 */       List invalidConfigured = new ArrayList();
/*     */ 
/* 120 */       if (this._includedTypes != null) {
/* 121 */         for (String s : this._includedTypes) {
/* 122 */           if (this._excludedTypes.contains(s)) {
/* 123 */             invalidConfigured.add(s);
/*     */           }
/*     */         }
/* 126 */         if (invalidConfigured.size() > 0) {
/* 127 */           throw new GeneralException(s_loc.get("invalid-types-excluded-types", invalidConfigured.toString()));
/*     */         }
/*     */       }
/* 130 */       ((DataCacheManagerImpl)manager).setTypes(this._includedTypes, this._excludedTypes);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void commit(Collection<DataCachePCData> additions, Collection<DataCachePCData> newUpdates, Collection<DataCachePCData> existingUpdates, Collection<Object> deletes)
/*     */   {
/* 137 */     putAllInternal(additions);
/* 138 */     putAllInternal(newUpdates);
/*     */ 
/* 142 */     if (recacheUpdates()) {
/* 143 */       putAllInternal(existingUpdates);
/*     */     }
/*     */ 
/* 148 */     removeAllInternal(deletes);
/*     */ 
/* 150 */     if (this.log.isTraceEnabled()) {
/* 151 */       Collection addIds = new ArrayList(additions.size());
/* 152 */       Collection upIds = new ArrayList(newUpdates.size());
/* 153 */       Collection exIds = new ArrayList(existingUpdates.size());
/*     */ 
/* 155 */       for (DataCachePCData addition : additions)
/* 156 */         addIds.add(addition.getId());
/* 157 */       for (DataCachePCData newUpdate : newUpdates)
/* 158 */         upIds.add(newUpdate.getId());
/* 159 */       for (DataCachePCData existingUpdate : existingUpdates) {
/* 160 */         exIds.add(existingUpdate.getId());
/*     */       }
/* 162 */       this.log.trace(s_loc.get("cache-commit", new Object[] { addIds, upIds, exIds, deletes }));
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean contains(Object key) {
/* 167 */     DataCachePCData o = getInternal(key);
/* 168 */     if ((o != null) && (o.isTimedOut())) {
/* 169 */       o = null;
/* 170 */       removeInternal(key);
/* 171 */       if (this.log.isTraceEnabled())
/* 172 */         this.log.trace(s_loc.get("cache-timeout", key));
/*     */     }
/* 174 */     return o != null;
/*     */   }
/*     */ 
/*     */   public BitSet containsAll(Collection<Object> keys) {
/* 178 */     if (keys.isEmpty()) {
/* 179 */       return EMPTY_BITSET;
/*     */     }
/* 181 */     BitSet set = new BitSet(keys.size());
/* 182 */     int i = 0;
/* 183 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 184 */       if (contains(iter.next()))
/* 185 */         set.set(i);
/* 186 */     return set;
/*     */   }
/*     */ 
/*     */   public DataCachePCData get(Object key) {
/* 190 */     DataCachePCData o = getInternal(key);
/* 191 */     if ((o != null) && (o.isTimedOut())) {
/* 192 */       o = null;
/* 193 */       removeInternal(key);
/* 194 */       if (this.log.isTraceEnabled())
/* 195 */         this.log.trace(s_loc.get("cache-timeout", key));
/*     */     }
/* 197 */     if (this.log.isTraceEnabled()) {
/* 198 */       if (o == null)
/* 199 */         this.log.trace(s_loc.get("cache-miss", key));
/*     */       else {
/* 201 */         this.log.trace(s_loc.get("cache-hit", key));
/*     */       }
/*     */     }
/* 204 */     return o;
/*     */   }
/*     */ 
/*     */   public Map<Object, DataCachePCData> getAll(List<Object> keys)
/*     */   {
/* 212 */     Map resultMap = new HashMap(keys.size());
/* 213 */     for (Iterator i$ = keys.iterator(); i$.hasNext(); ) { Object key = i$.next();
/* 214 */       resultMap.put(key, get(key)); }
/* 215 */     return resultMap;
/*     */   }
/*     */ 
/*     */   public DataCachePCData put(DataCachePCData data) {
/* 219 */     DataCachePCData o = putInternal(data.getId(), data);
/* 220 */     if (this.log.isTraceEnabled())
/* 221 */       this.log.trace(s_loc.get("cache-put", data.getId()));
/* 222 */     return (o == null) || (o.isTimedOut()) ? null : o;
/*     */   }
/*     */ 
/*     */   public void update(DataCachePCData data) {
/* 226 */     if (recacheUpdates())
/* 227 */       putInternal(data.getId(), data);
/*     */   }
/*     */ 
/*     */   public DataCachePCData remove(Object key)
/*     */   {
/* 232 */     DataCachePCData o = removeInternal(key);
/* 233 */     if ((o != null) && (o.isTimedOut()))
/* 234 */       o = null;
/* 235 */     if (this.log.isTraceEnabled()) {
/* 236 */       if (o == null)
/* 237 */         this.log.trace(s_loc.get("cache-remove-miss", key));
/*     */       else
/* 239 */         this.log.trace(s_loc.get("cache-remove-hit", key));
/*     */     }
/* 241 */     return o;
/*     */   }
/*     */ 
/*     */   public BitSet removeAll(Collection<Object> keys) {
/* 245 */     if (keys.isEmpty()) {
/* 246 */       return EMPTY_BITSET;
/*     */     }
/* 248 */     BitSet set = new BitSet(keys.size());
/* 249 */     int i = 0;
/* 250 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 251 */       if (remove(iter.next()) != null)
/* 252 */         set.set(i);
/* 253 */     return set;
/*     */   }
/*     */ 
/*     */   public void removeAll(Class<?> cls, boolean subClasses)
/*     */   {
/* 260 */     removeAllInternal(cls, subClasses);
/*     */   }
/*     */ 
/*     */   public boolean pin(Object key) {
/* 264 */     boolean bool = pinInternal(key);
/* 265 */     if (this.log.isTraceEnabled()) {
/* 266 */       if (bool)
/* 267 */         this.log.trace(s_loc.get("cache-pin-hit", key));
/*     */       else
/* 269 */         this.log.trace(s_loc.get("cache-pin-miss", key));
/*     */     }
/* 271 */     return bool;
/*     */   }
/*     */ 
/*     */   public BitSet pinAll(Collection<Object> keys) {
/* 275 */     if (keys.isEmpty()) {
/* 276 */       return EMPTY_BITSET;
/*     */     }
/* 278 */     BitSet set = new BitSet(keys.size());
/* 279 */     int i = 0;
/* 280 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 281 */       if (pin(iter.next()))
/* 282 */         set.set(i);
/* 283 */     return set;
/*     */   }
/*     */ 
/*     */   public void pinAll(Class<?> cls, boolean subs) {
/* 287 */     if (this.log.isWarnEnabled())
/* 288 */       this.log.warn(s_loc.get("cache-class-pin", getName()));
/*     */   }
/*     */ 
/*     */   public boolean unpin(Object key) {
/* 292 */     boolean bool = unpinInternal(key);
/* 293 */     if (this.log.isTraceEnabled()) {
/* 294 */       if (bool)
/* 295 */         this.log.trace(s_loc.get("cache-unpin-hit", key));
/*     */       else
/* 297 */         this.log.trace(s_loc.get("cache-unpin-miss", key));
/*     */     }
/* 299 */     return bool;
/*     */   }
/*     */ 
/*     */   public BitSet unpinAll(Collection<Object> keys) {
/* 303 */     if (keys.isEmpty()) {
/* 304 */       return EMPTY_BITSET;
/*     */     }
/* 306 */     BitSet set = new BitSet(keys.size());
/* 307 */     int i = 0;
/* 308 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 309 */       if (unpin(iter.next()))
/* 310 */         set.set(i);
/* 311 */     return set;
/*     */   }
/*     */ 
/*     */   public void unpinAll(Class<?> cls, boolean subs) {
/* 315 */     if (this.log.isWarnEnabled())
/* 316 */       this.log.warn(s_loc.get("cache-class-unpin", getName()));
/*     */   }
/*     */ 
/*     */   public void clear() {
/* 320 */     clearInternal();
/* 321 */     if (this.log.isTraceEnabled())
/* 322 */       this.log.trace(s_loc.get("cache-clear", getName()));
/*     */   }
/*     */ 
/*     */   public void close() {
/* 326 */     close(true);
/*     */   }
/*     */ 
/*     */   protected void close(boolean clear) {
/* 330 */     if (!this._closed) {
/* 331 */       if (clear)
/* 332 */         clearInternal();
/* 333 */       this._closed = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isClosed() {
/* 338 */     return this._closed;
/*     */   }
/*     */ 
/*     */   public void addExpirationListener(ExpirationListener listen) {
/* 342 */     addListener(listen);
/*     */   }
/*     */ 
/*     */   public boolean removeExpirationListener(ExpirationListener listen) {
/* 346 */     return removeListener(listen);
/*     */   }
/*     */ 
/*     */   public String toString() {
/* 350 */     return "[" + super.toString() + ":" + this._name + "]";
/*     */   }
/*     */ 
/*     */   public void afterCommit(RemoteCommitEvent event)
/*     */   {
/* 360 */     if (this._closed) {
/* 361 */       return;
/*     */     }
/* 363 */     if (event.getPayloadType() == 2) {
/* 364 */       removeAllTypeNamesInternal(event.getUpdatedTypeNames());
/* 365 */       removeAllTypeNamesInternal(event.getDeletedTypeNames());
/*     */     }
/*     */     else
/*     */     {
/* 370 */       removeAllInternal(event.getUpdatedObjectIds());
/* 371 */       removeAllInternal(event.getDeletedObjectIds());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void keyRemoved(Object key, boolean expired)
/*     */   {
/* 382 */     if (hasListeners()) {
/* 383 */       fireEvent(new ExpirationEvent(this, key, expired));
/*     */     }
/* 385 */     if ((expired) && (this.log.isTraceEnabled()))
/* 386 */       this.log.trace(s_loc.get("cache-expired", key));
/*     */   }
/*     */ 
/*     */   protected boolean recacheUpdates()
/*     */   {
/* 395 */     return false;
/*     */   }
/*     */ 
/*     */   protected abstract DataCachePCData getInternal(Object paramObject);
/*     */ 
/*     */   protected abstract DataCachePCData putInternal(Object paramObject, DataCachePCData paramDataCachePCData);
/*     */ 
/*     */   protected void putAllInternal(Collection<DataCachePCData> pcs)
/*     */   {
/* 414 */     for (DataCachePCData pc : pcs)
/* 415 */       putInternal(pc.getId(), pc);
/*     */   }
/*     */ 
/*     */   protected abstract DataCachePCData removeInternal(Object paramObject);
/*     */ 
/*     */   protected abstract void removeAllInternal(Class<?> paramClass, boolean paramBoolean);
/*     */ 
/*     */   protected void removeAllInternal(Collection<Object> oids)
/*     */   {
/* 433 */     for (Iterator i$ = oids.iterator(); i$.hasNext(); ) { Object oid = i$.next();
/* 434 */       removeInternal(oid);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void removeAllTypeNamesInternal(Collection<String> classNames)
/*     */   {
/* 441 */     Collection classes = Caches.addTypesByName(this.conf, classNames, null);
/* 442 */     if (classes == null) {
/* 443 */       return;
/*     */     }
/* 445 */     for (Class cls : classes) {
/* 446 */       if (this.log.isTraceEnabled())
/* 447 */         this.log.trace(s_loc.get("cache-removeclass", cls.getName()));
/* 448 */       removeAllInternal(cls, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected abstract void clearInternal();
/*     */ 
/*     */   protected abstract boolean pinInternal(Object paramObject);
/*     */ 
/*     */   protected abstract boolean unpinInternal(Object paramObject);
/*     */ 
/*     */   public DataCache getPartition(String name, boolean create)
/*     */   {
/* 471 */     if (StringUtils.equals(this._name, name))
/* 472 */       return this;
/* 473 */     return null;
/*     */   }
/*     */ 
/*     */   public Set<String> getPartitionNames()
/*     */   {
/* 480 */     return Collections.emptySet();
/*     */   }
/*     */ 
/*     */   public boolean isPartitioned() {
/* 484 */     return false;
/*     */   }
/*     */ 
/*     */   public CacheStatistics getStatistics() {
/* 488 */     return this._stats;
/*     */   }
/*     */ 
/*     */   public void setConfiguration(Configuration conf)
/*     */   {
/* 494 */     this.conf = ((OpenJPAConfiguration)conf);
/* 495 */     this.log = conf.getLog("openjpa.DataCache");
/*     */   }
/*     */ 
/*     */   public void startConfiguration() {
/*     */   }
/*     */ 
/*     */   public void endConfiguration() {
/* 502 */     if (this._name == null)
/* 503 */       setName("default");
/*     */   }
/*     */ 
/*     */   protected void fireEvent(Object event, Object listener)
/*     */   {
/* 509 */     ExpirationListener listen = (ExpirationListener)listener;
/* 510 */     ExpirationEvent ev = (ExpirationEvent)event;
/*     */     try {
/* 512 */       listen.onExpire(ev);
/*     */     } catch (Exception e) {
/* 514 */       if (this.log.isWarnEnabled())
/* 515 */         this.log.warn(s_loc.get("exp-listener-ex"), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Set<String> getTypes() {
/* 520 */     return this._includedTypes;
/*     */   }
/*     */ 
/*     */   public Set<String> getExcludedTypes() {
/* 524 */     return this._excludedTypes;
/*     */   }
/*     */ 
/*     */   public void setTypes(Set<String> types) {
/* 528 */     this._includedTypes = types;
/*     */   }
/*     */ 
/*     */   public void setTypes(String types) {
/* 532 */     this._includedTypes = (StringUtils.isEmpty(types) ? null : new HashSet(Arrays.asList(Strings.split(types, ";", 0))));
/*     */   }
/*     */ 
/*     */   public void setExcludedTypes(Set<String> types)
/*     */   {
/* 537 */     this._excludedTypes = types;
/*     */   }
/*     */ 
/*     */   public void setExcludedTypes(String types) {
/* 541 */     this._excludedTypes = (StringUtils.isEmpty(types) ? null : new HashSet(Arrays.asList(Strings.split(types, ";", 0))));
/*     */   }
/*     */ 
/*     */   public DataCache selectCache(OpenJPAStateManager sm)
/*     */   {
/* 546 */     return this;
/*     */   }
/*     */ 
/*     */   public boolean getEvictOnBulkUpdate() {
/* 550 */     return this._evictOnBulkUpdate;
/*     */   }
/*     */ 
/*     */   public void setEvictOnBulkUpdate(boolean b) {
/* 554 */     this._evictOnBulkUpdate = b;
/*     */   }
/*     */ }

/* Location:           C:\Users\srybak\dev\java\projects\3rd_party_customized\openjpa\branches\2.1.1\openjpa\target\openjpa-2.1.1-AXWAY-1\
 * Qualified Name:     org.apache.openjpa.datacache.AbstractDataCache
 * JD-Core Version:    0.6.2
 */