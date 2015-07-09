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
/*  59 */   protected CacheStatisticsSPI _stats = new CacheStatisticsImpl();
/*     */ 
/*  61 */   private static final BitSet EMPTY_BITSET = new BitSet(0);
/*     */ 
/*  63 */   private static final Localizer s_loc = Localizer.forPackage(AbstractDataCache.class);
/*     */   protected OpenJPAConfiguration conf;
/*     */   protected Log log;
/*  76 */   private String _name = null;
/*  77 */   private boolean _closed = false;
/*  78 */   private String _schedule = null;
/*  79 */   protected Set<String> _includedTypes = new HashSet();
/*  80 */   protected Set<String> _excludedTypes = new HashSet();
/*  81 */   protected boolean _evictOnBulkUpdate = true;
/*     */ 
/*     */   public String getName() {
/*  84 */     return this._name;
/*     */   }
/*     */ 
/*     */   public void setName(String name) {
/*  88 */     this._name = name;
/*     */   }
/*     */   public void setEnableStatistics(boolean enable) {
/*  91 */     if (enable == true)
/*  92 */       this._stats.enable();
/*     */   }
/*     */ 
/*     */   public void getEnableStatistics() {
/*  96 */     this._stats.isEnabled();
/*     */   }
/*     */ 
/*     */   public String getEvictionSchedule() {
/* 100 */     return this._schedule;
/*     */   }
/*     */ 
/*     */   public void setEvictionSchedule(String s) {
/* 104 */     this._schedule = s;
/*     */   }
/*     */ 
/*     */   public void initialize(DataCacheManager manager) {
/* 108 */     if ((this._schedule != null) && (!"".equals(this._schedule))) {
/* 109 */       ClearableScheduler scheduler = manager.getClearableScheduler();
/* 110 */       if (scheduler != null) {
/* 111 */         scheduler.scheduleEviction(this, this._schedule);
/*     */       }
/*     */     }
/* 114 */     if ((manager instanceof DataCacheManagerImpl)) {
/* 115 */       List invalidConfigured = new ArrayList();
/*     */ 
/* 117 */       if (this._includedTypes != null) {
/* 118 */         for (String s : this._includedTypes) {
/* 119 */           if (this._excludedTypes.contains(s)) {
/* 120 */             invalidConfigured.add(s);
/*     */           }
/*     */         }
/* 123 */         if (invalidConfigured.size() > 0) {
/* 124 */           throw new GeneralException(s_loc.get("invalid-types-excluded-types", invalidConfigured.toString()));
/*     */         }
/*     */       }
/* 127 */       ((DataCacheManagerImpl)manager).setTypes(this._includedTypes, this._excludedTypes);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void commit(Collection<DataCachePCData> additions, Collection<DataCachePCData> newUpdates, Collection<DataCachePCData> existingUpdates, Collection<Object> deletes)
/*     */   {
/* 134 */     putAllInternal(additions);
/* 135 */     putAllInternal(newUpdates);
/*     */ 
/* 139 */     if (recacheUpdates()) {
/* 140 */       putAllInternal(existingUpdates);
/*     */     }
/*     */ 
/* 145 */     removeAllInternal(deletes);
/*     */ 
/* 147 */     if (this.log.isTraceEnabled()) {
/* 148 */       Collection addIds = new ArrayList(additions.size());
/* 149 */       Collection upIds = new ArrayList(newUpdates.size());
/* 150 */       Collection exIds = new ArrayList(existingUpdates.size());
/*     */ 
/* 152 */       for (DataCachePCData addition : additions)
/* 153 */         addIds.add(addition.getId());
/* 154 */       for (DataCachePCData newUpdate : newUpdates)
/* 155 */         upIds.add(newUpdate.getId());
/* 156 */       for (DataCachePCData existingUpdate : existingUpdates) {
/* 157 */         exIds.add(existingUpdate.getId());
/*     */       }
/* 159 */       this.log.trace(s_loc.get("cache-commit", new Object[] { addIds, upIds, exIds, deletes }));
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean contains(Object key) {
/* 164 */     DataCachePCData o = getInternal(key);
/* 165 */     if ((o != null) && (o.isTimedOut())) {
/* 166 */       o = null;
/* 167 */       removeInternal(key);
/* 168 */       if (this.log.isTraceEnabled())
/* 169 */         this.log.trace(s_loc.get("cache-timeout", key));
/*     */     }
/* 171 */     return o != null;
/*     */   }
/*     */ 
/*     */   public BitSet containsAll(Collection<Object> keys) {
/* 175 */     if (keys.isEmpty()) {
/* 176 */       return EMPTY_BITSET;
/*     */     }
/* 178 */     BitSet set = new BitSet(keys.size());
/* 179 */     int i = 0;
/* 180 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 181 */       if (contains(iter.next()))
/* 182 */         set.set(i);
/* 183 */     return set;
/*     */   }
/*     */ 
/*     */   public DataCachePCData get(Object key) {
/* 187 */     DataCachePCData o = getInternal(key);
/* 188 */     if ((o != null) && (o.isTimedOut())) {
/* 189 */       o = null;
/* 190 */       removeInternal(key);
/* 191 */       if (this.log.isTraceEnabled())
/* 192 */         this.log.trace(s_loc.get("cache-timeout", key));
/*     */     }
/* 194 */     if (this.log.isTraceEnabled()) {
/* 195 */       if (o == null)
/* 196 */         this.log.trace(s_loc.get("cache-miss", key));
/*     */       else {
/* 198 */         this.log.trace(s_loc.get("cache-hit", key));
/*     */       }
/*     */     }
/* 201 */     return o;
/*     */   }
/*     */ 
/*     */   public Map<Object, DataCachePCData> getAll(List<Object> keys)
/*     */   {
/* 209 */     Map resultMap = new HashMap(keys.size());
/* 210 */     for (Iterator i$ = keys.iterator(); i$.hasNext(); ) { Object key = i$.next();
/* 211 */       resultMap.put(key, get(key)); }
/* 212 */     return resultMap;
/*     */   }
/*     */ 
/*     */   public DataCachePCData put(DataCachePCData data) {
/* 216 */     DataCachePCData o = putInternal(data.getId(), data);
/* 217 */     if (this.log.isTraceEnabled())
/* 218 */       this.log.trace(s_loc.get("cache-put", data.getId()));
/* 219 */     return (o == null) || (o.isTimedOut()) ? null : o;
/*     */   }
/*     */ 
/*     */   public void update(DataCachePCData data) {
/* 223 */     if (recacheUpdates())
/* 224 */       putInternal(data.getId(), data);
/*     */   }
/*     */ 
/*     */   public DataCachePCData remove(Object key)
/*     */   {
/* 229 */     DataCachePCData o = removeInternal(key);
/* 230 */     if ((o != null) && (o.isTimedOut()))
/* 231 */       o = null;
/* 232 */     if (this.log.isTraceEnabled()) {
/* 233 */       if (o == null)
/* 234 */         this.log.trace(s_loc.get("cache-remove-miss", key));
/*     */       else
/* 236 */         this.log.trace(s_loc.get("cache-remove-hit", key));
/*     */     }
/* 238 */     return o;
/*     */   }
/*     */ 
/*     */   public BitSet removeAll(Collection<Object> keys) {
/* 242 */     if (keys.isEmpty()) {
/* 243 */       return EMPTY_BITSET;
/*     */     }
/* 245 */     BitSet set = new BitSet(keys.size());
/* 246 */     int i = 0;
/* 247 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 248 */       if (remove(iter.next()) != null)
/* 249 */         set.set(i);
/* 250 */     return set;
/*     */   }
/*     */ 
/*     */   public void removeAll(Class<?> cls, boolean subClasses)
/*     */   {
/* 257 */     removeAllInternal(cls, subClasses);
/*     */   }
/*     */ 
/*     */   public boolean pin(Object key) {
/* 261 */     boolean bool = pinInternal(key);
/* 262 */     if (this.log.isTraceEnabled()) {
/* 263 */       if (bool)
/* 264 */         this.log.trace(s_loc.get("cache-pin-hit", key));
/*     */       else
/* 266 */         this.log.trace(s_loc.get("cache-pin-miss", key));
/*     */     }
/* 268 */     return bool;
/*     */   }
/*     */ 
/*     */   public BitSet pinAll(Collection<Object> keys) {
/* 272 */     if (keys.isEmpty()) {
/* 273 */       return EMPTY_BITSET;
/*     */     }
/* 275 */     BitSet set = new BitSet(keys.size());
/* 276 */     int i = 0;
/* 277 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 278 */       if (pin(iter.next()))
/* 279 */         set.set(i);
/* 280 */     return set;
/*     */   }
/*     */ 
/*     */   public void pinAll(Class<?> cls, boolean subs) {
/* 284 */     if (this.log.isWarnEnabled())
/* 285 */       this.log.warn(s_loc.get("cache-class-pin", getName()));
/*     */   }
/*     */ 
/*     */   public boolean unpin(Object key) {
/* 289 */     boolean bool = unpinInternal(key);
/* 290 */     if (this.log.isTraceEnabled()) {
/* 291 */       if (bool)
/* 292 */         this.log.trace(s_loc.get("cache-unpin-hit", key));
/*     */       else
/* 294 */         this.log.trace(s_loc.get("cache-unpin-miss", key));
/*     */     }
/* 296 */     return bool;
/*     */   }
/*     */ 
/*     */   public BitSet unpinAll(Collection<Object> keys) {
/* 300 */     if (keys.isEmpty()) {
/* 301 */       return EMPTY_BITSET;
/*     */     }
/* 303 */     BitSet set = new BitSet(keys.size());
/* 304 */     int i = 0;
/* 305 */     for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
/* 306 */       if (unpin(iter.next()))
/* 307 */         set.set(i);
/* 308 */     return set;
/*     */   }
/*     */ 
/*     */   public void unpinAll(Class<?> cls, boolean subs) {
/* 312 */     if (this.log.isWarnEnabled())
/* 313 */       this.log.warn(s_loc.get("cache-class-unpin", getName()));
/*     */   }
/*     */ 
/*     */   public void clear() {
/* 317 */     clearInternal();
/* 318 */     if (this.log.isTraceEnabled())
/* 319 */       this.log.trace(s_loc.get("cache-clear", getName()));
/*     */   }
/*     */ 
/*     */   public void close() {
/* 323 */     close(true);
/*     */   }
/*     */ 
/*     */   protected void close(boolean clear) {
/* 327 */     if (!this._closed) {
/* 328 */       if (clear)
/* 329 */         clearInternal();
/* 330 */       this._closed = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isClosed() {
/* 335 */     return this._closed;
/*     */   }
/*     */ 
/*     */   public void addExpirationListener(ExpirationListener listen) {
/* 339 */     addListener(listen);
/*     */   }
/*     */ 
/*     */   public boolean removeExpirationListener(ExpirationListener listen) {
/* 343 */     return removeListener(listen);
/*     */   }
/*     */ 
/*     */   public String toString() {
/* 347 */     return "[" + super.toString() + ":" + this._name + "]";
/*     */   }
/*     */ 
/*     */   public void afterCommit(RemoteCommitEvent event)
/*     */   {
/* 357 */     if (this._closed) {
/* 358 */       return;
/*     */     }
/* 360 */     if (event.getPayloadType() == 2) {
/* 361 */       removeAllTypeNamesInternal(event.getUpdatedTypeNames());
/* 362 */       removeAllTypeNamesInternal(event.getDeletedTypeNames());
/*     */     }
/*     */     else
/*     */     {
/* 367 */       removeAllInternal(event.getUpdatedObjectIds());
/* 368 */       removeAllInternal(event.getDeletedObjectIds());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void keyRemoved(Object key, boolean expired)
/*     */   {
/* 379 */     if (hasListeners()) {
/* 380 */       fireEvent(new ExpirationEvent(this, key, expired));
/*     */     }
/* 382 */     if ((expired) && (this.log.isTraceEnabled()))
/* 383 */       this.log.trace(s_loc.get("cache-expired", key));
/*     */   }
/*     */ 
/*     */   protected boolean recacheUpdates()
/*     */   {
/* 392 */     return false;
/*     */   }
/*     */ 
/*     */   protected abstract DataCachePCData getInternal(Object paramObject);
/*     */ 
/*     */   protected abstract DataCachePCData putInternal(Object paramObject, DataCachePCData paramDataCachePCData);
/*     */ 
/*     */   protected void putAllInternal(Collection<DataCachePCData> pcs)
/*     */   {
/* 411 */     for (DataCachePCData pc : pcs)
/* 412 */       putInternal(pc.getId(), pc);
/*     */   }
/*     */ 
/*     */   protected abstract DataCachePCData removeInternal(Object paramObject);
/*     */ 
/*     */   protected abstract void removeAllInternal(Class<?> paramClass, boolean paramBoolean);
/*     */ 
/*     */   protected void removeAllInternal(Collection<Object> oids)
/*     */   {
/* 430 */     for (Iterator i$ = oids.iterator(); i$.hasNext(); ) { Object oid = i$.next();
/* 431 */       removeInternal(oid);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void removeAllTypeNamesInternal(Collection<String> classNames)
/*     */   {
/* 438 */     Collection classes = Caches.addTypesByName(this.conf, classNames, null);
/* 439 */     if (classes == null) {
/* 440 */       return;
/*     */     }
/* 442 */     for (Class cls : classes) {
/* 443 */       if (this.log.isTraceEnabled())
/* 444 */         this.log.trace(s_loc.get("cache-removeclass", cls.getName()));
/* 445 */       removeAllInternal(cls, false);
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
/* 468 */     if (StringUtils.equals(this._name, name))
/* 469 */       return this;
/* 470 */     return null;
/*     */   }
/*     */ 
/*     */   public Set<String> getPartitionNames()
/*     */   {
/* 477 */     return Collections.emptySet();
/*     */   }
/*     */ 
/*     */   public boolean isPartitioned() {
/* 481 */     return false;
/*     */   }
/*     */ 
/*     */   public CacheStatistics getStatistics() {
/* 485 */     return this._stats;
/*     */   }
/*     */ 
/*     */   public void setConfiguration(Configuration conf)
/*     */   {
/* 491 */     this.conf = ((OpenJPAConfiguration)conf);
/* 492 */     this.log = conf.getLog("openjpa.DataCache");
/*     */   }
/*     */ 
/*     */   public void startConfiguration() {
/*     */   }
/*     */ 
/*     */   public void endConfiguration() {
/* 499 */     if (this._name == null)
/* 500 */       setName("default");
/*     */   }
/*     */ 
/*     */   protected void fireEvent(Object event, Object listener)
/*     */   {
/* 506 */     ExpirationListener listen = (ExpirationListener)listener;
/* 507 */     ExpirationEvent ev = (ExpirationEvent)event;
/*     */     try {
/* 509 */       listen.onExpire(ev);
/*     */     } catch (Exception e) {
/* 511 */       if (this.log.isWarnEnabled())
/* 512 */         this.log.warn(s_loc.get("exp-listener-ex"), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Set<String> getTypes() {
/* 517 */     return this._includedTypes;
/*     */   }
/*     */ 
/*     */   public Set<String> getExcludedTypes() {
/* 521 */     return this._excludedTypes;
/*     */   }
/*     */ 
/*     */   public void setTypes(Set<String> types) {
/* 525 */     this._includedTypes = types;
/*     */   }
/*     */ 
/*     */   public void setTypes(String types) {
/* 529 */     this._includedTypes = (StringUtils.isEmpty(types) ? null : new HashSet(Arrays.asList(Strings.split(types, ";", 0))));
/*     */   }
/*     */ 
/*     */   public void setExcludedTypes(Set<String> types)
/*     */   {
/* 534 */     this._excludedTypes = types;
/*     */   }
/*     */ 
/*     */   public void setExcludedTypes(String types) {
/* 538 */     this._excludedTypes = (StringUtils.isEmpty(types) ? null : new HashSet(Arrays.asList(Strings.split(types, ";", 0))));
/*     */   }
/*     */ 
/*     */   public DataCache selectCache(OpenJPAStateManager sm)
/*     */   {
/* 543 */     return this;
/*     */   }
/*     */ 
/*     */   public boolean getEvictOnBulkUpdate() {
/* 547 */     return this._evictOnBulkUpdate;
/*     */   }
/*     */ 
/*     */   public void setEvictOnBulkUpdate(boolean b) {
/* 551 */     this._evictOnBulkUpdate = b;
/*     */   }
/*     */ }

/* Location:           C:\Users\srybak\dev\java\projects\GI\repository\5.12.0.0.x-acf\corelib\openjpa-2.1.1-CUSTOMIZED\
 * Qualified Name:     org.apache.openjpa.datacache.AbstractDataCache
 * JD-Core Version:    0.6.2
 */