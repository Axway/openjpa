/*     */ package org.apache.openjpa.slice.jdbc;
/*     */ 
/*     */ import java.io.PrintWriter;
/*     */ import java.sql.Connection;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import javax.sql.DataSource;
/*     */ import javax.sql.XAConnection;
/*     */ import javax.sql.XADataSource;
/*     */ import org.apache.openjpa.lib.jdbc.DecoratingDataSource;
/*     */ 
/*     */ public class DistributedDataSource extends DecoratingDataSource
/*     */   implements Iterable<DataSource>
/*     */ {
/*  41 */   private List<DataSource> real = new ArrayList();
/*     */   private DataSource master;
/*     */ 
/*     */   public DistributedDataSource(List<DataSource> dataSources)
/*     */   {
/*  45 */     super((DataSource)dataSources.get(0));
/*  46 */     this.real = dataSources;
/*  47 */     this.master = ((DataSource)dataSources.get(0));
/*     */   }
/*     */ 
/*     */   public void addDataSource(DataSource ds) {
/*  51 */     this.real.add(ds);
/*     */   }
/*     */ 
/*     */   Connection getConnection(DataSource ds) throws SQLException {
/*  55 */     if ((ds instanceof DecoratingDataSource)) {
/*  56 */       return getConnection(((DecoratingDataSource)ds).getInnermostDelegate());
/*     */     }
/*  58 */     if ((ds instanceof XADataSource))
/*  59 */       return ((XADataSource)ds).getXAConnection().getConnection();
/*  60 */     return ds.getConnection();
/*     */   }
/*     */ 
/*     */   Connection getConnection(DataSource ds, String user, String pwd) throws SQLException
/*     */   {
/*  65 */     if ((ds instanceof DecoratingDataSource)) {
/*  66 */       return getConnection(((DecoratingDataSource)ds).getInnermostDelegate(), user, pwd);
/*     */     }
/*  68 */     if ((ds instanceof XADataSource)) {
/*  69 */       return ((XADataSource)ds).getXAConnection(user, pwd).getConnection();
/*     */     }
/*  71 */     return ds.getConnection(user, pwd);
/*     */   }
/*     */ 
/*     */   public Iterator<DataSource> iterator() {
/*  75 */     return this.real.iterator();
/*     */   }
/*     */ 
/*     */   public Connection getConnection() throws SQLException {
/*  79 */     List c = new ArrayList();
/*  80 */     for (DataSource ds : this.real)
/*  81 */       c.add(ds.getConnection());
/*  82 */     return DistributedConnection.newInstance(c);
/*     */   }
/*     */ 
/*     */   public Connection getConnection(String username, String password) throws SQLException
/*     */   {
/*  87 */     List c = new ArrayList();
/*  88 */     for (DataSource ds : this.real)
/*  89 */       c.add(ds.getConnection(username, password));
/*  90 */     return DistributedConnection.newInstance(c);
/*     */   }
/*     */ 
/*     */   public PrintWriter getLogWriter() throws SQLException {
/*  94 */     return this.master.getLogWriter();
/*     */   }
/*     */ 
/*     */   public int getLoginTimeout() throws SQLException {
/*  98 */     return this.master.getLoginTimeout();
/*     */   }
/*     */ 
/*     */   public void setLogWriter(PrintWriter out) throws SQLException {
/* 102 */     for (DataSource ds : this.real)
/* 103 */       ds.setLogWriter(out);
/*     */   }
/*     */ 
/*     */   public void setLoginTimeout(int seconds) throws SQLException {
/* 107 */     for (DataSource ds : this.real)
/* 108 */       ds.setLoginTimeout(seconds);
/*     */   }
/*     */ 
/*     */   protected void enforceAbstract()
/*     */   {
/*     */   }
/*     */ }

/* Location:           C:\Users\srybak\dev\java\projects\GI\repository\5.12.0.0.x-acf\corelib\openjpa-2.1.1-CUSTOMIZED\
 * Qualified Name:     org.apache.openjpa.slice.jdbc.DistributedDataSource
 * JD-Core Version:    0.6.2
 */