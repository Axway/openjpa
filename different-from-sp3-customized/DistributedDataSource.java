/*     */ package org.apache.openjpa.slice.jdbc;
/*     */ 
/*     */ import java.io.PrintWriter;
/*     */ import java.sql.Connection;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.SQLFeatureNotSupportedException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.logging.Logger;
/*     */ import javax.sql.DataSource;
/*     */ import javax.sql.XAConnection;
/*     */ import javax.sql.XADataSource;
/*     */ import org.apache.openjpa.lib.jdbc.DecoratingDataSource;
/*     */ 
/*     */ public class DistributedDataSource extends DecoratingDataSource
/*     */   implements Iterable<DataSource>
/*     */ {
/*  43 */   private List<DataSource> real = new ArrayList();
/*     */   private DataSource master;
/*     */ 
/*     */   public DistributedDataSource(List<DataSource> dataSources)
/*     */   {
/*  47 */     super((DataSource)dataSources.get(0));
/*  48 */     this.real = dataSources;
/*  49 */     this.master = ((DataSource)dataSources.get(0));
/*     */   }
/*     */ 
/*     */   public void addDataSource(DataSource ds) {
/*  53 */     this.real.add(ds);
/*     */   }
/*     */ 
/*     */   Connection getConnection(DataSource ds) throws SQLException {
/*  57 */     if ((ds instanceof DecoratingDataSource)) {
/*  58 */       return getConnection(((DecoratingDataSource)ds).getInnermostDelegate());
/*     */     }
/*  60 */     if ((ds instanceof XADataSource))
/*  61 */       return ((XADataSource)ds).getXAConnection().getConnection();
/*  62 */     return ds.getConnection();
/*     */   }
/*     */ 
/*     */   Connection getConnection(DataSource ds, String user, String pwd) throws SQLException
/*     */   {
/*  67 */     if ((ds instanceof DecoratingDataSource)) {
/*  68 */       return getConnection(((DecoratingDataSource)ds).getInnermostDelegate(), user, pwd);
/*     */     }
/*  70 */     if ((ds instanceof XADataSource)) {
/*  71 */       return ((XADataSource)ds).getXAConnection(user, pwd).getConnection();
/*     */     }
/*  73 */     return ds.getConnection(user, pwd);
/*     */   }
/*     */ 
/*     */   public Iterator<DataSource> iterator() {
/*  77 */     return this.real.iterator();
/*     */   }
/*     */ 
/*     */   public Connection getConnection() throws SQLException {
/*  81 */     List c = new ArrayList();
/*  82 */     for (DataSource ds : this.real)
/*  83 */       c.add(ds.getConnection());
/*  84 */     return DistributedConnection.newInstance(c);
/*     */   }
/*     */ 
/*     */   public Connection getConnection(String username, String password) throws SQLException
/*     */   {
/*  89 */     List c = new ArrayList();
/*  90 */     for (DataSource ds : this.real)
/*  91 */       c.add(ds.getConnection(username, password));
/*  92 */     return DistributedConnection.newInstance(c);
/*     */   }
/*     */ 
/*     */   public PrintWriter getLogWriter() throws SQLException {
/*  96 */     return this.master.getLogWriter();
/*     */   }
/*     */ 
/*     */   public int getLoginTimeout() throws SQLException {
/* 100 */     return this.master.getLoginTimeout();
/*     */   }
/*     */ 
/*     */   public void setLogWriter(PrintWriter out) throws SQLException {
/* 104 */     for (DataSource ds : this.real)
/* 105 */       ds.setLogWriter(out);
/*     */   }
/*     */ 
/*     */   public void setLoginTimeout(int seconds) throws SQLException {
/* 109 */     for (DataSource ds : this.real)
/* 110 */       ds.setLoginTimeout(seconds);
/*     */   }
/*     */ 
/*     */   protected void enforceAbstract()
/*     */   {
/*     */   }
/*     */ 
/*     */   public Logger getParentLogger() throws SQLFeatureNotSupportedException
/*     */   {
/* 119 */     return null;
/*     */   }
/*     */ }

/* Location:           C:\Users\srybak\dev\java\projects\3rd_party_customized\openjpa\branches\2.1.1\openjpa\target\openjpa-2.1.1-AXWAY-1\
 * Qualified Name:     org.apache.openjpa.slice.jdbc.DistributedDataSource
 * JD-Core Version:    0.6.2
 */