/*      */ package org.apache.openjpa.enhance;
/*      */ 
/*      */ import java.io.Externalizable;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.ObjectInput;
/*      */ import java.io.ObjectInputStream;
/*      */ import java.io.ObjectOutput;
/*      */ import java.io.ObjectOutputStream;
/*      */ import java.io.ObjectStreamClass;
/*      */ import java.io.ObjectStreamException;
/*      */ import java.io.PrintStream;
/*      */ import java.io.Serializable;
/*      */ import java.lang.reflect.Field;
/*      */ import java.lang.reflect.Method;
/*      */ import java.lang.reflect.Modifier;
/*      */ import java.math.BigDecimal;
/*      */ import java.math.BigInteger;
/*      */ import java.security.AccessController;
/*      */ import java.security.PrivilegedActionException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Collection;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import org.apache.commons.lang.StringUtils;
/*      */ import org.apache.openjpa.conf.DetachOptions;
/*      */ import org.apache.openjpa.conf.OpenJPAConfiguration;
/*      */ import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
/*      */ import org.apache.openjpa.lib.conf.Configurations;
/*      */ import org.apache.openjpa.lib.conf.Configurations.Runnable;
/*      */ import org.apache.openjpa.lib.log.Log;
/*      */ import org.apache.openjpa.lib.meta.ClassArgParser;
/*      */ import org.apache.openjpa.lib.util.BytecodeWriter;
/*      */ import org.apache.openjpa.lib.util.Files;
/*      */ import org.apache.openjpa.lib.util.J2DoPrivHelper;
/*      */ import org.apache.openjpa.lib.util.Localizer;
/*      */ import org.apache.openjpa.lib.util.Localizer.Message;
/*      */ import org.apache.openjpa.lib.util.Options;
/*      */ import org.apache.openjpa.lib.util.Services;
/*      */ import org.apache.openjpa.lib.util.svn.SVNUtils;
/*      */ import org.apache.openjpa.meta.AccessCode;
/*      */ import org.apache.openjpa.meta.ClassMetaData;
/*      */ import org.apache.openjpa.meta.FieldMetaData;
/*      */ import org.apache.openjpa.meta.MetaDataFactory;
/*      */ import org.apache.openjpa.meta.MetaDataRepository;
/*      */ import org.apache.openjpa.util.BigDecimalId;
/*      */ import org.apache.openjpa.util.BigIntegerId;
/*      */ import org.apache.openjpa.util.ByteId;
/*      */ import org.apache.openjpa.util.CharId;
/*      */ import org.apache.openjpa.util.ClassResolver;
/*      */ import org.apache.openjpa.util.DateId;
/*      */ import org.apache.openjpa.util.DoubleId;
/*      */ import org.apache.openjpa.util.FloatId;
/*      */ import org.apache.openjpa.util.GeneralException;
/*      */ import org.apache.openjpa.util.Id;
/*      */ import org.apache.openjpa.util.ImplHelper;
/*      */ import org.apache.openjpa.util.IntId;
/*      */ import org.apache.openjpa.util.InternalException;
/*      */ import org.apache.openjpa.util.LongId;
/*      */ import org.apache.openjpa.util.ObjectId;
/*      */ import org.apache.openjpa.util.OpenJPAException;
/*      */ import org.apache.openjpa.util.ShortId;
/*      */ import org.apache.openjpa.util.StringId;
/*      */ import org.apache.openjpa.util.UserException;
/*      */ import serp.bytecode.BCClass;
/*      */ import serp.bytecode.BCField;
/*      */ import serp.bytecode.BCMethod;
/*      */ import serp.bytecode.ClassConstantInstruction;
/*      */ import serp.bytecode.ClassInstruction;
/*      */ import serp.bytecode.Code;
/*      */ import serp.bytecode.ConstantInstruction;
/*      */ import serp.bytecode.Exceptions;
/*      */ import serp.bytecode.FieldInstruction;
/*      */ import serp.bytecode.GetFieldInstruction;
/*      */ import serp.bytecode.IIncInstruction;
/*      */ import serp.bytecode.IfInstruction;
/*      */ import serp.bytecode.Instruction;
/*      */ import serp.bytecode.JumpInstruction;
/*      */ import serp.bytecode.LoadInstruction;
/*      */ import serp.bytecode.LocalVariableInstruction;
/*      */ import serp.bytecode.LookupSwitchInstruction;
/*      */ import serp.bytecode.MethodInstruction;
/*      */ import serp.bytecode.NewArrayInstruction;
/*      */ import serp.bytecode.Project;
/*      */ import serp.bytecode.PutFieldInstruction;
/*      */ import serp.bytecode.ReturnInstruction;
/*      */ import serp.bytecode.StoreInstruction;
/*      */ import serp.bytecode.TableSwitchInstruction;
/*      */ import serp.util.Strings;
/*      */ 
/*      */ public class PCEnhancer
/*      */ {
/*      */   public static final int ENHANCER_VERSION;
/*  123 */   boolean _addVersionInitFlag = true;
/*      */   public static final int ENHANCE_NONE = 0;
/*      */   public static final int ENHANCE_AWARE = 2;
/*      */   public static final int ENHANCE_INTERFACE = 4;
/*      */   public static final int ENHANCE_PC = 8;
/*      */   public static final String PRE = "pc";
/*      */   public static final String ISDETACHEDSTATEDEFINITIVE = "pcisDetachedStateDefinitive";
/*  134 */   private static final Class PCTYPE = PersistenceCapable.class;
/*      */   private static final String SM = "pcStateManager";
/*  136 */   private static final Class SMTYPE = StateManager.class;
/*      */   private static final String INHERIT = "pcInheritedFieldCount";
/*      */   private static final String CONTEXTNAME = "GenericContext";
/*  139 */   private static final Class USEREXCEP = UserException.class;
/*  140 */   private static final Class INTERNEXCEP = InternalException.class;
/*  141 */   private static final Class HELPERTYPE = PCRegistry.class;
/*      */   private static final String SUPER = "pcPCSuperclass";
/*  143 */   private static final Class OIDFSTYPE = FieldSupplier.class;
/*  144 */   private static final Class OIDFCTYPE = FieldConsumer.class;
/*      */   private static final String VERSION_INIT_STR = "pcVersionInit";
/*  148 */   private static final Localizer _loc = Localizer.forPackage(PCEnhancer.class);
/*  149 */   private static final String REDEFINED_ATTRIBUTE = new StringBuilder().append(PCEnhancer.class.getName()).append("#redefined-type").toString();
/*      */   private static final AuxiliaryEnhancer[] _auxEnhancers;
/*      */   private BCClass _pc;
/*      */   private final BCClass _managedType;
/*      */   private final MetaDataRepository _repos;
/*      */   private final ClassMetaData _meta;
/*      */   private final Log _log;
/*  196 */   private Collection _oids = null;
/*  197 */   private boolean _defCons = true;
/*  198 */   private boolean _redefine = false;
/*  199 */   private boolean _subclass = false;
/*  200 */   private boolean _fail = false;
/*  201 */   private Set _violations = null;
/*  202 */   private File _dir = null;
/*  203 */   private BytecodeWriter _writer = null;
/*  204 */   private Map _backingFields = null;
/*  205 */   private Map _attrsToFields = null;
/*  206 */   private Map _fieldsToAttrs = null;
/*  207 */   private boolean _isAlreadyRedefined = false;
/*  208 */   private boolean _isAlreadySubclassed = false;
/*  209 */   private boolean _bcsConfigured = false;
/*      */ 
/*      */   public PCEnhancer(OpenJPAConfiguration conf, Class type)
/*      */   {
/*  217 */     this(conf, (BCClass)AccessController.doPrivileged(J2DoPrivHelper.loadProjectClassAction(new Project(), type)), (MetaDataRepository)null);
/*      */   }
/*      */ 
/*      */   public PCEnhancer(OpenJPAConfiguration conf, ClassMetaData meta)
/*      */   {
/*  227 */     this(conf, (BCClass)AccessController.doPrivileged(J2DoPrivHelper.loadProjectClassAction(new Project(), meta.getDescribedType())), meta.getRepository());
/*      */   }
/*      */ 
/*      */   /** @deprecated */
/*      */   public PCEnhancer(OpenJPAConfiguration conf, BCClass type, MetaDataRepository repos)
/*      */   {
/*  248 */     this(conf, type, repos, null);
/*      */   }
/*      */ 
/*      */   public PCEnhancer(OpenJPAConfiguration conf, BCClass type, MetaDataRepository repos, ClassLoader loader)
/*      */   {
/*  267 */     this._managedType = type;
/*  268 */     this._pc = type;
/*      */ 
/*  270 */     this._log = conf.getLog("openjpa.Enhance");
/*      */ 
/*  272 */     if (repos == null) {
/*  273 */       this._repos = conf.newMetaDataRepositoryInstance();
/*  274 */       this._repos.setSourceMode(1);
/*      */     } else {
/*  276 */       this._repos = repos;
/*  277 */     }this._meta = this._repos.getMetaData(type.getType(), loader, false);
/*      */   }
/*      */ 
/*      */   public PCEnhancer(MetaDataRepository repos, BCClass type, ClassMetaData meta)
/*      */   {
/*  300 */     this._managedType = type;
/*  301 */     this._pc = type;
/*      */ 
/*  303 */     this._log = repos.getConfiguration().getLog("openjpa.Enhance");
/*      */ 
/*  306 */     this._repos = repos;
/*  307 */     this._meta = meta;
/*      */   }
/*      */ 
/*      */   static String toPCSubclassName(Class cls) {
/*  311 */     return new StringBuilder().append(Strings.getPackageName(PCEnhancer.class)).append(".").append(cls.getName().replace('.', '$')).append("$pcsubclass").toString();
/*      */   }
/*      */ 
/*      */   public static boolean isPCSubclassName(String className)
/*      */   {
/*  322 */     return (className.startsWith(Strings.getPackageName(PCEnhancer.class))) && (className.endsWith("$pcsubclass"));
/*      */   }
/*      */ 
/*      */   public static String toManagedTypeName(String className)
/*      */   {
/*  334 */     if (isPCSubclassName(className)) {
/*  335 */       className = className.substring(Strings.getPackageName(PCEnhancer.class).length() + 1);
/*      */ 
/*  337 */       className = className.substring(0, className.lastIndexOf("$"));
/*      */ 
/*  339 */       className = className.replace('$', '.');
/*      */     }
/*      */ 
/*  342 */     return className;
/*      */   }
/*      */ 
/*      */   public PCEnhancer(OpenJPAConfiguration conf, BCClass type, ClassMetaData meta)
/*      */   {
/*  350 */     this(conf, type, meta.getRepository());
/*      */   }
/*      */ 
/*      */   public BCClass getPCBytecode()
/*      */   {
/*  358 */     return this._pc;
/*      */   }
/*      */ 
/*      */   public BCClass getManagedTypeBytecode()
/*      */   {
/*  368 */     return this._managedType;
/*      */   }
/*      */ 
/*      */   public ClassMetaData getMetaData()
/*      */   {
/*  376 */     return this._meta;
/*      */   }
/*      */ 
/*      */   public boolean getAddDefaultConstructor()
/*      */   {
/*  386 */     return this._defCons;
/*      */   }
/*      */ 
/*      */   public void setAddDefaultConstructor(boolean addDefaultConstructor)
/*      */   {
/*  396 */     this._defCons = addDefaultConstructor;
/*      */   }
/*      */ 
/*      */   public boolean getRedefine()
/*      */   {
/*  407 */     return this._redefine;
/*      */   }
/*      */ 
/*      */   public void setRedefine(boolean redefine)
/*      */   {
/*  418 */     this._redefine = redefine;
/*      */   }
/*      */ 
/*      */   public boolean isAlreadyRedefined()
/*      */   {
/*  428 */     return this._isAlreadyRedefined;
/*      */   }
/*      */ 
/*      */   public boolean isAlreadySubclassed()
/*      */   {
/*  438 */     return this._isAlreadySubclassed;
/*      */   }
/*      */ 
/*      */   public boolean getCreateSubclass()
/*      */   {
/*  448 */     return this._subclass;
/*      */   }
/*      */ 
/*      */   public void setCreateSubclass(boolean subclass)
/*      */   {
/*  458 */     this._subclass = subclass;
/*  459 */     this._addVersionInitFlag = false;
/*      */   }
/*      */ 
/*      */   public boolean getEnforcePropertyRestrictions()
/*      */   {
/*  468 */     return this._fail;
/*      */   }
/*      */ 
/*      */   public void setEnforcePropertyRestrictions(boolean fail)
/*      */   {
/*  477 */     this._fail = fail;
/*      */   }
/*      */ 
/*      */   public File getDirectory()
/*      */   {
/*  486 */     return this._dir;
/*      */   }
/*      */ 
/*      */   public void setDirectory(File dir)
/*      */   {
/*  495 */     this._dir = dir;
/*      */   }
/*      */ 
/*      */   public BytecodeWriter getBytecodeWriter()
/*      */   {
/*  502 */     return this._writer;
/*      */   }
/*      */ 
/*      */   public void setBytecodeWriter(BytecodeWriter writer)
/*      */   {
/*  509 */     this._writer = writer;
/*      */   }
/*      */ 
/*      */   public int run()
/*      */   {
/*  518 */     Class type = this._managedType.getType();
/*      */     try
/*      */     {
/*  521 */       if (this._pc.isInterface()) {
/*  522 */         return 4;
/*      */       }
/*      */ 
/*  525 */       ClassLoader loader = (ClassLoader)AccessController.doPrivileged(J2DoPrivHelper.getClassLoaderAction(type));
/*  526 */       for (Class iface : this._managedType.getDeclaredInterfaceTypes()) {
/*  527 */         if (iface.getName().equals(PCTYPE.getName())) {
/*  528 */           if (this._log.isTraceEnabled()) {
/*  529 */             this._log.trace(_loc.get("pc-type", type, loader));
/*      */           }
/*  531 */           return 0;
/*      */         }
/*      */       }
/*  534 */       if (this._log.isTraceEnabled()) {
/*  535 */         this._log.trace(_loc.get("enhance-start", type, loader));
/*      */       }
/*      */ 
/*  539 */       configureBCs();
/*      */ 
/*  543 */       if (isPropertyAccess(this._meta)) {
/*  544 */         validateProperties();
/*  545 */         if (getCreateSubclass())
/*  546 */           addAttributeTranslation();
/*      */       }
/*  548 */       replaceAndValidateFieldAccess();
/*  549 */       processViolations();
/*      */ 
/*  551 */       if (this._meta != null) {
/*  552 */         enhanceClass();
/*  553 */         addFields();
/*  554 */         addStaticInitializer();
/*  555 */         addPCMethods();
/*  556 */         addAccessors();
/*  557 */         addAttachDetachCode();
/*  558 */         addSerializationCode();
/*  559 */         addCloningCode();
/*  560 */         runAuxiliaryEnhancers();
/*  561 */         return 8;
/*      */       }
/*      */ 
/*  564 */       if (this._log.isWarnEnabled())
/*  565 */         this._log.warn(_loc.get("pers-aware", type, loader));
/*  566 */       return 2;
/*      */     } catch (OpenJPAException ke) {
/*  568 */       throw ke;
/*      */     } catch (Exception e) {
/*  570 */       throw new GeneralException(_loc.get("enhance-error", type.getName(), e.getMessage()), e);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void configureBCs()
/*      */   {
/*  576 */     if (!this._bcsConfigured) {
/*  577 */       if (getRedefine()) {
/*  578 */         if (this._managedType.getAttribute(REDEFINED_ATTRIBUTE) == null)
/*  579 */           this._managedType.addAttribute(REDEFINED_ATTRIBUTE);
/*      */         else {
/*  581 */           this._isAlreadyRedefined = true;
/*      */         }
/*      */       }
/*  584 */       if (getCreateSubclass()) {
/*  585 */         PCSubclassValidator val = new PCSubclassValidator(this._meta, this._managedType, this._log, this._fail);
/*      */ 
/*  587 */         val.assertCanSubclass();
/*      */ 
/*  589 */         this._pc = this._managedType.getProject().loadClass(toPCSubclassName(this._managedType.getType()));
/*      */ 
/*  591 */         if (this._pc.getSuperclassBC() != this._managedType) {
/*  592 */           this._pc.setSuperclass(this._managedType);
/*  593 */           this._pc.setAbstract(this._managedType.isAbstract());
/*  594 */           this._pc.declareInterface(DynamicPersistenceCapable.class);
/*      */         } else {
/*  596 */           this._isAlreadySubclassed = true;
/*      */         }
/*      */       }
/*      */ 
/*  600 */       this._bcsConfigured = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void record()
/*      */     throws IOException
/*      */   {
/*  610 */     if ((this._managedType != this._pc) && (getRedefine()))
/*  611 */       record(this._managedType);
/*  612 */     record(this._pc);
/*      */     Iterator itr;
/*  613 */     if (this._oids != null)
/*  614 */       for (itr = this._oids.iterator(); itr.hasNext(); )
/*  615 */         record((BCClass)itr.next());
/*      */   }
/*      */ 
/*      */   private void record(BCClass bc)
/*      */     throws IOException
/*      */   {
/*  624 */     if (this._writer != null) {
/*  625 */       this._writer.write(bc);
/*  626 */     } else if (this._dir == null) {
/*  627 */       AsmAdaptor.write(bc);
/*      */     } else {
/*  629 */       File dir = Files.getPackageFile(this._dir, bc.getPackageName(), true);
/*  630 */       AsmAdaptor.write(bc, new File(dir, new StringBuilder().append(bc.getClassName()).append(".class").toString()));
/*      */     }
/*      */   }
/*      */ 
/*      */   private void validateProperties()
/*      */   {
/*      */     FieldMetaData[] fmds;
/*      */     FieldMetaData[] fmds;
/*  641 */     if (getCreateSubclass())
/*  642 */       fmds = this._meta.getFields();
/*      */     else {
/*  644 */       fmds = this._meta.getDeclaredFields();
/*      */     }
/*      */ 
/*  647 */     BCField assigned = null;
/*  648 */     for (int i = 0; i < fmds.length; i++)
/*      */     {
/*  650 */       if (!(fmds[i].getBackingMember() instanceof Method))
/*      */       {
/*  654 */         if (!this._meta.isMixedAccess()) {
/*  655 */           addViolation("property-bad-member", new Object[] { fmds[i], fmds[i].getBackingMember() }, true);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  662 */         Method meth = (Method)fmds[i].getBackingMember();
/*      */ 
/*  664 */         BCClass declaringType = this._managedType.getProject().loadClass(fmds[i].getDeclaringType());
/*      */ 
/*  666 */         BCMethod getter = declaringType.getDeclaredMethod(meth.getName(), meth.getParameterTypes());
/*      */ 
/*  668 */         if (getter == null) {
/*  669 */           addViolation("property-no-getter", new Object[] { fmds[i] }, true);
/*      */         }
/*      */         else
/*      */         {
/*  673 */           BCField returned = getReturnedField(getter);
/*  674 */           if (returned != null) {
/*  675 */             registerBackingFieldInfo(fmds[i], getter, returned);
/*      */           }
/*  677 */           BCMethod setter = declaringType.getDeclaredMethod(getSetterName(fmds[i]), new Class[] { fmds[i].getDeclaredType() });
/*      */ 
/*  679 */           if (setter == null) {
/*  680 */             if (returned == null) {
/*  681 */               addViolation("property-no-setter", new Object[] { fmds[i] }, true);
/*      */ 
/*  683 */               continue;
/*  684 */             }if (!getRedefine())
/*      */             {
/*  686 */               setter = this._managedType.declareMethod(getSetterName(fmds[i]), Void.TYPE, new Class[] { fmds[i].getDeclaredType() });
/*      */ 
/*  688 */               setter.makePrivate();
/*  689 */               Code code = setter.getCode(true);
/*  690 */               code.aload().setThis();
/*  691 */               code.xload().setParam(0);
/*  692 */               code.putfield().setField(returned);
/*  693 */               code.vreturn();
/*  694 */               code.calculateMaxStack();
/*  695 */               code.calculateMaxLocals();
/*      */             }
/*      */           }
/*      */ 
/*  699 */           if (setter != null) {
/*  700 */             assigned = getAssignedField(setter);
/*      */           }
/*  702 */           if (assigned != null) {
/*  703 */             if (setter != null) {
/*  704 */               registerBackingFieldInfo(fmds[i], setter, assigned);
/*      */             }
/*  706 */             if (assigned != returned)
/*  707 */               addViolation("property-setter-getter-mismatch", new Object[] { fmds[i], assigned.getName(), returned == null ? null : returned.getName() }, false);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void registerBackingFieldInfo(FieldMetaData fmd, BCMethod method, BCField field)
/*      */   {
/*  716 */     if (this._backingFields == null)
/*  717 */       this._backingFields = new HashMap();
/*  718 */     this._backingFields.put(method.getName(), field.getName());
/*      */ 
/*  720 */     if (this._attrsToFields == null)
/*  721 */       this._attrsToFields = new HashMap();
/*  722 */     this._attrsToFields.put(fmd.getName(), field.getName());
/*      */ 
/*  724 */     if (this._fieldsToAttrs == null)
/*  725 */       this._fieldsToAttrs = new HashMap();
/*  726 */     this._fieldsToAttrs.put(field.getName(), fmd.getName());
/*      */   }
/*      */ 
/*      */   private void addAttributeTranslation()
/*      */   {
/*  732 */     ArrayList propFmds = new ArrayList();
/*  733 */     FieldMetaData[] fmds = this._meta.getFields();
/*      */ 
/*  735 */     if (this._meta.isMixedAccess())
/*      */     {
/*  738 */       propFmds = new ArrayList();
/*      */ 
/*  742 */       for (int i = 0; i < fmds.length; i++) {
/*  743 */         if (isPropertyAccess(fmds[i])) {
/*  744 */           propFmds.add(Integer.valueOf(i));
/*      */         }
/*      */       }
/*      */ 
/*  748 */       if (propFmds.size() == 0) {
/*  749 */         return;
/*      */       }
/*      */     }
/*  752 */     this._pc.declareInterface(AttributeTranslator.class);
/*  753 */     BCMethod method = this._pc.declareMethod("pcAttributeIndexToFieldName", String.class, new Class[] { Integer.TYPE });
/*      */ 
/*  755 */     method.makePublic();
/*  756 */     Code code = method.getCode(true);
/*      */ 
/*  759 */     code.iload().setParam(0);
/*  760 */     if (!this._meta.isMixedAccess())
/*      */     {
/*  763 */       TableSwitchInstruction tabins = code.tableswitch();
/*      */ 
/*  765 */       tabins.setLow(0);
/*  766 */       tabins.setHigh(fmds.length - 1);
/*      */ 
/*  770 */       for (int i = 0; i < fmds.length; i++) {
/*  771 */         tabins.addTarget(code.constant().setValue(this._attrsToFields.get(fmds[i].getName())));
/*      */ 
/*  773 */         code.areturn();
/*      */       }
/*      */ 
/*  776 */       tabins.setDefaultTarget(throwException(code, IllegalArgumentException.class));
/*      */     }
/*      */     else
/*      */     {
/*  782 */       LookupSwitchInstruction lookupins = code.lookupswitch();
/*      */ 
/*  784 */       for (Integer i : propFmds) {
/*  785 */         lookupins.addCase(i.intValue(), code.constant().setValue(this._attrsToFields.get(fmds[i.intValue()].getName())));
/*      */ 
/*  788 */         code.areturn();
/*      */       }
/*      */ 
/*  791 */       lookupins.setDefaultTarget(throwException(code, IllegalArgumentException.class));
/*      */     }
/*      */ 
/*  795 */     code.calculateMaxLocals();
/*  796 */     code.calculateMaxStack();
/*      */   }
/*      */ 
/*      */   private static String getSetterName(FieldMetaData fmd)
/*      */   {
/*  803 */     return new StringBuilder().append("set").append(StringUtils.capitalize(fmd.getName())).toString();
/*      */   }
/*      */ 
/*      */   static BCField getReturnedField(BCMethod meth)
/*      */   {
/*  811 */     return findField(meth, ((Code)AccessController.doPrivileged(J2DoPrivHelper.newCodeAction())).xreturn().setType(meth.getReturnType()), false);
/*      */   }
/*      */ 
/*      */   static BCField getAssignedField(BCMethod meth)
/*      */   {
/*  820 */     return findField(meth, ((Code)AccessController.doPrivileged(J2DoPrivHelper.newCodeAction())).putfield(), true);
/*      */   }
/*      */ 
/*      */   private static BCField findField(BCMethod meth, Instruction template, boolean findAccessed)
/*      */   {
/*  832 */     if (meth.isStatic()) {
/*  833 */       return null;
/*      */     }
/*  835 */     Code code = meth.getCode(false);
/*  836 */     if (code == null)
/*  837 */       return null;
/*  838 */     code.beforeFirst();
/*      */ 
/*  840 */     BCField field = null;
/*      */ 
/*  842 */     while (code.searchForward(template)) {
/*  843 */       int backupCount = 3;
/*  844 */       Instruction templateIns = code.previous();
/*  845 */       if (!code.hasPrevious())
/*  846 */         return null;
/*  847 */       Instruction prevIns = code.previous();
/*      */ 
/*  849 */       if (((prevIns instanceof ClassInstruction)) && (code.hasPrevious()))
/*      */       {
/*  851 */         prevIns = code.previous();
/*  852 */         backupCount++;
/*      */       }
/*      */ 
/*  855 */       if (!code.hasPrevious())
/*  856 */         return null;
/*  857 */       Instruction earlierIns = code.previous();
/*      */ 
/*  862 */       if ((!(earlierIns instanceof LoadInstruction)) || (!((LoadInstruction)earlierIns).isThis()))
/*      */       {
/*  864 */         return null;
/*      */       }
/*      */       BCField cur;
/*  868 */       if ((!findAccessed) && ((prevIns instanceof GetFieldInstruction))) {
/*  869 */         FieldInstruction fPrevIns = (FieldInstruction)prevIns;
/*  870 */         cur = (BCField)AccessController.doPrivileged(J2DoPrivHelper.getFieldInstructionFieldAction(fPrevIns));
/*      */       }
/*      */       else
/*      */       {
/*      */         BCField cur;
/*  873 */         if ((findAccessed) && ((prevIns instanceof LoadInstruction)) && (((LoadInstruction)prevIns).getParam() == 0))
/*      */         {
/*  875 */           FieldInstruction fTemplateIns = (FieldInstruction)templateIns;
/*      */ 
/*  877 */           cur = (BCField)AccessController.doPrivileged(J2DoPrivHelper.getFieldInstructionFieldAction(fTemplateIns));
/*      */         } else {
/*  879 */           return null;
/*      */         }
/*      */       }
/*      */       BCField cur;
/*  881 */       if ((field != null) && (cur != field))
/*  882 */         return null;
/*  883 */       field = cur;
/*      */ 
/*  886 */       while (backupCount > 0) {
/*  887 */         code.next();
/*  888 */         backupCount--;
/*      */       }
/*      */     }
/*  891 */     return field;
/*      */   }
/*      */ 
/*      */   private void addViolation(String key, Object[] args, boolean fatal)
/*      */   {
/*  898 */     if (this._violations == null)
/*  899 */       this._violations = new HashSet();
/*  900 */     this._violations.add(_loc.get(key, args));
/*  901 */     this._fail |= fatal;
/*      */   }
/*      */ 
/*      */   private void processViolations()
/*      */   {
/*  908 */     if (this._violations == null) {
/*  909 */       return;
/*      */     }
/*  911 */     String sep = J2DoPrivHelper.getLineSeparator();
/*  912 */     StringBuilder buf = new StringBuilder();
/*  913 */     for (Iterator itr = this._violations.iterator(); itr.hasNext(); ) {
/*  914 */       buf.append(itr.next());
/*  915 */       if (itr.hasNext())
/*  916 */         buf.append(sep);
/*      */     }
/*  918 */     Localizer.Message msg = _loc.get("property-violations", buf);
/*      */ 
/*  920 */     if (this._fail)
/*  921 */       throw new UserException(msg);
/*  922 */     if (this._log.isWarnEnabled())
/*  923 */       this._log.warn(msg);
/*      */   }
/*      */ 
/*      */   private void replaceAndValidateFieldAccess()
/*      */     throws NoSuchMethodException
/*      */   {
/*  934 */     Code template = (Code)AccessController.doPrivileged(J2DoPrivHelper.newCodeAction());
/*  935 */     Instruction put = template.putfield();
/*  936 */     Instruction get = template.getfield();
/*  937 */     Instruction stat = template.invokestatic();
/*      */ 
/*  941 */     BCMethod[] methods = this._managedType.getDeclaredMethods();
/*      */ 
/*  943 */     for (int i = 0; i < methods.length; i++) {
/*  944 */       Code code = methods[i].getCode(false);
/*      */ 
/*  947 */       if ((code != null) && (!skipEnhance(methods[i]))) {
/*  948 */         replaceAndValidateFieldAccess(code, get, true, stat);
/*  949 */         replaceAndValidateFieldAccess(code, put, false, stat);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void replaceAndValidateFieldAccess(Code code, Instruction ins, boolean get, Instruction stat)
/*      */     throws NoSuchMethodException
/*      */   {
/*  969 */     code.beforeFirst();
/*      */ 
/*  975 */     while (code.searchForward(ins))
/*      */     {
/*  977 */       FieldInstruction fi = (FieldInstruction)code.previous();
/*  978 */       String name = fi.getFieldName();
/*  979 */       String typeName = fi.getFieldTypeName();
/*  980 */       ClassMetaData owner = getPersistenceCapableOwner(name, fi.getFieldDeclarerType());
/*  981 */       FieldMetaData fmd = owner == null ? null : owner.getField(name);
/*  982 */       if (isPropertyAccess(fmd))
/*      */       {
/*  985 */         if ((owner != this._meta) && (owner.getDeclaredField(name) != null) && (this._meta != null) && (!owner.getDescribedType().isAssignableFrom(this._meta.getDescribedType())))
/*      */         {
/*  988 */           throw new UserException(_loc.get("property-field-access", new Object[] { this._meta, owner, name, code.getMethod().getName() }));
/*      */         }
/*      */ 
/*  994 */         if (isBackingFieldOfAnotherProperty(name, code)) {
/*  995 */           addViolation("property-field-access", new Object[] { this._meta, owner, name, code.getMethod().getName() }, false);
/*      */         }
/*      */       }
/*      */ 
/*  999 */       if ((owner == null) || (owner.getDeclaredField(fromBackingFieldName(name)) == null))
/*      */       {
/* 1002 */         code.next();
/*      */       } else {
/* 1004 */         if ((!getRedefine()) && (!getCreateSubclass()) && (isFieldAccess(fmd)))
/*      */         {
/* 1008 */           MethodInstruction mi = (MethodInstruction)code.set(stat);
/*      */ 
/* 1011 */           String prefix = get ? "pcGet" : "pcSet";
/* 1012 */           String methodName = new StringBuilder().append(prefix).append(name).toString();
/* 1013 */           if (get) {
/* 1014 */             mi.setMethod(getType(owner).getName(), methodName, typeName, new String[] { getType(owner).getName() });
/*      */           }
/*      */           else
/*      */           {
/* 1018 */             mi.setMethod(getType(owner).getName(), methodName, "void", new String[] { getType(owner).getName(), typeName });
/*      */           }
/*      */ 
/* 1022 */           code.next();
/* 1023 */         } else if (getRedefine()) {
/* 1024 */           name = fromBackingFieldName(name);
/* 1025 */           if (get) {
/* 1026 */             addNotifyAccess(code, owner.getField(name));
/* 1027 */             code.next();
/*      */           }
/*      */           else
/*      */           {
/* 1032 */             loadManagedInstance(code, false);
/* 1033 */             FieldInstruction fFi = fi;
/* 1034 */             code.getfield().setField((BCField)AccessController.doPrivileged(J2DoPrivHelper.getFieldInstructionFieldAction(fFi)));
/*      */ 
/* 1036 */             int val = code.getNextLocalsIndex();
/* 1037 */             code.xstore().setLocal(val).setType(fi.getFieldType());
/*      */ 
/* 1040 */             code.next();
/* 1041 */             addNotifyMutation(code, owner.getField(name), val, -1);
/*      */           }
/*      */         } else {
/* 1044 */           code.next();
/*      */         }
/* 1046 */         code.calculateMaxLocals();
/* 1047 */         code.calculateMaxStack();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void addNotifyAccess(Code code, FieldMetaData fmd) {
/* 1053 */     code.aload().setThis();
/* 1054 */     code.constant().setValue(fmd.getIndex());
/* 1055 */     code.invokestatic().setMethod(RedefinitionHelper.class, "accessingField", Void.TYPE, new Class[] { Object.class, Integer.TYPE });
/*      */   }
/*      */ 
/*      */   private void addNotifyMutation(Code code, FieldMetaData fmd, int val, int param)
/*      */     throws NoSuchMethodException
/*      */   {
/* 1076 */     code.aload().setThis();
/* 1077 */     code.constant().setValue(fmd.getIndex());
/* 1078 */     Class type = fmd.getDeclaredType();
/*      */ 
/* 1080 */     if ((!type.isPrimitive()) && (type != String.class))
/* 1081 */       type = Object.class;
/* 1082 */     code.xload().setLocal(val).setType(type);
/* 1083 */     if (param == -1) {
/* 1084 */       loadManagedInstance(code, false);
/* 1085 */       addGetManagedValueCode(code, fmd);
/*      */     } else {
/* 1087 */       code.xload().setParam(param).setType(type);
/*      */     }
/* 1089 */     code.invokestatic().setMethod(RedefinitionHelper.class, "settingField", Void.TYPE, new Class[] { Object.class, Integer.TYPE, type, type });
/*      */   }
/*      */ 
/*      */   private boolean isBackingFieldOfAnotherProperty(String name, Code code)
/*      */   {
/* 1100 */     String methName = code.getMethod().getName();
/* 1101 */     return (!"<init>".equals(methName)) && (this._backingFields != null) && (!name.equals(this._backingFields.get(methName))) && (this._backingFields.containsValue(name));
/*      */   }
/*      */ 
/*      */   private ClassMetaData getPersistenceCapableOwner(String fieldName, Class owner)
/*      */   {
/* 1120 */     Field f = Reflection.findField(owner, fieldName, false);
/* 1121 */     if (f == null) {
/* 1122 */       return null;
/*      */     }
/*      */ 
/* 1125 */     if ((this._meta != null) && (this._meta.getDescribedType().isInterface())) {
/* 1126 */       return this._meta;
/*      */     }
/* 1128 */     return this._repos.getMetaData(f.getDeclaringClass(), null, false);
/*      */   }
/*      */ 
/*      */   private void addPCMethods()
/*      */     throws NoSuchMethodException
/*      */   {
/* 1142 */     addClearFieldsMethod();
/* 1143 */     addNewInstanceMethod(true);
/* 1144 */     addNewInstanceMethod(false);
/* 1145 */     addManagedFieldCountMethod();
/* 1146 */     addReplaceFieldsMethods();
/* 1147 */     addProvideFieldsMethods();
/* 1148 */     addCopyFieldsMethod();
/*      */ 
/* 1150 */     if ((this._meta.getPCSuperclass() == null) || (getCreateSubclass())) {
/* 1151 */       addStockMethods();
/* 1152 */       addGetVersionMethod();
/* 1153 */       addReplaceStateManagerMethod();
/*      */ 
/* 1155 */       if (this._meta.getIdentityType() != 2) {
/* 1156 */         addNoOpApplicationIdentityMethods();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1163 */     if ((this._meta.getIdentityType() == 2) && ((this._meta.getPCSuperclass() == null) || (getCreateSubclass()) || (this._meta.getObjectIdType() != this._meta.getPCSuperclassMetaData().getObjectIdType())))
/*      */     {
/* 1167 */       addCopyKeyFieldsToObjectIdMethod(true);
/* 1168 */       addCopyKeyFieldsToObjectIdMethod(false);
/* 1169 */       addCopyKeyFieldsFromObjectIdMethod(true);
/* 1170 */       addCopyKeyFieldsFromObjectIdMethod(false);
/* 1171 */       if (this._meta.hasAbstractPKField() == true) {
/* 1172 */         addGetIDOwningClass();
/*      */       }
/* 1174 */       addNewObjectIdInstanceMethod(true);
/* 1175 */       addNewObjectIdInstanceMethod(false);
/*      */     }
/* 1177 */     else if (this._meta.hasPKFieldsFromAbstractClass()) {
/* 1178 */       addGetIDOwningClass();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void addClearFieldsMethod()
/*      */     throws NoSuchMethodException
/*      */   {
/* 1191 */     BCMethod method = this._pc.declareMethod("pcClearFields", Void.TYPE, null);
/*      */ 
/* 1193 */     method.makeProtected();
/* 1194 */     Code code = method.getCode(true);
/*      */ 
/* 1197 */     if ((this._meta.getPCSuperclass() != null) && (!getCreateSubclass())) {
/* 1198 */       code.aload().setThis();
/* 1199 */       code.invokespecial().setMethod(getType(this._meta.getPCSuperclassMetaData()), "pcClearFields", Void.TYPE, null);
/*      */     }
/*      */ 
/* 1204 */     FieldMetaData[] fmds = this._meta.getDeclaredFields();
/* 1205 */     for (int i = 0; i < fmds.length; i++) {
/* 1206 */       if (fmds[i].getManagement() == 3)
/*      */       {
/* 1209 */         loadManagedInstance(code, false);
/* 1210 */         switch (fmds[i].getDeclaredTypeCode()) {
/*      */         case 0:
/*      */         case 1:
/*      */         case 2:
/*      */         case 5:
/*      */         case 7:
/* 1216 */           code.constant().setValue(0);
/* 1217 */           break;
/*      */         case 3:
/* 1219 */           code.constant().setValue(0.0D);
/* 1220 */           break;
/*      */         case 4:
/* 1222 */           code.constant().setValue(0.0F);
/* 1223 */           break;
/*      */         case 6:
/* 1225 */           code.constant().setValue(0L);
/* 1226 */           break;
/*      */         default:
/* 1228 */           code.constant().setNull();
/*      */         }
/*      */ 
/* 1232 */         addSetManagedValueCode(code, fmds[i]);
/*      */       }
/*      */     }
/* 1235 */     code.vreturn();
/* 1236 */     code.calculateMaxStack();
/* 1237 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addNewInstanceMethod(boolean oid)
/*      */   {
/* 1250 */     Class[] args = { SMTYPE, oid ? new Class[] { SMTYPE, Object.class, Boolean.TYPE } : Boolean.TYPE };
/*      */ 
/* 1253 */     BCMethod method = this._pc.declareMethod("pcNewInstance", PCTYPE, args);
/* 1254 */     Code code = method.getCode(true);
/*      */ 
/* 1257 */     if (this._pc.isAbstract()) {
/* 1258 */       throwException(code, USEREXCEP);
/* 1259 */       code.vreturn();
/*      */ 
/* 1261 */       code.calculateMaxStack();
/* 1262 */       code.calculateMaxLocals();
/* 1263 */       return;
/*      */     }
/*      */ 
/* 1267 */     code.anew().setType(this._pc);
/* 1268 */     code.dup();
/* 1269 */     code.invokespecial().setMethod("<init>", Void.TYPE, null);
/* 1270 */     int inst = code.getNextLocalsIndex();
/* 1271 */     code.astore().setLocal(inst);
/*      */ 
/* 1275 */     code.iload().setParam(oid ? 2 : 1);
/* 1276 */     JumpInstruction noclear = code.ifeq();
/* 1277 */     code.aload().setLocal(inst);
/* 1278 */     code.invokevirtual().setMethod("pcClearFields", Void.TYPE, null);
/*      */ 
/* 1281 */     noclear.setTarget(code.aload().setLocal(inst));
/* 1282 */     code.aload().setParam(0);
/* 1283 */     code.putfield().setField("pcStateManager", SMTYPE);
/*      */ 
/* 1286 */     if (oid) {
/* 1287 */       code.aload().setLocal(inst);
/* 1288 */       code.aload().setParam(1);
/* 1289 */       code.invokevirtual().setMethod("pcCopyKeyFieldsFromObjectId", Void.TYPE, new Class[] { Object.class });
/*      */     }
/*      */ 
/* 1294 */     code.aload().setLocal(inst);
/* 1295 */     code.areturn();
/*      */ 
/* 1297 */     code.calculateMaxStack();
/* 1298 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addManagedFieldCountMethod()
/*      */   {
/* 1308 */     BCMethod method = this._pc.declareMethod("pcGetManagedFieldCount", Integer.TYPE, null);
/*      */ 
/* 1310 */     method.setStatic(true);
/* 1311 */     method.makeProtected();
/* 1312 */     Code code = method.getCode(true);
/*      */ 
/* 1320 */     code.constant().setValue(this._meta.getDeclaredFields().length);
/* 1321 */     if (this._meta.getPCSuperclass() != null) {
/* 1322 */       Class superClass = getType(this._meta.getPCSuperclassMetaData());
/* 1323 */       String superName = getCreateSubclass() ? toPCSubclassName(superClass) : superClass.getName();
/*      */ 
/* 1326 */       code.invokestatic().setMethod(superName, "pcGetManagedFieldCount", Integer.TYPE.getName(), null);
/*      */ 
/* 1328 */       code.iadd();
/*      */     }
/* 1330 */     code.ireturn();
/* 1331 */     code.calculateMaxStack();
/*      */   }
/*      */ 
/*      */   private void addProvideFieldsMethods()
/*      */     throws NoSuchMethodException
/*      */   {
/* 1342 */     BCMethod method = this._pc.declareMethod("pcProvideField", Void.TYPE, new Class[] { Integer.TYPE });
/*      */ 
/* 1344 */     Code code = method.getCode(true);
/*      */ 
/* 1347 */     int relLocal = beginSwitchMethod("pcProvideField", code);
/*      */ 
/* 1350 */     FieldMetaData[] fmds = getCreateSubclass() ? this._meta.getFields() : this._meta.getDeclaredFields();
/*      */ 
/* 1352 */     if (fmds.length == 0) {
/* 1353 */       throwException(code, IllegalArgumentException.class);
/*      */     }
/*      */     else {
/* 1356 */       code.iload().setLocal(relLocal);
/* 1357 */       TableSwitchInstruction tabins = code.tableswitch();
/* 1358 */       tabins.setLow(0);
/* 1359 */       tabins.setHigh(fmds.length - 1);
/*      */ 
/* 1363 */       for (int i = 0; i < fmds.length; i++) {
/* 1364 */         tabins.addTarget(loadManagedInstance(code, false));
/* 1365 */         code.getfield().setField("pcStateManager", SMTYPE);
/* 1366 */         loadManagedInstance(code, false);
/* 1367 */         code.iload().setParam(0);
/* 1368 */         loadManagedInstance(code, false);
/* 1369 */         addGetManagedValueCode(code, fmds[i]);
/* 1370 */         code.invokeinterface().setMethod(getStateManagerMethod(fmds[i].getDeclaredType(), "provided", false, false));
/*      */ 
/* 1372 */         code.vreturn();
/*      */       }
/*      */ 
/* 1376 */       tabins.setDefaultTarget(throwException(code, IllegalArgumentException.class));
/*      */     }
/*      */ 
/* 1380 */     code.calculateMaxStack();
/* 1381 */     code.calculateMaxLocals();
/*      */ 
/* 1383 */     addMultipleFieldsMethodVersion(method);
/*      */   }
/*      */ 
/*      */   private void addReplaceFieldsMethods()
/*      */     throws NoSuchMethodException
/*      */   {
/* 1394 */     BCMethod method = this._pc.declareMethod("pcReplaceField", Void.TYPE, new Class[] { Integer.TYPE });
/*      */ 
/* 1396 */     Code code = method.getCode(true);
/*      */ 
/* 1399 */     int relLocal = beginSwitchMethod("pcReplaceField", code);
/*      */ 
/* 1402 */     FieldMetaData[] fmds = getCreateSubclass() ? this._meta.getFields() : this._meta.getDeclaredFields();
/*      */ 
/* 1404 */     if (fmds.length == 0) {
/* 1405 */       throwException(code, IllegalArgumentException.class);
/*      */     }
/*      */     else {
/* 1408 */       code.iload().setLocal(relLocal);
/* 1409 */       TableSwitchInstruction tabins = code.tableswitch();
/* 1410 */       tabins.setLow(0);
/* 1411 */       tabins.setHigh(fmds.length - 1);
/*      */ 
/* 1415 */       for (int i = 0; i < fmds.length; i++)
/*      */       {
/* 1417 */         tabins.addTarget(loadManagedInstance(code, false, fmds[i]));
/*      */ 
/* 1419 */         loadManagedInstance(code, false, fmds[i]);
/* 1420 */         code.getfield().setField("pcStateManager", SMTYPE);
/* 1421 */         loadManagedInstance(code, false, fmds[i]);
/* 1422 */         code.iload().setParam(0);
/* 1423 */         code.invokeinterface().setMethod(getStateManagerMethod(fmds[i].getDeclaredType(), "replace", true, false));
/*      */ 
/* 1425 */         if (!fmds[i].getDeclaredType().isPrimitive()) {
/* 1426 */           code.checkcast().setType(fmds[i].getDeclaredType());
/*      */         }
/* 1428 */         addSetManagedValueCode(code, fmds[i]);
/* 1429 */         if ((this._addVersionInitFlag) && 
/* 1430 */           (fmds[i].isVersion()))
/*      */         {
/* 1433 */           loadManagedInstance(code, false);
/* 1434 */           code.constant().setValue(1);
/* 1435 */           putfield(code, null, "pcVersionInit", Boolean.TYPE);
/*      */         }
/*      */ 
/* 1438 */         code.vreturn();
/*      */       }
/*      */ 
/* 1442 */       tabins.setDefaultTarget(throwException(code, IllegalArgumentException.class));
/*      */     }
/*      */ 
/* 1446 */     code.calculateMaxStack();
/* 1447 */     code.calculateMaxLocals();
/*      */ 
/* 1449 */     addMultipleFieldsMethodVersion(method);
/*      */   }
/*      */ 
/*      */   private void addCopyFieldsMethod()
/*      */     throws NoSuchMethodException
/*      */   {
/* 1459 */     BCMethod method = this._pc.declareMethod("pcCopyField", Void.TYPE.getName(), new String[] { this._managedType.getName(), Integer.TYPE.getName() });
/*      */ 
/* 1462 */     method.makeProtected();
/* 1463 */     Code code = method.getCode(true);
/*      */ 
/* 1466 */     int relLocal = beginSwitchMethod("pcCopyField", code);
/*      */ 
/* 1469 */     FieldMetaData[] fmds = getCreateSubclass() ? this._meta.getFields() : this._meta.getDeclaredFields();
/*      */ 
/* 1471 */     if (fmds.length == 0) {
/* 1472 */       throwException(code, IllegalArgumentException.class);
/*      */     }
/*      */     else {
/* 1475 */       code.iload().setLocal(relLocal);
/* 1476 */       TableSwitchInstruction tabins = code.tableswitch();
/* 1477 */       tabins.setLow(0);
/* 1478 */       tabins.setHigh(fmds.length - 1);
/*      */ 
/* 1480 */       for (int i = 0; i < fmds.length; i++)
/*      */       {
/* 1483 */         tabins.addTarget(loadManagedInstance(code, false, fmds[i]));
/* 1484 */         code.aload().setParam(0);
/* 1485 */         addGetManagedValueCode(code, fmds[i], false);
/* 1486 */         addSetManagedValueCode(code, fmds[i]);
/*      */ 
/* 1489 */         code.vreturn();
/*      */       }
/*      */ 
/* 1493 */       tabins.setDefaultTarget(throwException(code, IllegalArgumentException.class));
/*      */     }
/*      */ 
/* 1497 */     code.calculateMaxStack();
/* 1498 */     code.calculateMaxLocals();
/*      */ 
/* 1500 */     addMultipleFieldsMethodVersion(method);
/*      */   }
/*      */ 
/*      */   private int beginSwitchMethod(String name, Code code)
/*      */   {
/* 1513 */     boolean copy = "pcCopyField".equals(name);
/* 1514 */     int fieldNumber = copy ? 1 : 0;
/*      */ 
/* 1516 */     int relLocal = code.getNextLocalsIndex();
/* 1517 */     if (getCreateSubclass()) {
/* 1518 */       code.iload().setParam(fieldNumber);
/* 1519 */       code.istore().setLocal(relLocal);
/* 1520 */       return relLocal;
/*      */     }
/*      */ 
/* 1524 */     code.iload().setParam(fieldNumber);
/* 1525 */     code.getstatic().setField("pcInheritedFieldCount", Integer.TYPE);
/* 1526 */     code.isub();
/* 1527 */     code.istore().setLocal(relLocal);
/* 1528 */     code.iload().setLocal(relLocal);
/*      */ 
/* 1532 */     JumpInstruction ifins = code.ifge();
/* 1533 */     if (this._meta.getPCSuperclass() != null) {
/* 1534 */       loadManagedInstance(code, false);
/*      */       String[] args;
/* 1536 */       if (copy) {
/* 1537 */         String[] args = { getType(this._meta.getPCSuperclassMetaData()).getName(), Integer.TYPE.getName() };
/*      */ 
/* 1539 */         code.aload().setParam(0);
/*      */       } else {
/* 1541 */         args = new String[] { Integer.TYPE.getName() };
/* 1542 */       }code.iload().setParam(fieldNumber);
/* 1543 */       code.invokespecial().setMethod(getType(this._meta.getPCSuperclassMetaData()).getName(), name, Void.TYPE.getName(), args);
/*      */ 
/* 1546 */       code.vreturn();
/*      */     } else {
/* 1548 */       throwException(code, IllegalArgumentException.class);
/*      */     }
/* 1550 */     ifins.setTarget(code.nop());
/* 1551 */     return relLocal;
/*      */   }
/*      */ 
/*      */   private void addMultipleFieldsMethodVersion(BCMethod single)
/*      */   {
/* 1563 */     boolean copy = "pcCopyField".equals(single.getName());
/*      */ 
/* 1566 */     Class[] args = { copy ? new Class[] { Object.class, [I.class } : [I.class };
/*      */ 
/* 1568 */     BCMethod method = this._pc.declareMethod(new StringBuilder().append(single.getName()).append("s").toString(), Void.TYPE, args);
/*      */ 
/* 1570 */     Code code = method.getCode(true);
/*      */ 
/* 1572 */     int fieldNumbers = 0;
/* 1573 */     int inst = 0;
/* 1574 */     if (copy) {
/* 1575 */       fieldNumbers = 1;
/*      */ 
/* 1577 */       if (getCreateSubclass())
/*      */       {
/* 1579 */         code.aload().setParam(0);
/* 1580 */         code.invokestatic().setMethod(ImplHelper.class, "getManagedInstance", Object.class, new Class[] { Object.class });
/*      */ 
/* 1583 */         code.checkcast().setType(this._managedType);
/* 1584 */         inst = code.getNextLocalsIndex();
/* 1585 */         code.astore().setLocal(inst);
/*      */ 
/* 1589 */         code.aload().setParam(0);
/* 1590 */         code.aload().setThis();
/* 1591 */         code.getfield().setField("pcStateManager", SMTYPE);
/* 1592 */         code.invokestatic().setMethod(ImplHelper.class, "toPersistenceCapable", PersistenceCapable.class, new Class[] { Object.class, Object.class });
/*      */ 
/* 1595 */         code.invokeinterface().setMethod(PersistenceCapable.class, "pcGetStateManager", StateManager.class, null);
/*      */       }
/*      */       else
/*      */       {
/* 1599 */         code.aload().setParam(0);
/* 1600 */         code.checkcast().setType(this._pc);
/* 1601 */         inst = code.getNextLocalsIndex();
/* 1602 */         code.astore().setLocal(inst);
/*      */ 
/* 1605 */         code.aload().setLocal(inst);
/* 1606 */         code.getfield().setField("pcStateManager", SMTYPE);
/*      */       }
/*      */ 
/* 1612 */       loadManagedInstance(code, false);
/* 1613 */       code.getfield().setField("pcStateManager", SMTYPE);
/* 1614 */       JumpInstruction ifins = code.ifacmpeq();
/* 1615 */       throwException(code, IllegalArgumentException.class);
/* 1616 */       ifins.setTarget(code.nop());
/*      */ 
/* 1620 */       loadManagedInstance(code, false);
/* 1621 */       code.getfield().setField("pcStateManager", SMTYPE);
/* 1622 */       ifins = code.ifnonnull();
/* 1623 */       throwException(code, IllegalStateException.class);
/* 1624 */       ifins.setTarget(code.nop());
/*      */     }
/*      */ 
/* 1628 */     code.constant().setValue(0);
/* 1629 */     int idx = code.getNextLocalsIndex();
/* 1630 */     code.istore().setLocal(idx);
/* 1631 */     JumpInstruction testins = code.go2();
/*      */ 
/* 1634 */     Instruction bodyins = loadManagedInstance(code, false);
/* 1635 */     if (copy)
/* 1636 */       code.aload().setLocal(inst);
/* 1637 */     code.aload().setParam(fieldNumbers);
/* 1638 */     code.iload().setLocal(idx);
/* 1639 */     code.iaload();
/* 1640 */     code.invokevirtual().setMethod(single);
/*      */ 
/* 1643 */     code.iinc().setIncrement(1).setLocal(idx);
/*      */ 
/* 1646 */     testins.setTarget(code.iload().setLocal(idx));
/* 1647 */     code.aload().setParam(fieldNumbers);
/* 1648 */     code.arraylength();
/* 1649 */     code.ificmplt().setTarget(bodyins);
/* 1650 */     code.vreturn();
/*      */ 
/* 1652 */     code.calculateMaxStack();
/* 1653 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addStockMethods()
/*      */     throws NoSuchMethodException
/*      */   {
/*      */     try
/*      */     {
/* 1666 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "getGenericContext", (Class[])null)), false);
/*      */ 
/* 1671 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "fetchObjectId", (Class[])null)), false);
/*      */ 
/* 1676 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "isDeleted", (Class[])null)), false);
/*      */ 
/* 1681 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "isDirty", (Class[])null)), true);
/*      */ 
/* 1685 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "isNew", (Class[])null)), false);
/*      */ 
/* 1689 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "isPersistent", (Class[])null)), false);
/*      */ 
/* 1694 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "isTransactional", (Class[])null)), false);
/*      */ 
/* 1699 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "serializing", (Class[])null)), false);
/*      */ 
/* 1704 */       translateFromStateManagerMethod((Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(SMTYPE, "dirty", new Class[] { String.class })), false);
/*      */ 
/* 1709 */       BCMethod meth = this._pc.declareMethod("pcGetStateManager", StateManager.class, null);
/*      */ 
/* 1711 */       Code code = meth.getCode(true);
/* 1712 */       loadManagedInstance(code, false);
/* 1713 */       code.getfield().setField("pcStateManager", StateManager.class);
/* 1714 */       code.areturn();
/* 1715 */       code.calculateMaxStack();
/* 1716 */       code.calculateMaxLocals();
/*      */     } catch (PrivilegedActionException pae) {
/* 1718 */       throw ((NoSuchMethodException)pae.getException());
/*      */     }
/*      */   }
/*      */ 
/*      */   private void translateFromStateManagerMethod(Method m, boolean isDirtyCheckMethod)
/*      */   {
/* 1731 */     String name = new StringBuilder().append("pc").append(StringUtils.capitalize(m.getName())).toString();
/* 1732 */     Class[] params = m.getParameterTypes();
/* 1733 */     Class returnType = m.getReturnType();
/*      */ 
/* 1736 */     BCMethod method = this._pc.declareMethod(name, returnType, params);
/* 1737 */     Code code = method.getCode(true);
/*      */ 
/* 1740 */     loadManagedInstance(code, false);
/* 1741 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 1742 */     JumpInstruction ifins = code.ifnonnull();
/* 1743 */     if (returnType.equals(Boolean.TYPE))
/* 1744 */       code.constant().setValue(false);
/* 1745 */     else if (!returnType.equals(Void.TYPE))
/* 1746 */       code.constant().setNull();
/* 1747 */     code.xreturn().setType(returnType);
/*      */ 
/* 1751 */     if ((isDirtyCheckMethod) && (!getRedefine()))
/*      */     {
/* 1753 */       ifins.setTarget(loadManagedInstance(code, false));
/* 1754 */       code.getfield().setField("pcStateManager", SMTYPE);
/* 1755 */       code.dup();
/* 1756 */       code.invokestatic().setMethod(RedefinitionHelper.class, "dirtyCheck", Void.TYPE, new Class[] { SMTYPE });
/*      */     }
/*      */     else {
/* 1759 */       ifins.setTarget(loadManagedInstance(code, false));
/* 1760 */       code.getfield().setField("pcStateManager", SMTYPE);
/*      */     }
/*      */ 
/* 1765 */     for (int i = 0; i < params.length; i++)
/* 1766 */       code.xload().setParam(i);
/* 1767 */     code.invokeinterface().setMethod(m);
/* 1768 */     code.xreturn().setType(returnType);
/*      */ 
/* 1770 */     code.calculateMaxStack();
/* 1771 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addGetVersionMethod()
/*      */     throws NoSuchMethodException
/*      */   {
/* 1780 */     BCMethod method = this._pc.declareMethod("pcGetVersion", Object.class, null);
/*      */ 
/* 1782 */     Code code = method.getCode(true);
/*      */ 
/* 1785 */     loadManagedInstance(code, false);
/* 1786 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 1787 */     JumpInstruction ifins = code.ifnonnull();
/* 1788 */     FieldMetaData versionField = this._meta.getVersionField();
/*      */ 
/* 1790 */     if (versionField == null) {
/* 1791 */       code.constant().setNull();
/*      */     }
/*      */     else {
/* 1794 */       Class wrapper = toPrimitiveWrapper(versionField);
/* 1795 */       if (wrapper != versionField.getDeclaredType()) {
/* 1796 */         code.anew().setType(wrapper);
/* 1797 */         code.dup();
/*      */       }
/* 1799 */       loadManagedInstance(code, false);
/* 1800 */       addGetManagedValueCode(code, versionField);
/* 1801 */       if (wrapper != versionField.getDeclaredType()) {
/* 1802 */         code.invokespecial().setMethod(wrapper, "<init>", Void.TYPE, new Class[] { versionField.getDeclaredType() });
/*      */       }
/*      */     }
/* 1805 */     code.areturn();
/*      */ 
/* 1808 */     ifins.setTarget(loadManagedInstance(code, false));
/* 1809 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 1810 */     code.invokeinterface().setMethod(SMTYPE, "getVersion", Object.class, null);
/*      */ 
/* 1812 */     code.areturn();
/*      */ 
/* 1814 */     code.calculateMaxStack();
/* 1815 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private Class toPrimitiveWrapper(FieldMetaData fmd)
/*      */   {
/* 1823 */     switch (fmd.getDeclaredTypeCode()) {
/*      */     case 0:
/* 1825 */       return Boolean.class;
/*      */     case 1:
/* 1827 */       return Byte.class;
/*      */     case 2:
/* 1829 */       return Character.class;
/*      */     case 3:
/* 1831 */       return Double.class;
/*      */     case 4:
/* 1833 */       return Float.class;
/*      */     case 5:
/* 1835 */       return Integer.class;
/*      */     case 6:
/* 1837 */       return Long.class;
/*      */     case 7:
/* 1839 */       return Short.class;
/*      */     }
/* 1841 */     return fmd.getDeclaredType();
/*      */   }
/*      */ 
/*      */   private void addReplaceStateManagerMethod()
/*      */   {
/* 1850 */     BCMethod method = this._pc.declareMethod("pcReplaceStateManager", Void.TYPE, new Class[] { SMTYPE });
/*      */ 
/* 1852 */     method.setSynchronized(true);
/* 1853 */     method.getExceptions(true).addException(SecurityException.class);
/* 1854 */     Code code = method.getCode(true);
/*      */ 
/* 1858 */     loadManagedInstance(code, false);
/* 1859 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 1860 */     JumpInstruction ifins = code.ifnull();
/* 1861 */     loadManagedInstance(code, false);
/* 1862 */     loadManagedInstance(code, false);
/* 1863 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 1864 */     code.aload().setParam(0);
/* 1865 */     code.invokeinterface().setMethod(SMTYPE, "replaceStateManager", SMTYPE, new Class[] { SMTYPE });
/*      */ 
/* 1867 */     code.putfield().setField("pcStateManager", SMTYPE);
/* 1868 */     code.vreturn();
/*      */ 
/* 1873 */     ifins.setTarget(code.invokestatic().setMethod(System.class, "getSecurityManager", SecurityManager.class, null));
/*      */ 
/* 1877 */     ifins.setTarget(loadManagedInstance(code, false));
/* 1878 */     code.aload().setParam(0);
/* 1879 */     code.putfield().setField("pcStateManager", SMTYPE);
/* 1880 */     code.vreturn();
/*      */ 
/* 1882 */     code.calculateMaxStack();
/* 1883 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addNoOpApplicationIdentityMethods()
/*      */   {
/* 1893 */     BCMethod method = this._pc.declareMethod("pcCopyKeyFieldsToObjectId", Void.TYPE, new Class[] { OIDFSTYPE, Object.class });
/*      */ 
/* 1895 */     Code code = method.getCode(true);
/* 1896 */     code.vreturn();
/* 1897 */     code.calculateMaxLocals();
/*      */ 
/* 1900 */     method = this._pc.declareMethod("pcCopyKeyFieldsToObjectId", Void.TYPE, new Class[] { Object.class });
/*      */ 
/* 1902 */     code = method.getCode(true);
/* 1903 */     code.vreturn();
/* 1904 */     code.calculateMaxLocals();
/*      */ 
/* 1908 */     method = this._pc.declareMethod("pcCopyKeyFieldsFromObjectId", Void.TYPE, new Class[] { OIDFCTYPE, Object.class });
/*      */ 
/* 1910 */     code = method.getCode(true);
/* 1911 */     code.vreturn();
/* 1912 */     code.calculateMaxLocals();
/*      */ 
/* 1915 */     method = this._pc.declareMethod("pcCopyKeyFieldsFromObjectId", Void.TYPE, new Class[] { Object.class });
/*      */ 
/* 1917 */     code = method.getCode(true);
/* 1918 */     code.vreturn();
/* 1919 */     code.calculateMaxLocals();
/*      */ 
/* 1922 */     method = this._pc.declareMethod("pcNewObjectIdInstance", Object.class, null);
/*      */ 
/* 1924 */     code = method.getCode(true);
/* 1925 */     code.constant().setNull();
/* 1926 */     code.areturn();
/* 1927 */     code.calculateMaxStack();
/* 1928 */     code.calculateMaxLocals();
/*      */ 
/* 1931 */     method = this._pc.declareMethod("pcNewObjectIdInstance", Object.class, new Class[] { Object.class });
/*      */ 
/* 1933 */     code = method.getCode(true);
/* 1934 */     code.constant().setNull();
/* 1935 */     code.areturn();
/* 1936 */     code.calculateMaxStack();
/* 1937 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addCopyKeyFieldsToObjectIdMethod(boolean fieldManager)
/*      */     throws NoSuchMethodException
/*      */   {
/* 1949 */     String[] args = { fieldManager ? new String[] { OIDFSTYPE.getName(), Object.class.getName() } : Object.class.getName() };
/*      */ 
/* 1952 */     BCMethod method = this._pc.declareMethod("pcCopyKeyFieldsToObjectId", Void.TYPE.getName(), args);
/*      */ 
/* 1954 */     Code code = method.getCode(true);
/*      */ 
/* 1957 */     if (this._meta.isOpenJPAIdentity()) {
/* 1958 */       throwException(code, INTERNEXCEP);
/* 1959 */       code.vreturn();
/*      */ 
/* 1961 */       code.calculateMaxStack();
/* 1962 */       code.calculateMaxLocals();
/* 1963 */       return;
/*      */     }
/*      */ 
/* 1967 */     if ((this._meta.getPCSuperclass() != null) && (!getCreateSubclass())) {
/* 1968 */       loadManagedInstance(code, false);
/* 1969 */       for (int i = 0; i < args.length; i++)
/* 1970 */         code.aload().setParam(i);
/* 1971 */       code.invokespecial().setMethod(getType(this._meta.getPCSuperclassMetaData()).getName(), "pcCopyKeyFieldsToObjectId", Void.TYPE.getName(), args);
/*      */     }
/*      */ 
/* 1977 */     if (fieldManager)
/* 1978 */       code.aload().setParam(1);
/*      */     else {
/* 1980 */       code.aload().setParam(0);
/*      */     }
/* 1982 */     if (this._meta.isObjectIdTypeShared())
/*      */     {
/* 1984 */       code.checkcast().setType(ObjectId.class);
/* 1985 */       code.invokevirtual().setMethod(ObjectId.class, "getId", Object.class, null);
/*      */     }
/*      */ 
/* 1990 */     int id = code.getNextLocalsIndex();
/* 1991 */     Class oidType = this._meta.getObjectIdType();
/* 1992 */     code.checkcast().setType(oidType);
/* 1993 */     code.astore().setLocal(id);
/*      */ 
/* 1996 */     int inherited = 0;
/* 1997 */     if (fieldManager) {
/* 1998 */       code.getstatic().setField("pcInheritedFieldCount", Integer.TYPE);
/* 1999 */       inherited = code.getNextLocalsIndex();
/* 2000 */       code.istore().setLocal(inherited);
/*      */     }
/*      */ 
/* 2005 */     FieldMetaData[] fmds = getCreateSubclass() ? this._meta.getFields() : this._meta.getDeclaredFields();
/*      */ 
/* 2012 */     for (int i = 0; i < fmds.length; i++)
/* 2013 */       if (fmds[i].isPrimaryKey())
/*      */       {
/* 2015 */         code.aload().setLocal(id);
/*      */ 
/* 2017 */         String name = fmds[i].getName();
/* 2018 */         Class type = fmds[i].getObjectIdFieldType();
/*      */         Field field;
/*      */         Method setter;
/*      */         boolean reflect;
/* 2019 */         if (isFieldAccess(fmds[i])) {
/* 2020 */           Method setter = null;
/* 2021 */           Field field = Reflection.findField(oidType, name, true);
/* 2022 */           boolean reflect = !Modifier.isPublic(field.getModifiers());
/* 2023 */           if (reflect) {
/* 2024 */             code.classconstant().setClass(oidType);
/* 2025 */             code.constant().setValue(name);
/* 2026 */             code.constant().setValue(true);
/* 2027 */             code.invokestatic().setMethod(Reflection.class, "findField", Field.class, new Class[] { Class.class, String.class, Boolean.TYPE });
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 2032 */           field = null;
/* 2033 */           setter = Reflection.findSetter(oidType, name, type, true);
/* 2034 */           reflect = !Modifier.isPublic(setter.getModifiers());
/* 2035 */           if (reflect) {
/* 2036 */             code.classconstant().setClass(oidType);
/* 2037 */             code.constant().setValue(name);
/* 2038 */             code.classconstant().setClass(type);
/* 2039 */             code.constant().setValue(true);
/* 2040 */             code.invokestatic().setMethod(Reflection.class, "findSetter", Method.class, new Class[] { Class.class, String.class, Class.class, Boolean.TYPE });
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 2046 */         if (fieldManager) {
/* 2047 */           code.aload().setParam(0);
/* 2048 */           code.constant().setValue(i);
/* 2049 */           code.iload().setLocal(inherited);
/* 2050 */           code.iadd();
/* 2051 */           code.invokeinterface().setMethod(getFieldSupplierMethod(type));
/*      */ 
/* 2053 */           if ((fmds[i].getObjectIdFieldTypeCode() == 8) && (!fmds[i].getDeclaredType().isEnum()))
/*      */           {
/* 2055 */             code.checkcast().setType(ObjectId.class);
/* 2056 */             code.invokevirtual().setMethod(ObjectId.class, "getId", Object.class, null);
/*      */           }
/*      */ 
/* 2063 */           if ((!reflect) && (!type.isPrimitive()) && (!type.getName().equals(String.class.getName())))
/*      */           {
/* 2065 */             code.checkcast().setType(type);
/*      */           }
/*      */         } else { loadManagedInstance(code, false);
/* 2068 */           addGetManagedValueCode(code, fmds[i]);
/*      */ 
/* 2071 */           if (fmds[i].getDeclaredTypeCode() == 15) {
/* 2072 */             addExtractObjectIdFieldValueCode(code, fmds[i]);
/*      */           }
/*      */         }
/* 2075 */         if ((reflect) && (field != null)) {
/* 2076 */           code.invokestatic().setMethod(Reflection.class, "set", Void.TYPE, new Class[] { Object.class, Field.class, type.isPrimitive() ? type : Object.class });
/*      */         }
/* 2079 */         else if (reflect) {
/* 2080 */           code.invokestatic().setMethod(Reflection.class, "set", Void.TYPE, new Class[] { Object.class, Method.class, type.isPrimitive() ? type : Object.class });
/*      */         }
/* 2083 */         else if (field != null)
/* 2084 */           code.putfield().setField(field);
/*      */         else
/* 2086 */           code.invokevirtual().setMethod(setter);
/*      */       }
/* 2088 */     code.vreturn();
/*      */ 
/* 2090 */     code.calculateMaxStack();
/* 2091 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addExtractObjectIdFieldValueCode(Code code, FieldMetaData pk)
/*      */   {
/* 2101 */     int pc = code.getNextLocalsIndex();
/* 2102 */     code.astore().setLocal(pc);
/* 2103 */     code.aload().setLocal(pc);
/* 2104 */     JumpInstruction ifnull1 = code.ifnull();
/* 2105 */     code.aload().setLocal(pc);
/* 2106 */     code.checkcast().setType(PersistenceCapable.class);
/* 2107 */     if (!pk.getTypeMetaData().isOpenJPAIdentity()) {
/* 2108 */       code.invokeinterface().setMethod(PersistenceCapable.class, "pcFetchObjectId", Object.class, null);
/*      */     }
/*      */     else {
/* 2111 */       code.invokeinterface().setMethod(PersistenceCapable.class, "pcNewObjectIdInstance", Object.class, null);
/*      */     }
/*      */ 
/* 2114 */     int oid = code.getNextLocalsIndex();
/* 2115 */     code.astore().setLocal(oid);
/* 2116 */     code.aload().setLocal(oid);
/* 2117 */     JumpInstruction ifnull2 = code.ifnull();
/*      */ 
/* 2122 */     ClassMetaData pkmeta = pk.getDeclaredTypeMetaData();
/* 2123 */     int pkcode = pk.getObjectIdFieldTypeCode();
/* 2124 */     Class pktype = pk.getObjectIdFieldType();
/* 2125 */     if ((pkmeta.getIdentityType() == 1) && (pkcode == 6))
/*      */     {
/* 2127 */       code.aload().setLocal(oid);
/* 2128 */       code.checkcast().setType(Id.class);
/* 2129 */       code.invokevirtual().setMethod(Id.class, "getId", Long.TYPE, null);
/*      */     }
/* 2131 */     else if (pkmeta.getIdentityType() == 1) {
/* 2132 */       code.aload().setLocal(oid);
/* 2133 */     } else if (pkmeta.isOpenJPAIdentity()) {
/* 2134 */       switch (pkcode) {
/*      */       case 17:
/* 2136 */         code.anew().setType(Byte.class);
/* 2137 */         code.dup();
/*      */       case 1:
/* 2140 */         code.aload().setLocal(oid);
/* 2141 */         code.checkcast().setType(ByteId.class);
/* 2142 */         code.invokevirtual().setMethod(ByteId.class, "getId", Byte.TYPE, null);
/*      */ 
/* 2144 */         if (pkcode != 17) break;
/* 2145 */         code.invokespecial().setMethod(Byte.class, "<init>", Void.TYPE, new Class[] { Byte.TYPE }); break;
/*      */       case 18:
/* 2149 */         code.anew().setType(Character.class);
/* 2150 */         code.dup();
/*      */       case 2:
/* 2153 */         code.aload().setLocal(oid);
/* 2154 */         code.checkcast().setType(CharId.class);
/* 2155 */         code.invokevirtual().setMethod(CharId.class, "getId", Character.TYPE, null);
/*      */ 
/* 2157 */         if (pkcode != 18) break;
/* 2158 */         code.invokespecial().setMethod(Character.class, "<init>", Void.TYPE, new Class[] { Character.TYPE }); break;
/*      */       case 19:
/* 2162 */         code.anew().setType(Double.class);
/* 2163 */         code.dup();
/*      */       case 3:
/* 2166 */         code.aload().setLocal(oid);
/* 2167 */         code.checkcast().setType(DoubleId.class);
/* 2168 */         code.invokevirtual().setMethod(DoubleId.class, "getId", Double.TYPE, null);
/*      */ 
/* 2170 */         if (pkcode != 19) break;
/* 2171 */         code.invokespecial().setMethod(Double.class, "<init>", Void.TYPE, new Class[] { Double.TYPE }); break;
/*      */       case 20:
/* 2175 */         code.anew().setType(Float.class);
/* 2176 */         code.dup();
/*      */       case 4:
/* 2179 */         code.aload().setLocal(oid);
/* 2180 */         code.checkcast().setType(FloatId.class);
/* 2181 */         code.invokevirtual().setMethod(FloatId.class, "getId", Float.TYPE, null);
/*      */ 
/* 2183 */         if (pkcode != 20) break;
/* 2184 */         code.invokespecial().setMethod(Float.class, "<init>", Void.TYPE, new Class[] { Float.TYPE }); break;
/*      */       case 21:
/* 2188 */         code.anew().setType(Integer.class);
/* 2189 */         code.dup();
/*      */       case 5:
/* 2192 */         code.aload().setLocal(oid);
/* 2193 */         code.checkcast().setType(IntId.class);
/* 2194 */         code.invokevirtual().setMethod(IntId.class, "getId", Integer.TYPE, null);
/*      */ 
/* 2196 */         if (pkcode != 21) break;
/* 2197 */         code.invokespecial().setMethod(Integer.class, "<init>", Void.TYPE, new Class[] { Integer.TYPE }); break;
/*      */       case 22:
/* 2201 */         code.anew().setType(Long.class);
/* 2202 */         code.dup();
/*      */       case 6:
/* 2205 */         code.aload().setLocal(oid);
/* 2206 */         code.checkcast().setType(LongId.class);
/* 2207 */         code.invokevirtual().setMethod(LongId.class, "getId", Long.TYPE, null);
/*      */ 
/* 2209 */         if (pkcode != 22) break;
/* 2210 */         code.invokespecial().setMethod(Long.class, "<init>", Void.TYPE, new Class[] { Long.TYPE }); break;
/*      */       case 23:
/* 2214 */         code.anew().setType(Short.class);
/* 2215 */         code.dup();
/*      */       case 7:
/* 2218 */         code.aload().setLocal(oid);
/* 2219 */         code.checkcast().setType(ShortId.class);
/* 2220 */         code.invokevirtual().setMethod(ShortId.class, "getId", Short.TYPE, null);
/*      */ 
/* 2222 */         if (pkcode != 23) break;
/* 2223 */         code.invokespecial().setMethod(Short.class, "<init>", Void.TYPE, new Class[] { Short.TYPE }); break;
/*      */       case 14:
/* 2227 */         code.aload().setLocal(oid);
/* 2228 */         code.checkcast().setType(DateId.class);
/* 2229 */         code.invokevirtual().setMethod(DateId.class, "getId", Date.class, null);
/*      */ 
/* 2231 */         if (pktype == Date.class)
/*      */           break;
/* 2233 */         code.checkcast().setType(pktype); break;
/*      */       case 9:
/* 2237 */         code.aload().setLocal(oid);
/* 2238 */         code.checkcast().setType(StringId.class);
/* 2239 */         code.invokevirtual().setMethod(StringId.class, "getId", String.class, null);
/*      */ 
/* 2241 */         break;
/*      */       case 24:
/* 2243 */         code.aload().setLocal(oid);
/* 2244 */         code.checkcast().setType(BigDecimalId.class);
/* 2245 */         code.invokevirtual().setMethod(BigDecimalId.class, "getId", BigDecimal.class, null);
/*      */ 
/* 2247 */         break;
/*      */       case 25:
/* 2249 */         code.aload().setLocal(oid);
/* 2250 */         code.checkcast().setType(BigIntegerId.class);
/* 2251 */         code.invokevirtual().setMethod(BigIntegerId.class, "getId", BigInteger.class, null);
/*      */ 
/* 2253 */         break;
/*      */       case 8:
/*      */       case 10:
/*      */       case 11:
/*      */       case 12:
/*      */       case 13:
/*      */       case 15:
/*      */       case 16:
/*      */       default:
/* 2255 */         code.aload().setLocal(oid);
/* 2256 */         code.checkcast().setType(ObjectId.class);
/* 2257 */         code.invokevirtual().setMethod(ObjectId.class, "getId", Object.class, null); break;
/*      */       }
/*      */     }
/* 2260 */     else if (pkmeta.getObjectIdType() != null) {
/* 2261 */       code.aload().setLocal(oid);
/* 2262 */       if (pkcode == 8) {
/* 2263 */         code.checkcast().setType(ObjectId.class);
/* 2264 */         code.invokevirtual().setMethod(ObjectId.class, "getId", Object.class, null);
/*      */       }
/*      */ 
/* 2267 */       code.checkcast().setType(pktype);
/*      */     } else {
/* 2269 */       code.aload().setLocal(oid);
/* 2270 */     }JumpInstruction go2 = code.go2();
/*      */     Instruction def;
/* 2275 */     switch (pkcode) {
/*      */     case 0:
/* 2277 */       def = code.constant().setValue(false);
/* 2278 */       break;
/*      */     case 1:
/* 2280 */       def = code.constant().setValue((short)0);
/* 2281 */       break;
/*      */     case 2:
/* 2283 */       def = code.constant().setValue('\000');
/* 2284 */       break;
/*      */     case 3:
/* 2286 */       def = code.constant().setValue(0.0D);
/* 2287 */       break;
/*      */     case 4:
/* 2289 */       def = code.constant().setValue(0.0F);
/* 2290 */       break;
/*      */     case 5:
/* 2292 */       def = code.constant().setValue(0);
/* 2293 */       break;
/*      */     case 6:
/* 2295 */       def = code.constant().setValue(0L);
/* 2296 */       break;
/*      */     case 7:
/* 2298 */       def = code.constant().setValue((short)0);
/* 2299 */       break;
/*      */     default:
/* 2301 */       def = code.constant().setNull();
/*      */     }
/* 2303 */     ifnull1.setTarget(def);
/* 2304 */     ifnull2.setTarget(def);
/* 2305 */     go2.setTarget(code.nop());
/*      */   }
/*      */ 
/*      */   private void addCopyKeyFieldsFromObjectIdMethod(boolean fieldManager)
/*      */     throws NoSuchMethodException
/*      */   {
/* 2317 */     String[] args = { fieldManager ? new String[] { OIDFCTYPE.getName(), Object.class.getName() } : Object.class.getName() };
/*      */ 
/* 2320 */     BCMethod method = this._pc.declareMethod("pcCopyKeyFieldsFromObjectId", Void.TYPE.getName(), args);
/*      */ 
/* 2322 */     Code code = method.getCode(true);
/*      */ 
/* 2325 */     if ((this._meta.getPCSuperclass() != null) && (!getCreateSubclass())) {
/* 2326 */       loadManagedInstance(code, false);
/* 2327 */       for (int i = 0; i < args.length; i++)
/* 2328 */         code.aload().setParam(i);
/* 2329 */       code.invokespecial().setMethod(getType(this._meta.getPCSuperclassMetaData()).getName(), "pcCopyKeyFieldsFromObjectId", Void.TYPE.getName(), args);
/*      */     }
/*      */ 
/* 2334 */     if (fieldManager)
/* 2335 */       code.aload().setParam(1);
/*      */     else {
/* 2337 */       code.aload().setParam(0);
/*      */     }
/* 2339 */     if ((!this._meta.isOpenJPAIdentity()) && (this._meta.isObjectIdTypeShared()))
/*      */     {
/* 2341 */       code.checkcast().setType(ObjectId.class);
/* 2342 */       code.invokevirtual().setMethod(ObjectId.class, "getId", Object.class, null);
/*      */     }
/*      */ 
/* 2347 */     int id = code.getNextLocalsIndex();
/* 2348 */     Class oidType = this._meta.getObjectIdType();
/* 2349 */     code.checkcast().setType(oidType);
/* 2350 */     code.astore().setLocal(id);
/*      */ 
/* 2355 */     FieldMetaData[] fmds = getCreateSubclass() ? this._meta.getFields() : this._meta.getDeclaredFields();
/*      */ 
/* 2362 */     for (int i = 0; i < fmds.length; i++)
/* 2363 */       if (fmds[i].isPrimaryKey())
/*      */       {
/* 2366 */         String name = fmds[i].getName();
/* 2367 */         Class type = fmds[i].getObjectIdFieldType();
/* 2368 */         if ((!fieldManager) && (fmds[i].getDeclaredTypeCode() == 15))
/*      */         {
/* 2371 */           loadManagedInstance(code, false);
/* 2372 */           code.getfield().setField("pcStateManager", SMTYPE);
/* 2373 */           JumpInstruction ifins = code.ifnonnull();
/* 2374 */           code.vreturn();
/*      */ 
/* 2376 */           ifins.setTarget(loadManagedInstance(code, false));
/* 2377 */           code.dup();
/* 2378 */           code.getfield().setField("pcStateManager", SMTYPE);
/* 2379 */           code.aload().setLocal(id);
/* 2380 */           code.constant().setValue(i);
/* 2381 */           code.getstatic().setField("pcInheritedFieldCount", Integer.TYPE);
/* 2382 */           code.iadd();
/* 2383 */           code.invokeinterface().setMethod(StateManager.class, "getPCPrimaryKey", Object.class, new Class[] { Object.class, Integer.TYPE });
/*      */ 
/* 2386 */           code.checkcast().setType(fmds[i].getDeclaredType());
/*      */         } else {
/* 2388 */           Class unwrapped = fmds[i].getDeclaredTypeCode() == 15 ? type : unwrapSingleFieldIdentity(fmds[i]);
/*      */ 
/* 2390 */           if (fieldManager) {
/* 2391 */             code.aload().setParam(0);
/* 2392 */             code.constant().setValue(i);
/* 2393 */             code.getstatic().setField("pcInheritedFieldCount", Integer.TYPE);
/* 2394 */             code.iadd();
/*      */           } else {
/* 2396 */             loadManagedInstance(code, false);
/*      */           }
/* 2398 */           if (unwrapped != type) {
/* 2399 */             code.anew().setType(type);
/* 2400 */             code.dup();
/*      */           }
/* 2402 */           code.aload().setLocal(id);
/* 2403 */           if (this._meta.isOpenJPAIdentity()) {
/* 2404 */             if (oidType == ObjectId.class) {
/* 2405 */               code.invokevirtual().setMethod(oidType, "getId", Object.class, null);
/*      */ 
/* 2407 */               if ((!fieldManager) && (type != Object.class))
/* 2408 */                 code.checkcast().setType(fmds[i].getDeclaredType());
/* 2409 */             } else if (oidType == DateId.class) {
/* 2410 */               code.invokevirtual().setMethod(oidType, "getId", Date.class, null);
/*      */ 
/* 2412 */               if ((!fieldManager) && (type != Date.class))
/* 2413 */                 code.checkcast().setType(fmds[i].getDeclaredType());
/*      */             } else {
/* 2415 */               code.invokevirtual().setMethod(oidType, "getId", unwrapped, null);
/*      */ 
/* 2417 */               if (unwrapped != type)
/* 2418 */                 code.invokespecial().setMethod(type, "<init>", Void.TYPE, new Class[] { unwrapped });
/*      */             }
/*      */           }
/* 2421 */           else if (isFieldAccess(fmds[i])) {
/* 2422 */             Field field = Reflection.findField(oidType, name, true);
/* 2423 */             if (Modifier.isPublic(field.getModifiers())) {
/* 2424 */               code.getfield().setField(field);
/*      */             }
/*      */             else {
/* 2427 */               code.classconstant().setClass(oidType);
/* 2428 */               code.constant().setValue(name);
/* 2429 */               code.constant().setValue(true);
/* 2430 */               code.invokestatic().setMethod(Reflection.class, "findField", Field.class, new Class[] { Class.class, String.class, Boolean.TYPE });
/*      */ 
/* 2433 */               code.invokestatic().setMethod(getReflectionGetterMethod(type, Field.class));
/*      */ 
/* 2435 */               if ((!type.isPrimitive()) && (type != Object.class))
/* 2436 */                 code.checkcast().setType(type);
/*      */             }
/*      */           } else {
/* 2439 */             Method getter = Reflection.findGetter(oidType, name, true);
/* 2440 */             if (Modifier.isPublic(getter.getModifiers())) {
/* 2441 */               code.invokevirtual().setMethod(getter);
/*      */             }
/*      */             else {
/* 2444 */               code.classconstant().setClass(oidType);
/* 2445 */               code.constant().setValue(name);
/* 2446 */               code.constant().setValue(true);
/* 2447 */               code.invokestatic().setMethod(Reflection.class, "findGetter", Method.class, new Class[] { Class.class, String.class, Boolean.TYPE });
/*      */ 
/* 2450 */               code.invokestatic().setMethod(getReflectionGetterMethod(type, Method.class));
/*      */ 
/* 2452 */               if ((!type.isPrimitive()) && (type != Object.class)) {
/* 2453 */                 code.checkcast().setType(type);
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/* 2458 */         if (fieldManager)
/* 2459 */           code.invokeinterface().setMethod(getFieldConsumerMethod(type));
/*      */         else
/* 2461 */           addSetManagedValueCode(code, fmds[i]);
/*      */       }
/* 2463 */     code.vreturn();
/*      */ 
/* 2465 */     code.calculateMaxStack();
/* 2466 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private Boolean usesClassStringIdConstructor()
/*      */   {
/* 2474 */     if (this._meta.getIdentityType() != 2) {
/* 2475 */       return Boolean.FALSE;
/*      */     }
/* 2477 */     if (this._meta.isOpenJPAIdentity()) {
/* 2478 */       if (this._meta.getObjectIdType() == ObjectId.class)
/* 2479 */         return null;
/* 2480 */       return Boolean.TRUE;
/*      */     }
/*      */ 
/* 2483 */     Class oidType = this._meta.getObjectIdType();
/*      */     try {
/* 2485 */       oidType.getConstructor(new Class[] { Class.class, String.class });
/* 2486 */       return Boolean.TRUE;
/*      */     }
/*      */     catch (Throwable t) {
/*      */       try {
/* 2490 */         oidType.getConstructor(new Class[] { String.class });
/* 2491 */         return Boolean.FALSE; } catch (Throwable t) {
/*      */       }
/*      */     }
/* 2494 */     return null;
/*      */   }
/*      */ 
/*      */   private Class unwrapSingleFieldIdentity(FieldMetaData fmd)
/*      */   {
/* 2502 */     if (!fmd.getDefiningMetaData().isOpenJPAIdentity()) {
/* 2503 */       return fmd.getDeclaredType();
/*      */     }
/* 2505 */     switch (fmd.getDeclaredTypeCode()) {
/*      */     case 17:
/* 2507 */       return Byte.TYPE;
/*      */     case 18:
/* 2509 */       return Character.TYPE;
/*      */     case 19:
/* 2511 */       return Double.TYPE;
/*      */     case 20:
/* 2513 */       return Float.TYPE;
/*      */     case 21:
/* 2515 */       return Integer.TYPE;
/*      */     case 23:
/* 2517 */       return Short.TYPE;
/*      */     case 22:
/* 2519 */       return Long.TYPE;
/*      */     }
/* 2521 */     return fmd.getDeclaredType();
/*      */   }
/*      */ 
/*      */   private Method getReflectionGetterMethod(Class type, Class argType)
/*      */     throws NoSuchMethodException
/*      */   {
/* 2532 */     String name = "get";
/* 2533 */     if (type.isPrimitive())
/* 2534 */       name = new StringBuilder().append(name).append(StringUtils.capitalize(type.getName())).toString();
/* 2535 */     return Reflection.class.getMethod(name, new Class[] { Object.class, argType });
/*      */   }
/*      */ 
/*      */   private Method getFieldSupplierMethod(Class type)
/*      */     throws NoSuchMethodException
/*      */   {
/* 2546 */     return getMethod(OIDFSTYPE, type, "fetch", true, false, false);
/*      */   }
/*      */ 
/*      */   private Method getFieldConsumerMethod(Class type)
/*      */     throws NoSuchMethodException
/*      */   {
/* 2556 */     return getMethod(OIDFCTYPE, type, "store", false, false, false);
/*      */   }
/*      */ 
/*      */   private void addNewObjectIdInstanceMethod(boolean obj)
/*      */     throws NoSuchMethodException
/*      */   {
/* 2567 */     Class[] args = obj ? new Class[] { Object.class } : null;
/* 2568 */     BCMethod method = this._pc.declareMethod("pcNewObjectIdInstance", Object.class, args);
/*      */ 
/* 2570 */     Code code = method.getCode(true);
/*      */ 
/* 2572 */     Boolean usesClsString = usesClassStringIdConstructor();
/* 2573 */     Class oidType = this._meta.getObjectIdType();
/* 2574 */     if ((obj) && (usesClsString == null))
/*      */     {
/* 2576 */       String msg = _loc.get("str-cons", oidType, this._meta.getDescribedType()).getMessage();
/*      */ 
/* 2578 */       code.anew().setType(IllegalArgumentException.class);
/* 2579 */       code.dup();
/* 2580 */       code.constant().setValue(msg);
/* 2581 */       code.invokespecial().setMethod(IllegalArgumentException.class, "<init>", Void.TYPE, new Class[] { String.class });
/*      */ 
/* 2583 */       code.athrow();
/* 2584 */       code.vreturn();
/*      */ 
/* 2586 */       code.calculateMaxStack();
/* 2587 */       code.calculateMaxLocals();
/* 2588 */       return;
/*      */     }
/*      */ 
/* 2591 */     if ((!this._meta.isOpenJPAIdentity()) && (this._meta.isObjectIdTypeShared()))
/*      */     {
/* 2593 */       code.anew().setType(ObjectId.class);
/* 2594 */       code.dup();
/* 2595 */       if ((this._meta.isEmbeddedOnly()) || (this._meta.hasAbstractPKField() == true)) {
/* 2596 */         code.aload().setThis();
/* 2597 */         code.invokevirtual().setMethod("pcGetIDOwningClass", Class.class, null);
/*      */       }
/*      */       else {
/* 2600 */         code.classconstant().setClass(getType(this._meta));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2605 */     code.anew().setType(oidType);
/* 2606 */     code.dup();
/* 2607 */     if ((this._meta.isOpenJPAIdentity()) || ((obj) && (usesClsString == Boolean.TRUE)))
/*      */     {
/* 2609 */       if ((this._meta.isEmbeddedOnly()) || (this._meta.hasAbstractPKField() == true)) {
/* 2610 */         code.aload().setThis();
/* 2611 */         code.invokevirtual().setMethod("pcGetIDOwningClass", Class.class, null);
/*      */       }
/*      */       else {
/* 2614 */         code.classconstant().setClass(getType(this._meta));
/*      */       }
/*      */     }
/* 2617 */     if (obj) {
/* 2618 */       code.aload().setParam(0);
/* 2619 */       code.checkcast().setType(String.class);
/* 2620 */       if (usesClsString == Boolean.TRUE)
/* 2621 */         args = new Class[] { Class.class, String.class };
/* 2622 */       else if (usesClsString == Boolean.FALSE)
/* 2623 */         args = new Class[] { String.class };
/* 2624 */     } else if (this._meta.isOpenJPAIdentity())
/*      */     {
/* 2626 */       loadManagedInstance(code, false);
/* 2627 */       FieldMetaData pk = this._meta.getPrimaryKeyFields()[0];
/* 2628 */       addGetManagedValueCode(code, pk);
/* 2629 */       if (pk.getDeclaredTypeCode() == 15)
/* 2630 */         addExtractObjectIdFieldValueCode(code, pk);
/* 2631 */       if (this._meta.getObjectIdType() == ObjectId.class)
/* 2632 */         args = new Class[] { Class.class, Object.class };
/* 2633 */       else if (this._meta.getObjectIdType() == Date.class)
/* 2634 */         args = new Class[] { Class.class, Date.class };
/*      */       else {
/* 2636 */         args = new Class[] { Class.class, pk.getObjectIdFieldType() };
/*      */       }
/*      */     }
/* 2639 */     code.invokespecial().setMethod(oidType, "<init>", Void.TYPE, args);
/* 2640 */     if ((!this._meta.isOpenJPAIdentity()) && (this._meta.isObjectIdTypeShared())) {
/* 2641 */       code.invokespecial().setMethod(ObjectId.class, "<init>", Void.TYPE, new Class[] { Class.class, Object.class });
/*      */     }
/* 2643 */     code.areturn();
/*      */ 
/* 2645 */     code.calculateMaxStack();
/* 2646 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private Method getStateManagerMethod(Class type, String prefix, boolean get, boolean curValue)
/*      */     throws NoSuchMethodException
/*      */   {
/* 2668 */     return getMethod(SMTYPE, type, prefix, get, true, curValue);
/*      */   }
/*      */ 
/*      */   private Method getMethod(Class owner, Class type, String prefix, boolean get, boolean haspc, boolean curValue)
/*      */     throws NoSuchMethodException
/*      */   {
/* 2690 */     String typeName = type.getName();
/* 2691 */     if (type.isPrimitive()) {
/* 2692 */       typeName = new StringBuilder().append(typeName.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(typeName.substring(1)).toString();
/*      */     }
/* 2694 */     else if (type.equals(String.class)) {
/* 2695 */       typeName = "String";
/*      */     } else {
/* 2697 */       typeName = "Object";
/* 2698 */       type = Object.class;
/*      */     }
/*      */ 
/* 2704 */     List plist = new ArrayList(4);
/* 2705 */     if (haspc)
/* 2706 */       plist.add(PCTYPE);
/* 2707 */     plist.add(Integer.TYPE);
/* 2708 */     if ((!get) || (curValue))
/* 2709 */       plist.add(type);
/* 2710 */     if ((!get) && (curValue)) {
/* 2711 */       plist.add(type);
/* 2712 */       plist.add(Integer.TYPE);
/*      */     }
/*      */ 
/* 2716 */     String name = new StringBuilder().append(prefix).append(typeName).append("Field").toString();
/* 2717 */     Class[] params = (Class[])plist.toArray(new Class[plist.size()]);
/*      */     try
/*      */     {
/* 2720 */       return (Method)AccessController.doPrivileged(J2DoPrivHelper.getDeclaredMethodAction(owner, name, params));
/*      */     } catch (PrivilegedActionException pae) {
/* 2722 */       throw ((NoSuchMethodException)pae.getException());
/*      */     }
/*      */   }
/*      */ 
/*      */   private Instruction throwException(Code code, Class type)
/*      */   {
/* 2731 */     Instruction ins = code.anew().setType(type);
/* 2732 */     code.dup();
/* 2733 */     code.invokespecial().setMethod(type, "<init>", Void.TYPE, null);
/* 2734 */     code.athrow();
/* 2735 */     return ins;
/*      */   }
/*      */ 
/*      */   private void enhanceClass()
/*      */   {
/* 2745 */     this._pc.declareInterface(PCTYPE);
/*      */ 
/* 2748 */     addGetEnhancementContractVersionMethod();
/*      */ 
/* 2751 */     BCMethod method = this._pc.getDeclaredMethod("<init>", (String[])null);
/*      */ 
/* 2754 */     if (method == null) {
/* 2755 */       String name = this._pc.getName();
/* 2756 */       if (!this._defCons) {
/* 2757 */         throw new UserException(_loc.get("enhance-defaultconst", name));
/*      */       }
/* 2759 */       method = this._pc.addDefaultConstructor();
/*      */       String access;
/*      */       String access;
/* 2761 */       if (this._meta.isDetachable())
/*      */       {
/* 2764 */         method.makePublic();
/* 2765 */         access = "public";
/*      */       }
/*      */       else
/*      */       {
/*      */         String access;
/* 2766 */         if (this._pc.isFinal()) {
/* 2767 */           method.makePrivate();
/* 2768 */           access = "private";
/*      */         } else {
/* 2770 */           method.makeProtected();
/* 2771 */           access = "protected";
/*      */         }
/*      */       }
/* 2773 */       if ((!this._meta.getDescribedType().isInterface()) && (!getCreateSubclass()) && (this._log.isWarnEnabled()))
/*      */       {
/* 2775 */         this._log.warn(_loc.get("enhance-adddefaultconst", name, access));
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void addFields()
/*      */   {
/* 2793 */     this._pc.declareField("pcInheritedFieldCount", Integer.TYPE).setStatic(true);
/* 2794 */     this._pc.declareField("pcFieldNames", [Ljava.lang.String.class).setStatic(true);
/* 2795 */     this._pc.declareField("pcFieldTypes", [Ljava.lang.Class.class).setStatic(true);
/* 2796 */     this._pc.declareField("pcFieldFlags", [B.class).setStatic(true);
/* 2797 */     this._pc.declareField("pcPCSuperclass", Class.class).setStatic(true);
/* 2798 */     if ((this._addVersionInitFlag) && (this._meta.getVersionField() != null))
/*      */     {
/* 2800 */       BCField field = this._pc.declareField("pcVersionInit", Boolean.TYPE);
/* 2801 */       field.makeProtected();
/* 2802 */       field.setTransient(true);
/*      */     }
/* 2804 */     if ((this._meta.getPCSuperclass() == null) || (getCreateSubclass())) {
/* 2805 */       BCField field = this._pc.declareField("pcStateManager", SMTYPE);
/* 2806 */       field.makeProtected();
/* 2807 */       field.setTransient(true);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void addStaticInitializer()
/*      */   {
/* 2817 */     Code code = getOrCreateClassInitCode(true);
/* 2818 */     if (this._meta.getPCSuperclass() != null) {
/* 2819 */       if (getCreateSubclass()) {
/* 2820 */         code.constant().setValue(0);
/* 2821 */         code.putstatic().setField("pcInheritedFieldCount", Integer.TYPE);
/*      */       }
/*      */       else {
/* 2824 */         code.invokestatic().setMethod(getType(this._meta.getPCSuperclassMetaData()).getName(), "pcGetManagedFieldCount", Integer.TYPE.getName(), null);
/*      */ 
/* 2827 */         code.putstatic().setField("pcInheritedFieldCount", Integer.TYPE);
/*      */       }
/*      */ 
/* 2833 */       code.classconstant().setClass(this._meta.getPCSuperclassMetaData().getDescribedType());
/*      */ 
/* 2835 */       code.putstatic().setField("pcPCSuperclass", Class.class);
/*      */     }
/*      */ 
/* 2839 */     FieldMetaData[] fmds = this._meta.getDeclaredFields();
/* 2840 */     code.constant().setValue(fmds.length);
/* 2841 */     code.anewarray().setType(String.class);
/* 2842 */     for (int i = 0; i < fmds.length; i++) {
/* 2843 */       code.dup();
/* 2844 */       code.constant().setValue(i);
/* 2845 */       code.constant().setValue(fmds[i].getName());
/* 2846 */       code.aastore();
/*      */     }
/* 2848 */     code.putstatic().setField("pcFieldNames", [Ljava.lang.String.class);
/*      */ 
/* 2851 */     code.constant().setValue(fmds.length);
/* 2852 */     code.anewarray().setType(Class.class);
/* 2853 */     for (int i = 0; i < fmds.length; i++) {
/* 2854 */       code.dup();
/* 2855 */       code.constant().setValue(i);
/* 2856 */       code.classconstant().setClass(fmds[i].getDeclaredType());
/* 2857 */       code.aastore();
/*      */     }
/* 2859 */     code.putstatic().setField("pcFieldTypes", [Ljava.lang.Class.class);
/*      */ 
/* 2862 */     code.constant().setValue(fmds.length);
/* 2863 */     code.newarray().setType(Byte.TYPE);
/* 2864 */     for (int i = 0; i < fmds.length; i++) {
/* 2865 */       code.dup();
/* 2866 */       code.constant().setValue(i);
/* 2867 */       code.constant().setValue((short)getFieldFlag(fmds[i]));
/* 2868 */       code.bastore();
/*      */     }
/* 2870 */     code.putstatic().setField("pcFieldFlags", [B.class);
/*      */ 
/* 2875 */     code.classconstant().setClass(this._meta.getDescribedType());
/* 2876 */     code.getstatic().setField("pcFieldNames", [Ljava.lang.String.class);
/* 2877 */     code.getstatic().setField("pcFieldTypes", [Ljava.lang.Class.class);
/* 2878 */     code.getstatic().setField("pcFieldFlags", [B.class);
/* 2879 */     code.getstatic().setField("pcPCSuperclass", Class.class);
/*      */ 
/* 2881 */     if ((this._meta.isMapped()) || (this._meta.isAbstract()))
/* 2882 */       code.constant().setValue(this._meta.getTypeAlias());
/*      */     else {
/* 2884 */       code.constant().setNull();
/*      */     }
/* 2886 */     if (this._pc.isAbstract()) {
/* 2887 */       code.constant().setNull();
/*      */     } else {
/* 2889 */       code.anew().setType(this._pc);
/* 2890 */       code.dup();
/* 2891 */       code.invokespecial().setMethod("<init>", Void.TYPE, null);
/*      */     }
/*      */ 
/* 2894 */     code.invokestatic().setMethod(HELPERTYPE, "register", Void.TYPE, new Class[] { Class.class, [Ljava.lang.String.class, [Ljava.lang.Class.class, [B.class, Class.class, String.class, PCTYPE });
/*      */ 
/* 2898 */     code.vreturn();
/* 2899 */     code.calculateMaxStack();
/*      */   }
/*      */ 
/*      */   private static byte getFieldFlag(FieldMetaData fmd)
/*      */   {
/* 2906 */     if (fmd.getManagement() == 0) {
/* 2907 */       return -1;
/*      */     }
/* 2909 */     byte flags = 0;
/* 2910 */     if ((fmd.getDeclaredType().isPrimitive()) || (Serializable.class.isAssignableFrom(fmd.getDeclaredType())))
/*      */     {
/* 2912 */       flags = 16;
/*      */     }
/* 2914 */     if (fmd.getManagement() == 1)
/* 2915 */       flags = (byte)(flags | 0x4);
/* 2916 */     else if ((!fmd.isPrimaryKey()) && (!fmd.isInDefaultFetchGroup())) {
/* 2917 */       flags = (byte)(flags | 0x5);
/*      */     }
/*      */     else {
/* 2920 */       flags = (byte)(flags | 0xA);
/*      */     }
/* 2922 */     return flags;
/*      */   }
/*      */ 
/*      */   private void addSerializationCode()
/*      */   {
/* 2933 */     if ((externalizeDetached()) || (!Serializable.class.isAssignableFrom(this._meta.getDescribedType())))
/*      */     {
/* 2935 */       return;
/*      */     }
/* 2937 */     if (getCreateSubclass())
/*      */     {
/* 2941 */       if (!Externalizable.class.isAssignableFrom(this._meta.getDescribedType()))
/*      */       {
/* 2943 */         addSubclassSerializationCode();
/* 2944 */       }return;
/*      */     }
/*      */ 
/* 2951 */     BCField field = this._pc.getDeclaredField("serialVersionUID");
/* 2952 */     if (field == null) {
/* 2953 */       Long uid = null;
/*      */       try {
/* 2955 */         uid = Long.valueOf(ObjectStreamClass.lookup(this._meta.getDescribedType()).getSerialVersionUID());
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 2959 */         if (this._log.isTraceEnabled())
/* 2960 */           this._log.warn(_loc.get("enhance-uid-access", this._meta), t);
/*      */         else {
/* 2962 */           this._log.warn(_loc.get("enhance-uid-access", this._meta));
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2968 */       if (uid != null) {
/* 2969 */         field = this._pc.declareField("serialVersionUID", Long.TYPE);
/* 2970 */         field.makePrivate();
/* 2971 */         field.setStatic(true);
/* 2972 */         field.setFinal(true);
/*      */ 
/* 2974 */         Code code = getOrCreateClassInitCode(false);
/* 2975 */         code.beforeFirst();
/* 2976 */         code.constant().setValue(uid.longValue());
/* 2977 */         code.putstatic().setField(field);
/*      */ 
/* 2979 */         code.calculateMaxStack();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2984 */     BCMethod write = this._pc.getDeclaredMethod("writeObject", new Class[] { ObjectOutputStream.class });
/*      */ 
/* 2986 */     boolean full = write == null;
/* 2987 */     if (full)
/*      */     {
/* 2989 */       write = this._pc.declareMethod("writeObject", Void.TYPE, new Class[] { ObjectOutputStream.class });
/*      */ 
/* 2991 */       write.getExceptions(true).addException(IOException.class);
/* 2992 */       write.makePrivate();
/*      */     }
/* 2994 */     modifyWriteObjectMethod(write, full);
/*      */ 
/* 2997 */     BCMethod read = this._pc.getDeclaredMethod("readObject", new Class[] { ObjectInputStream.class });
/*      */ 
/* 2999 */     full = read == null;
/* 3000 */     if (full)
/*      */     {
/* 3002 */       read = this._pc.declareMethod("readObject", Void.TYPE, new Class[] { ObjectInputStream.class });
/*      */ 
/* 3004 */       read.getExceptions(true).addException(IOException.class);
/* 3005 */       read.getExceptions(true).addException(ClassNotFoundException.class);
/*      */ 
/* 3007 */       read.makePrivate();
/*      */     }
/* 3009 */     modifyReadObjectMethod(read, full);
/*      */   }
/*      */ 
/*      */   private void addSubclassSerializationCode()
/*      */   {
/* 3018 */     BCMethod method = this._pc.declareMethod("writeReplace", Object.class, null);
/* 3019 */     method.getExceptions(true).addException(ObjectStreamException.class);
/* 3020 */     Code code = method.getCode(true);
/*      */ 
/* 3023 */     code.anew().setType(this._managedType);
/* 3024 */     code.dup();
/* 3025 */     code.dup();
/* 3026 */     code.invokespecial().setMethod(this._managedType.getType(), "<init>", Void.TYPE, null);
/*      */ 
/* 3031 */     FieldMetaData[] fmds = this._meta.getFields();
/* 3032 */     for (int i = 0; i < fmds.length; i++) {
/* 3033 */       if (!fmds[i].isTransient())
/*      */       {
/* 3036 */         code.dup();
/* 3037 */         code.aload().setThis();
/* 3038 */         getfield(code, this._managedType, fmds[i].getName());
/* 3039 */         putfield(code, this._managedType, fmds[i].getName(), fmds[i].getDeclaredType());
/*      */       }
/*      */     }
/*      */ 
/* 3043 */     code.areturn().setType(Object.class);
/*      */ 
/* 3045 */     code.calculateMaxLocals();
/* 3046 */     code.calculateMaxStack();
/*      */   }
/*      */ 
/*      */   private boolean externalizeDetached()
/*      */   {
/* 3054 */     return ("`syn".equals(this._meta.getDetachedState())) && (Serializable.class.isAssignableFrom(this._meta.getDescribedType())) && (!this._repos.getConfiguration().getDetachStateInstance().isDetachedStateTransient());
/*      */   }
/*      */ 
/*      */   private void modifyWriteObjectMethod(BCMethod method, boolean full)
/*      */   {
/* 3066 */     Code code = method.getCode(true);
/* 3067 */     code.beforeFirst();
/*      */ 
/* 3070 */     loadManagedInstance(code, false);
/* 3071 */     code.invokevirtual().setMethod("pcSerializing", Boolean.TYPE, null);
/*      */ 
/* 3073 */     int clear = code.getNextLocalsIndex();
/* 3074 */     code.istore().setLocal(clear);
/*      */ 
/* 3076 */     if (full)
/*      */     {
/* 3078 */       code.aload().setParam(0);
/* 3079 */       code.invokevirtual().setMethod(ObjectOutputStream.class, "defaultWriteObject", Void.TYPE, null);
/*      */ 
/* 3081 */       code.vreturn();
/*      */     }
/*      */ 
/* 3084 */     Instruction tmplate = ((Code)AccessController.doPrivileged(J2DoPrivHelper.newCodeAction())).vreturn();
/*      */ 
/* 3087 */     code.beforeFirst();
/* 3088 */     while (code.searchForward(tmplate)) {
/* 3089 */       Instruction ret = code.previous();
/*      */ 
/* 3091 */       code.iload().setLocal(clear);
/* 3092 */       JumpInstruction toret = code.ifeq();
/* 3093 */       loadManagedInstance(code, false);
/* 3094 */       code.constant().setNull();
/* 3095 */       code.invokevirtual().setMethod("pcSetDetachedState", Void.TYPE, new Class[] { Object.class });
/*      */ 
/* 3097 */       toret.setTarget(ret);
/* 3098 */       code.next();
/*      */     }
/* 3100 */     code.calculateMaxStack();
/* 3101 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void modifyReadObjectMethod(BCMethod method, boolean full)
/*      */   {
/* 3109 */     Code code = method.getCode(true);
/* 3110 */     code.beforeFirst();
/*      */ 
/* 3114 */     if ("`syn".equals(this._meta.getDetachedState())) {
/* 3115 */       loadManagedInstance(code, false);
/* 3116 */       code.getstatic().setField(PersistenceCapable.class, "DESERIALIZED", Object.class);
/*      */ 
/* 3118 */       code.invokevirtual().setMethod("pcSetDetachedState", Void.TYPE, new Class[] { Object.class });
/*      */     }
/*      */ 
/* 3122 */     if (full)
/*      */     {
/* 3124 */       code.aload().setParam(0);
/* 3125 */       code.invokevirtual().setMethod(ObjectInputStream.class, "defaultReadObject", Void.TYPE, null);
/*      */ 
/* 3127 */       code.vreturn();
/*      */     }
/*      */ 
/* 3130 */     code.calculateMaxStack();
/* 3131 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addIsDetachedMethod()
/*      */     throws NoSuchMethodException
/*      */   {
/* 3142 */     BCMethod method = this._pc.declareMethod("pcIsDetached", Boolean.class, null);
/*      */ 
/* 3144 */     method.makePublic();
/* 3145 */     Code code = method.getCode(true);
/* 3146 */     boolean needsDefinitiveMethod = writeIsDetachedMethod(code);
/* 3147 */     code.calculateMaxStack();
/* 3148 */     code.calculateMaxLocals();
/* 3149 */     if (!needsDefinitiveMethod) {
/* 3150 */       return;
/*      */     }
/*      */ 
/* 3156 */     method = this._pc.declareMethod("pcisDetachedStateDefinitive", Boolean.TYPE, null);
/*      */ 
/* 3158 */     method.makePrivate();
/* 3159 */     code = method.getCode(true);
/* 3160 */     code.constant().setValue(false);
/* 3161 */     code.ireturn();
/* 3162 */     code.calculateMaxStack();
/* 3163 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private boolean writeIsDetachedMethod(Code code)
/*      */     throws NoSuchMethodException
/*      */   {
/* 3177 */     if (!this._meta.isDetachable()) {
/* 3178 */       code.getstatic().setField(Boolean.class, "FALSE", Boolean.class);
/* 3179 */       code.areturn();
/* 3180 */       return false;
/*      */     }
/*      */ 
/* 3185 */     loadManagedInstance(code, false);
/* 3186 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 3187 */     JumpInstruction ifins = code.ifnull();
/* 3188 */     loadManagedInstance(code, false);
/* 3189 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 3190 */     code.invokeinterface().setMethod(SMTYPE, "isDetached", Boolean.TYPE, null);
/*      */ 
/* 3192 */     JumpInstruction iffalse = code.ifeq();
/* 3193 */     code.getstatic().setField(Boolean.class, "TRUE", Boolean.class);
/* 3194 */     code.areturn();
/* 3195 */     iffalse.setTarget(code.getstatic().setField(Boolean.class, "FALSE", Boolean.class));
/*      */ 
/* 3197 */     code.areturn();
/*      */ 
/* 3203 */     Boolean state = this._meta.usesDetachedState();
/* 3204 */     JumpInstruction notdeser = null;
/*      */ 
/* 3206 */     if (state != Boolean.FALSE) {
/* 3207 */       ifins.setTarget(loadManagedInstance(code, false));
/* 3208 */       code.invokevirtual().setMethod("pcGetDetachedState", Object.class, null);
/*      */ 
/* 3210 */       ifins = code.ifnull();
/* 3211 */       loadManagedInstance(code, false);
/* 3212 */       code.invokevirtual().setMethod("pcGetDetachedState", Object.class, null);
/*      */ 
/* 3214 */       code.getstatic().setField(PersistenceCapable.class, "DESERIALIZED", Object.class);
/*      */ 
/* 3216 */       notdeser = code.ifacmpeq();
/* 3217 */       code.getstatic().setField(Boolean.class, "TRUE", Boolean.class);
/* 3218 */       code.areturn();
/*      */ 
/* 3220 */       if (state == Boolean.TRUE)
/*      */       {
/* 3223 */         Instruction target = code.getstatic().setField(Boolean.class, "FALSE", Boolean.class);
/*      */ 
/* 3225 */         ifins.setTarget(target);
/* 3226 */         notdeser.setTarget(target);
/* 3227 */         code.areturn();
/* 3228 */         return false;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3233 */     Instruction target = code.nop();
/* 3234 */     ifins.setTarget(target);
/* 3235 */     if (notdeser != null) {
/* 3236 */       notdeser.setTarget(target);
/*      */     }
/*      */ 
/* 3243 */     FieldMetaData version = this._meta.getVersionField();
/* 3244 */     if ((state != Boolean.TRUE) && (version != null))
/*      */     {
/* 3247 */       loadManagedInstance(code, false);
/* 3248 */       addGetManagedValueCode(code, version);
/* 3249 */       ifins = ifDefaultValue(code, version);
/* 3250 */       code.getstatic().setField(Boolean.class, "TRUE", Boolean.class);
/* 3251 */       code.areturn();
/* 3252 */       if (!this._addVersionInitFlag)
/*      */       {
/* 3254 */         ifins.setTarget(code.getstatic().setField(Boolean.class, "FALSE", Boolean.class));
/*      */       }
/*      */       else {
/* 3257 */         ifins.setTarget(code.nop());
/*      */ 
/* 3261 */         loadManagedInstance(code, false);
/* 3262 */         getfield(code, null, "pcVersionInit");
/* 3263 */         ifins = code.ifeq();
/* 3264 */         code.getstatic().setField(Boolean.class, "TRUE", Boolean.class);
/* 3265 */         code.areturn();
/* 3266 */         ifins.setTarget(code.nop());
/* 3267 */         code.constant().setNull();
/*      */       }
/* 3269 */       code.areturn();
/* 3270 */       return false;
/*      */     }
/*      */ 
/* 3274 */     ifins = null;
/* 3275 */     JumpInstruction ifins2 = null;
/* 3276 */     boolean hasAutoAssignedPK = false;
/* 3277 */     if ((state != Boolean.TRUE) && (this._meta.getIdentityType() == 2))
/*      */     {
/* 3282 */       FieldMetaData[] pks = this._meta.getPrimaryKeyFields();
/* 3283 */       for (int i = 0; i < pks.length; i++) {
/* 3284 */         if (pks[i].getValueStrategy() != 0)
/*      */         {
/* 3287 */           target = loadManagedInstance(code, false);
/* 3288 */           if (ifins != null)
/* 3289 */             ifins.setTarget(target);
/* 3290 */           if (ifins2 != null)
/* 3291 */             ifins2.setTarget(target);
/* 3292 */           ifins2 = null;
/*      */ 
/* 3294 */           addGetManagedValueCode(code, pks[i]);
/* 3295 */           ifins = ifDefaultValue(code, pks[i]);
/* 3296 */           if (pks[i].getDeclaredTypeCode() == 9) {
/* 3297 */             code.constant().setValue("");
/* 3298 */             loadManagedInstance(code, false);
/* 3299 */             addGetManagedValueCode(code, pks[i]);
/* 3300 */             code.invokevirtual().setMethod(String.class, "equals", Boolean.TYPE, new Class[] { Object.class });
/*      */ 
/* 3302 */             ifins2 = code.ifne();
/*      */           }
/* 3304 */           code.getstatic().setField(Boolean.class, "TRUE", Boolean.class);
/*      */ 
/* 3306 */           code.areturn();
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 3311 */     target = code.nop();
/* 3312 */     if (ifins != null)
/* 3313 */       ifins.setTarget(target);
/* 3314 */     if (ifins2 != null) {
/* 3315 */       ifins2.setTarget(target);
/*      */     }
/*      */ 
/* 3319 */     if (hasAutoAssignedPK) {
/* 3320 */       code.getstatic().setField(Boolean.class, "FALSE", Boolean.class);
/* 3321 */       code.areturn();
/* 3322 */       return false;
/*      */     }
/*      */ 
/* 3328 */     code.aload().setThis();
/* 3329 */     code.invokespecial().setMethod("pcisDetachedStateDefinitive", Boolean.TYPE, null);
/*      */ 
/* 3331 */     ifins = code.ifne();
/* 3332 */     code.constant().setNull();
/* 3333 */     code.areturn();
/* 3334 */     ifins.setTarget(code.nop());
/*      */ 
/* 3339 */     if ((state == null) && ((!"`syn".equals(this._meta.getDetachedState())) || (!Serializable.class.isAssignableFrom(this._meta.getDescribedType())) || (!this._repos.getConfiguration().getDetachStateInstance().isDetachedStateTransient())))
/*      */     {
/* 3345 */       code.getstatic().setField(Boolean.class, "FALSE", Boolean.class);
/* 3346 */       code.areturn();
/* 3347 */       return true;
/*      */     }
/*      */ 
/* 3353 */     if (state == null)
/*      */     {
/* 3356 */       loadManagedInstance(code, false);
/* 3357 */       code.invokevirtual().setMethod("pcGetDetachedState", Object.class, null);
/*      */ 
/* 3359 */       ifins = code.ifnonnull();
/* 3360 */       code.getstatic().setField(Boolean.class, "FALSE", Boolean.class);
/* 3361 */       code.areturn();
/* 3362 */       ifins.setTarget(code.nop());
/*      */     }
/*      */ 
/* 3366 */     code.constant().setNull();
/* 3367 */     code.areturn();
/* 3368 */     return true;
/*      */   }
/*      */ 
/*      */   private static JumpInstruction ifDefaultValue(Code code, FieldMetaData fmd)
/*      */   {
/* 3377 */     switch (fmd.getDeclaredTypeCode()) {
/*      */     case 0:
/*      */     case 1:
/*      */     case 2:
/*      */     case 5:
/*      */     case 7:
/* 3383 */       return code.ifeq();
/*      */     case 3:
/* 3385 */       code.constant().setValue(0.0D);
/* 3386 */       code.dcmpl();
/* 3387 */       return code.ifeq();
/*      */     case 4:
/* 3389 */       code.constant().setValue(0.0F);
/* 3390 */       code.fcmpl();
/* 3391 */       return code.ifeq();
/*      */     case 6:
/* 3393 */       code.constant().setValue(0L);
/* 3394 */       code.lcmp();
/* 3395 */       return code.ifeq();
/*      */     }
/* 3397 */     return code.ifnull();
/*      */   }
/*      */ 
/*      */   private Code getOrCreateClassInitCode(boolean replaceLast)
/*      */   {
/* 3406 */     BCMethod clinit = this._pc.getDeclaredMethod("<clinit>");
/*      */ 
/* 3408 */     if (clinit != null) {
/* 3409 */       Code code = clinit.getCode(true);
/* 3410 */       if (replaceLast) {
/* 3411 */         Code template = (Code)AccessController.doPrivileged(J2DoPrivHelper.newCodeAction());
/* 3412 */         code.searchForward(template.vreturn());
/* 3413 */         code.previous();
/* 3414 */         code.set(template.nop());
/* 3415 */         code.next();
/*      */       }
/* 3417 */       return code;
/*      */     }
/*      */ 
/* 3421 */     clinit = this._pc.declareMethod("<clinit>", Void.TYPE, null);
/* 3422 */     clinit.makePackage();
/* 3423 */     clinit.setStatic(true);
/* 3424 */     clinit.setFinal(true);
/*      */ 
/* 3426 */     Code code = clinit.getCode(true);
/* 3427 */     if (!replaceLast) {
/* 3428 */       code.vreturn();
/* 3429 */       code.previous();
/*      */     }
/* 3431 */     return code;
/*      */   }
/*      */ 
/*      */   private void addCloningCode()
/*      */   {
/* 3444 */     if ((this._meta.getPCSuperclass() != null) && (!getCreateSubclass())) {
/* 3445 */       return;
/*      */     }
/*      */ 
/* 3448 */     BCMethod clone = this._pc.getDeclaredMethod("clone", (String[])null);
/*      */ 
/* 3450 */     String superName = this._managedType.getSuperclassName();
/* 3451 */     Code code = null;
/* 3452 */     if (clone == null)
/*      */     {
/* 3455 */       boolean isCloneable = Cloneable.class.isAssignableFrom(this._managedType.getType());
/*      */ 
/* 3457 */       boolean extendsObject = superName.equals(Object.class.getName());
/*      */ 
/* 3459 */       if ((!isCloneable) || ((!extendsObject) && (!getCreateSubclass()))) {
/* 3460 */         return;
/*      */       }
/* 3462 */       if ((!getCreateSubclass()) && 
/* 3463 */         (this._log.isTraceEnabled())) {
/* 3464 */         this._log.trace(_loc.get("enhance-cloneable", this._managedType.getName()));
/*      */       }
/*      */ 
/* 3469 */       clone = this._pc.declareMethod("clone", Object.class, null);
/* 3470 */       if (!setVisibilityToSuperMethod(clone))
/* 3471 */         clone.makeProtected();
/* 3472 */       clone.getExceptions(true).addException(CloneNotSupportedException.class);
/*      */ 
/* 3474 */       code = clone.getCode(true);
/*      */ 
/* 3477 */       loadManagedInstance(code, false);
/* 3478 */       code.invokespecial().setMethod(superName, "clone", Object.class.getName(), null);
/*      */ 
/* 3480 */       code.areturn();
/*      */     }
/*      */     else {
/* 3483 */       code = clone.getCode(false);
/* 3484 */       if (code == null) {
/* 3485 */         return;
/*      */       }
/*      */     }
/*      */ 
/* 3489 */     Instruction template = ((Code)AccessController.doPrivileged(J2DoPrivHelper.newCodeAction())).invokespecial().setMethod(superName, "clone", Object.class.getName(), null);
/*      */ 
/* 3494 */     code.beforeFirst();
/* 3495 */     if (code.searchForward(template))
/*      */     {
/* 3497 */       code.dup();
/* 3498 */       code.checkcast().setType(this._pc);
/* 3499 */       code.constant().setNull();
/* 3500 */       code.putfield().setField("pcStateManager", SMTYPE);
/*      */ 
/* 3503 */       code.calculateMaxStack();
/* 3504 */       code.calculateMaxLocals();
/*      */     }
/*      */   }
/*      */ 
/*      */   public AuxiliaryEnhancer[] getAuxiliaryEnhancers()
/*      */   {
/* 3512 */     return _auxEnhancers;
/*      */   }
/*      */ 
/*      */   private void runAuxiliaryEnhancers()
/*      */   {
/* 3519 */     for (int i = 0; i < _auxEnhancers.length; i++)
/* 3520 */       _auxEnhancers[i].run(this._pc, this._meta);
/*      */   }
/*      */ 
/*      */   private boolean skipEnhance(BCMethod method)
/*      */   {
/* 3531 */     if ("<init>".equals(method.getName())) {
/* 3532 */       return true;
/*      */     }
/* 3534 */     for (int i = 0; i < _auxEnhancers.length; i++) {
/* 3535 */       if (_auxEnhancers[i].skipEnhance(method))
/* 3536 */         return true;
/*      */     }
/* 3538 */     return false;
/*      */   }
/*      */ 
/*      */   private void addAccessors()
/*      */     throws NoSuchMethodException
/*      */   {
/* 3548 */     FieldMetaData[] fmds = getCreateSubclass() ? this._meta.getFields() : this._meta.getDeclaredFields();
/*      */ 
/* 3550 */     for (int i = 0; i < fmds.length; i++)
/* 3551 */       if (getCreateSubclass()) {
/* 3552 */         if ((!getRedefine()) && (isPropertyAccess(fmds[i]))) {
/* 3553 */           addSubclassSetMethod(fmds[i]);
/* 3554 */           addSubclassGetMethod(fmds[i]);
/*      */         }
/*      */       } else {
/* 3557 */         addGetMethod(i, fmds[i]);
/* 3558 */         addSetMethod(i, fmds[i]);
/*      */       }
/*      */   }
/*      */ 
/*      */   private void addSubclassSetMethod(FieldMetaData fmd)
/*      */     throws NoSuchMethodException
/*      */   {
/* 3570 */     Class propType = fmd.getDeclaredType();
/* 3571 */     String setterName = getSetterName(fmd);
/* 3572 */     BCMethod setter = this._pc.declareMethod(setterName, Void.TYPE, new Class[] { propType });
/*      */ 
/* 3574 */     setVisibilityToSuperMethod(setter);
/* 3575 */     Code code = setter.getCode(true);
/*      */ 
/* 3578 */     if (!getRedefine())
/*      */     {
/* 3580 */       code.aload().setThis();
/* 3581 */       addGetManagedValueCode(code, fmd);
/* 3582 */       int val = code.getNextLocalsIndex();
/* 3583 */       code.xstore().setLocal(val).setType(fmd.getDeclaredType());
/* 3584 */       addNotifyMutation(code, fmd, val, 0);
/*      */     }
/*      */ 
/* 3590 */     code.aload().setThis();
/* 3591 */     code.xload().setParam(0).setType(propType);
/* 3592 */     code.invokespecial().setMethod(this._managedType.getType(), setterName, Void.TYPE, new Class[] { propType });
/*      */ 
/* 3595 */     code.vreturn();
/* 3596 */     code.calculateMaxLocals();
/* 3597 */     code.calculateMaxStack();
/*      */   }
/*      */ 
/*      */   private boolean setVisibilityToSuperMethod(BCMethod method) {
/* 3601 */     BCMethod[] methods = this._managedType.getMethods(method.getName(), method.getParamTypes());
/*      */ 
/* 3603 */     if (methods.length == 0) {
/* 3604 */       throw new UserException(_loc.get("no-accessor", this._managedType.getName(), method.getName()));
/*      */     }
/* 3606 */     BCMethod superMeth = methods[0];
/* 3607 */     if (superMeth.isPrivate()) {
/* 3608 */       method.makePrivate();
/* 3609 */       return true;
/* 3610 */     }if (superMeth.isPackage()) {
/* 3611 */       method.makePackage();
/* 3612 */       return true;
/* 3613 */     }if (superMeth.isProtected()) {
/* 3614 */       method.makeProtected();
/* 3615 */       return true;
/* 3616 */     }if (superMeth.isPublic()) {
/* 3617 */       method.makePublic();
/* 3618 */       return true;
/*      */     }
/* 3620 */     return false;
/*      */   }
/*      */ 
/*      */   private void addSubclassGetMethod(FieldMetaData fmd)
/*      */   {
/* 3628 */     String methName = new StringBuilder().append("get").append(StringUtils.capitalize(fmd.getName())).toString();
/* 3629 */     if (this._managedType.getMethods(methName, new Class[0]).length == 0)
/* 3630 */       methName = new StringBuilder().append("is").append(StringUtils.capitalize(fmd.getName())).toString();
/* 3631 */     BCMethod getter = this._pc.declareMethod(methName, fmd.getDeclaredType(), null);
/*      */ 
/* 3633 */     setVisibilityToSuperMethod(getter);
/* 3634 */     getter.makePublic();
/* 3635 */     Code code = getter.getCode(true);
/*      */ 
/* 3640 */     if (!getRedefine()) {
/* 3641 */       addNotifyAccess(code, fmd);
/*      */     }
/* 3643 */     code.aload().setThis();
/* 3644 */     code.invokespecial().setMethod(this._managedType.getType(), methName, fmd.getDeclaredType(), null);
/*      */ 
/* 3646 */     code.xreturn().setType(fmd.getDeclaredType());
/* 3647 */     code.calculateMaxLocals();
/* 3648 */     code.calculateMaxStack();
/*      */   }
/*      */ 
/*      */   private void addGetMethod(int index, FieldMetaData fmd)
/*      */     throws NoSuchMethodException
/*      */   {
/* 3662 */     BCMethod method = createGetMethod(fmd);
/* 3663 */     Code code = method.getCode(true);
/*      */ 
/* 3666 */     byte fieldFlag = getFieldFlag(fmd);
/* 3667 */     if (((fieldFlag & 0x1) == 0) && ((fieldFlag & 0x2) == 0))
/*      */     {
/* 3669 */       loadManagedInstance(code, true, fmd);
/* 3670 */       addGetManagedValueCode(code, fmd);
/* 3671 */       code.xreturn().setType(fmd.getDeclaredType());
/*      */ 
/* 3673 */       code.calculateMaxStack();
/* 3674 */       code.calculateMaxLocals();
/* 3675 */       return;
/*      */     }
/*      */ 
/* 3679 */     loadManagedInstance(code, true, fmd);
/* 3680 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 3681 */     JumpInstruction ifins = code.ifnonnull();
/* 3682 */     loadManagedInstance(code, true, fmd);
/* 3683 */     addGetManagedValueCode(code, fmd);
/* 3684 */     code.xreturn().setType(fmd.getDeclaredType());
/*      */ 
/* 3687 */     int fieldLocal = code.getNextLocalsIndex();
/* 3688 */     ifins.setTarget(code.getstatic().setField("pcInheritedFieldCount", Integer.TYPE));
/* 3689 */     code.constant().setValue(index);
/* 3690 */     code.iadd();
/* 3691 */     code.istore().setLocal(fieldLocal);
/*      */ 
/* 3695 */     loadManagedInstance(code, true, fmd);
/* 3696 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 3697 */     code.iload().setLocal(fieldLocal);
/* 3698 */     code.invokeinterface().setMethod(SMTYPE, "accessingField", Void.TYPE, new Class[] { Integer.TYPE });
/*      */ 
/* 3700 */     loadManagedInstance(code, true, fmd);
/* 3701 */     addGetManagedValueCode(code, fmd);
/* 3702 */     code.xreturn().setType(fmd.getDeclaredType());
/*      */ 
/* 3704 */     code.calculateMaxStack();
/* 3705 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addSetMethod(int index, FieldMetaData fmd)
/*      */     throws NoSuchMethodException
/*      */   {
/* 3719 */     BCMethod method = createSetMethod(fmd);
/* 3720 */     Code code = method.getCode(true);
/*      */ 
/* 3723 */     int firstParamOffset = getAccessorParameterOffset(fmd);
/*      */ 
/* 3726 */     loadManagedInstance(code, true, fmd);
/* 3727 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 3728 */     JumpInstruction ifins = code.ifnonnull();
/* 3729 */     loadManagedInstance(code, true, fmd);
/* 3730 */     code.xload().setParam(firstParamOffset);
/* 3731 */     addSetManagedValueCode(code, fmd);
/* 3732 */     if ((fmd.isVersion() == true) && (this._addVersionInitFlag))
/*      */     {
/* 3734 */       loadManagedInstance(code, true);
/* 3735 */       code.constant().setValue(1);
/*      */ 
/* 3737 */       putfield(code, null, "pcVersionInit", Boolean.TYPE);
/*      */     }
/* 3739 */     code.vreturn();
/*      */ 
/* 3743 */     ifins.setTarget(loadManagedInstance(code, true, fmd));
/* 3744 */     code.getfield().setField("pcStateManager", SMTYPE);
/* 3745 */     loadManagedInstance(code, true, fmd);
/* 3746 */     code.getstatic().setField("pcInheritedFieldCount", Integer.TYPE);
/* 3747 */     code.constant().setValue(index);
/* 3748 */     code.iadd();
/* 3749 */     loadManagedInstance(code, true, fmd);
/* 3750 */     addGetManagedValueCode(code, fmd);
/* 3751 */     code.xload().setParam(firstParamOffset);
/* 3752 */     code.constant().setValue(0);
/* 3753 */     code.invokeinterface().setMethod(getStateManagerMethod(fmd.getDeclaredType(), "setting", false, true));
/*      */ 
/* 3755 */     code.vreturn();
/*      */ 
/* 3757 */     code.calculateMaxStack();
/* 3758 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addAttachDetachCode()
/*      */     throws NoSuchMethodException
/*      */   {
/* 3768 */     boolean parentDetachable = false;
/* 3769 */     for (ClassMetaData parent = this._meta.getPCSuperclassMetaData(); 
/* 3770 */       parent != null; parent = parent.getPCSuperclassMetaData()) {
/* 3771 */       if (parent.isDetachable()) {
/* 3772 */         parentDetachable = true;
/* 3773 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3779 */     if ((this._meta.getPCSuperclass() == null) || (getCreateSubclass()) || (parentDetachable != this._meta.isDetachable()))
/*      */     {
/* 3781 */       addIsDetachedMethod();
/* 3782 */       addDetachedStateMethods(this._meta.usesDetachedState() != Boolean.FALSE);
/*      */     }
/*      */ 
/* 3789 */     if (externalizeDetached())
/*      */       try {
/* 3791 */         addDetachExternalize(parentDetachable, this._meta.usesDetachedState() != Boolean.FALSE);
/*      */       }
/*      */       catch (NoSuchMethodException nsme) {
/* 3794 */         throw new GeneralException(nsme);
/*      */       }
/*      */   }
/*      */ 
/*      */   private void addDetachedStateMethods(boolean impl)
/*      */   {
/* 3805 */     Field detachField = this._meta.getDetachedStateField();
/* 3806 */     String name = null;
/* 3807 */     String declarer = null;
/* 3808 */     if ((impl) && (detachField == null)) {
/* 3809 */       name = "pcDetachedState";
/* 3810 */       declarer = this._pc.getName();
/* 3811 */       BCField field = this._pc.declareField(name, Object.class);
/* 3812 */       field.makePrivate();
/* 3813 */       field.setTransient(true);
/* 3814 */     } else if (impl) {
/* 3815 */       name = detachField.getName();
/* 3816 */       declarer = detachField.getDeclaringClass().getName();
/*      */     }
/*      */ 
/* 3820 */     BCMethod method = this._pc.declareMethod("pcGetDetachedState", Object.class, null);
/*      */ 
/* 3822 */     method.setStatic(false);
/* 3823 */     method.makePublic();
/* 3824 */     int access = method.getAccessFlags();
/*      */ 
/* 3826 */     Code code = method.getCode(true);
/* 3827 */     if (impl)
/*      */     {
/* 3829 */       loadManagedInstance(code, false);
/* 3830 */       getfield(code, this._managedType.getProject().loadClass(declarer), name);
/*      */     }
/*      */     else {
/* 3833 */       code.constant().setNull();
/* 3834 */     }code.areturn();
/* 3835 */     code.calculateMaxLocals();
/* 3836 */     code.calculateMaxStack();
/*      */ 
/* 3839 */     method = this._pc.declareMethod("pcSetDetachedState", Void.TYPE, new Class[] { Object.class });
/*      */ 
/* 3841 */     method.setAccessFlags(access);
/* 3842 */     code = method.getCode(true);
/* 3843 */     if (impl)
/*      */     {
/* 3845 */       loadManagedInstance(code, false);
/* 3846 */       code.aload().setParam(0);
/* 3847 */       putfield(code, this._managedType.getProject().loadClass(declarer), name, Object.class);
/*      */     }
/*      */ 
/* 3850 */     code.vreturn();
/* 3851 */     code.calculateMaxStack();
/* 3852 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void getfield(Code code, BCClass declarer, String attrName)
/*      */   {
/* 3864 */     if (declarer == null) {
/* 3865 */       declarer = this._managedType;
/*      */     }
/*      */ 
/* 3868 */     String fieldName = toBackingFieldName(attrName);
/*      */ 
/* 3871 */     BCField field = null;
/* 3872 */     for (BCClass bc = this._pc; bc != null; bc = bc.getSuperclassBC()) {
/* 3873 */       BCField[] fields = (BCField[])AccessController.doPrivileged(J2DoPrivHelper.getBCClassFieldsAction(bc, fieldName));
/*      */ 
/* 3875 */       for (int i = 0; i < fields.length; i++) {
/* 3876 */         field = fields[i];
/*      */ 
/* 3879 */         if (fields[i].getDeclarer() == declarer)
/*      */         {
/*      */           break label94;
/*      */         }
/*      */       }
/*      */     }
/* 3885 */     label94: if ((getCreateSubclass()) && (code.getMethod().getDeclarer() == this._pc) && ((field == null) || (!field.isPublic())))
/*      */     {
/* 3890 */       code.classconstant().setClass(declarer);
/* 3891 */       code.constant().setValue(fieldName);
/* 3892 */       code.constant().setValue(true);
/* 3893 */       code.invokestatic().setMethod(Reflection.class, "findField", Field.class, new Class[] { Class.class, String.class, Boolean.TYPE });
/*      */ 
/* 3896 */       Class type = this._meta.getField(attrName).getDeclaredType();
/*      */       try {
/* 3898 */         code.invokestatic().setMethod(getReflectionGetterMethod(type, Field.class));
/*      */       }
/*      */       catch (NoSuchMethodException e)
/*      */       {
/* 3902 */         throw new InternalException(e);
/*      */       }
/* 3904 */       if ((!type.isPrimitive()) && (type != Object.class))
/* 3905 */         code.checkcast().setType(type);
/*      */     } else {
/* 3907 */       code.getfield().setField(declarer.getName(), fieldName, field.getType().getName());
/*      */     }
/*      */   }
/*      */ 
/*      */   private void putfield(Code code, BCClass declarer, String attrName, Class fieldType)
/*      */   {
/* 3923 */     if (declarer == null) {
/* 3924 */       declarer = this._managedType;
/*      */     }
/* 3926 */     String fieldName = toBackingFieldName(attrName);
/*      */ 
/* 3928 */     if ((getRedefine()) || (getCreateSubclass()))
/*      */     {
/* 3930 */       code.classconstant().setClass(declarer);
/* 3931 */       code.constant().setValue(fieldName);
/* 3932 */       code.constant().setValue(true);
/* 3933 */       code.invokestatic().setMethod(Reflection.class, "findField", Field.class, new Class[] { Class.class, String.class, Boolean.TYPE });
/*      */ 
/* 3936 */       code.invokestatic().setMethod(Reflection.class, "set", Void.TYPE, new Class[] { Object.class, fieldType.isPrimitive() ? fieldType : Object.class, Field.class });
/*      */     }
/*      */     else
/*      */     {
/* 3943 */       code.putfield().setField(declarer.getName(), fieldName, fieldType.getName());
/*      */     }
/*      */   }
/*      */ 
/*      */   private String toBackingFieldName(String name)
/*      */   {
/* 3954 */     FieldMetaData fmd = this._meta == null ? null : this._meta.getField(name);
/* 3955 */     if ((this._meta != null) && (isPropertyAccess(fmd)) && (this._attrsToFields != null) && (this._attrsToFields.containsKey(name)))
/*      */     {
/* 3957 */       name = (String)this._attrsToFields.get(name);
/* 3958 */     }return name;
/*      */   }
/*      */ 
/*      */   private String fromBackingFieldName(String name)
/*      */   {
/* 3967 */     FieldMetaData fmd = this._meta == null ? null : this._meta.getField(name);
/* 3968 */     if ((this._meta != null) && (isPropertyAccess(fmd)) && (this._fieldsToAttrs != null) && (this._fieldsToAttrs.containsKey(name)))
/*      */     {
/* 3970 */       return (String)this._fieldsToAttrs.get(name);
/*      */     }
/* 3972 */     return name;
/*      */   }
/*      */ 
/*      */   private void addDetachExternalize(boolean parentDetachable, boolean detachedState)
/*      */     throws NoSuchMethodException
/*      */   {
/* 3984 */     BCMethod meth = this._pc.getDeclaredMethod("<init>", (String[])null);
/* 3985 */     if (!meth.isPublic()) {
/* 3986 */       if (this._log.isWarnEnabled()) {
/* 3987 */         this._log.warn(_loc.get("enhance-defcons-extern", this._meta.getDescribedType()));
/*      */       }
/* 3989 */       meth.makePublic();
/*      */     }
/*      */ 
/* 3992 */     if (!Externalizable.class.isAssignableFrom(this._meta.getDescribedType())) {
/* 3993 */       this._pc.declareInterface(Externalizable.class);
/*      */     }
/*      */ 
/* 3997 */     Class[] input = { ObjectInputStream.class };
/* 3998 */     Class[] output = { ObjectOutputStream.class };
/* 3999 */     if ((this._managedType.getDeclaredMethod("readObject", input) != null) || (this._managedType.getDeclaredMethod("writeObject", output) != null))
/*      */     {
/* 4001 */       throw new UserException(_loc.get("detach-custom-ser", this._meta));
/* 4002 */     }input[0] = ObjectInput.class;
/* 4003 */     output[0] = ObjectOutput.class;
/* 4004 */     if ((this._managedType.getDeclaredMethod("readExternal", input) != null) || (this._managedType.getDeclaredMethod("writeExternal", output) != null))
/*      */     {
/* 4006 */       throw new UserException(_loc.get("detach-custom-extern", this._meta));
/*      */     }
/*      */ 
/* 4009 */     BCField[] fields = this._managedType.getDeclaredFields();
/* 4010 */     Collection unmgd = new ArrayList(fields.length);
/* 4011 */     for (int i = 0; i < fields.length; i++) {
/* 4012 */       if ((!fields[i].isTransient()) && (!fields[i].isStatic()) && (!fields[i].isFinal()) && (!fields[i].getName().startsWith("pc")) && (this._meta.getDeclaredField(fields[i].getName()) == null))
/*      */       {
/* 4016 */         unmgd.add(fields[i]);
/*      */       }
/*      */     }
/* 4019 */     addReadExternal(parentDetachable, detachedState);
/* 4020 */     addReadUnmanaged(unmgd, parentDetachable);
/* 4021 */     addWriteExternal(parentDetachable, detachedState);
/* 4022 */     addWriteUnmanaged(unmgd, parentDetachable);
/*      */   }
/*      */ 
/*      */   private void addReadExternal(boolean parentDetachable, boolean detachedState)
/*      */     throws NoSuchMethodException
/*      */   {
/* 4032 */     Class[] inargs = { ObjectInput.class };
/* 4033 */     BCMethod meth = this._pc.declareMethod("readExternal", Void.TYPE, inargs);
/* 4034 */     Exceptions exceps = meth.getExceptions(true);
/* 4035 */     exceps.addException(IOException.class);
/* 4036 */     exceps.addException(ClassNotFoundException.class);
/* 4037 */     Code code = meth.getCode(true);
/*      */ 
/* 4040 */     Class sup = this._meta.getDescribedType().getSuperclass();
/* 4041 */     if ((!parentDetachable) && (Externalizable.class.isAssignableFrom(sup))) {
/* 4042 */       loadManagedInstance(code, false);
/* 4043 */       code.aload().setParam(0);
/* 4044 */       code.invokespecial().setMethod(sup, "readExternal", Void.TYPE, inargs);
/*      */     }
/*      */ 
/* 4049 */     loadManagedInstance(code, false);
/* 4050 */     code.aload().setParam(0);
/* 4051 */     code.invokevirtual().setMethod(getType(this._meta), "pcReadUnmanaged", Void.TYPE, inargs);
/*      */ 
/* 4054 */     if (detachedState)
/*      */     {
/* 4056 */       loadManagedInstance(code, false);
/* 4057 */       code.aload().setParam(0);
/* 4058 */       code.invokeinterface().setMethod(ObjectInput.class, "readObject", Object.class, null);
/*      */ 
/* 4060 */       code.invokevirtual().setMethod("pcSetDetachedState", Void.TYPE, new Class[] { Object.class });
/*      */ 
/* 4064 */       loadManagedInstance(code, false);
/* 4065 */       code.aload().setParam(0);
/* 4066 */       code.invokeinterface().setMethod(ObjectInput.class, "readObject", Object.class, null);
/*      */ 
/* 4068 */       code.checkcast().setType(StateManager.class);
/* 4069 */       code.invokevirtual().setMethod("pcReplaceStateManager", Void.TYPE, new Class[] { StateManager.class });
/*      */     }
/*      */ 
/* 4074 */     FieldMetaData[] fmds = this._meta.getFields();
/* 4075 */     for (int i = 0; i < fmds.length; i++) {
/* 4076 */       if (!fmds[i].isTransient()) {
/* 4077 */         readExternal(code, fmds[i].getName(), fmds[i].getDeclaredType(), fmds[i]);
/*      */       }
/*      */     }
/* 4080 */     code.vreturn();
/* 4081 */     code.calculateMaxStack();
/* 4082 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addReadUnmanaged(Collection unmgd, boolean parentDetachable)
/*      */     throws NoSuchMethodException
/*      */   {
/* 4091 */     Class[] inargs = { ObjectInput.class };
/* 4092 */     BCMethod meth = this._pc.declareMethod("pcReadUnmanaged", Void.TYPE, inargs);
/*      */ 
/* 4094 */     meth.makeProtected();
/* 4095 */     Exceptions exceps = meth.getExceptions(true);
/* 4096 */     exceps.addException(IOException.class);
/* 4097 */     exceps.addException(ClassNotFoundException.class);
/* 4098 */     Code code = meth.getCode(true);
/*      */ 
/* 4101 */     if (parentDetachable) {
/* 4102 */       loadManagedInstance(code, false);
/* 4103 */       code.aload().setParam(0);
/* 4104 */       code.invokespecial().setMethod(getType(this._meta.getPCSuperclassMetaData()), "pcReadUnmanaged", Void.TYPE, inargs);
/*      */     }
/*      */ 
/* 4111 */     for (Iterator itr = unmgd.iterator(); itr.hasNext(); ) {
/* 4112 */       BCField field = (BCField)itr.next();
/* 4113 */       readExternal(code, field.getName(), field.getType(), null);
/*      */     }
/* 4115 */     code.vreturn();
/* 4116 */     code.calculateMaxStack();
/* 4117 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void readExternal(Code code, String fieldName, Class type, FieldMetaData fmd)
/*      */     throws NoSuchMethodException
/*      */   {
/*      */     String methName;
/* 4128 */     if (type.isPrimitive()) {
/* 4129 */       String methName = type.getName();
/* 4130 */       methName = new StringBuilder().append(methName.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(methName.substring(1)).toString();
/*      */ 
/* 4132 */       methName = new StringBuilder().append("read").append(methName).toString();
/*      */     } else {
/* 4134 */       methName = "readObject";
/*      */     }
/*      */ 
/* 4137 */     loadManagedInstance(code, false);
/* 4138 */     code.aload().setParam(0);
/* 4139 */     Class ret = type.isPrimitive() ? type : Object.class;
/* 4140 */     code.invokeinterface().setMethod(ObjectInput.class, methName, ret, null);
/*      */ 
/* 4142 */     if ((!type.isPrimitive()) && (type != Object.class))
/* 4143 */       code.checkcast().setType(type);
/* 4144 */     if (fmd == null) {
/* 4145 */       putfield(code, null, fieldName, type);
/*      */     } else {
/* 4147 */       addSetManagedValueCode(code, fmd);
/* 4148 */       switch (fmd.getDeclaredTypeCode())
/*      */       {
/*      */       case 8:
/*      */       case 11:
/*      */       case 12:
/*      */       case 13:
/*      */       case 14:
/*      */       case 28:
/* 4157 */         loadManagedInstance(code, false);
/* 4158 */         code.getfield().setField("pcStateManager", SMTYPE);
/* 4159 */         IfInstruction ifins = code.ifnull();
/* 4160 */         loadManagedInstance(code, false);
/* 4161 */         code.getfield().setField("pcStateManager", SMTYPE);
/* 4162 */         code.constant().setValue(fmd.getIndex());
/* 4163 */         code.invokeinterface().setMethod(SMTYPE, "proxyDetachedDeserialized", Void.TYPE, new Class[] { Integer.TYPE });
/*      */ 
/* 4166 */         ifins.setTarget(code.nop());
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void addWriteExternal(boolean parentDetachable, boolean detachedState)
/*      */     throws NoSuchMethodException
/*      */   {
/* 4178 */     Class[] outargs = { ObjectOutput.class };
/* 4179 */     BCMethod meth = this._pc.declareMethod("writeExternal", Void.TYPE, outargs);
/* 4180 */     Exceptions exceps = meth.getExceptions(true);
/* 4181 */     exceps.addException(IOException.class);
/* 4182 */     Code code = meth.getCode(true);
/*      */ 
/* 4185 */     Class sup = getType(this._meta).getSuperclass();
/* 4186 */     if ((!parentDetachable) && (Externalizable.class.isAssignableFrom(sup))) {
/* 4187 */       loadManagedInstance(code, false);
/* 4188 */       code.aload().setParam(0);
/* 4189 */       code.invokespecial().setMethod(sup, "writeExternal", Void.TYPE, outargs);
/*      */     }
/*      */ 
/* 4194 */     loadManagedInstance(code, false);
/* 4195 */     code.aload().setParam(0);
/* 4196 */     code.invokevirtual().setMethod(getType(this._meta), "pcWriteUnmanaged", Void.TYPE, outargs);
/*      */ 
/* 4199 */     JumpInstruction go2 = null;
/* 4200 */     if (detachedState)
/*      */     {
/* 4204 */       loadManagedInstance(code, false);
/* 4205 */       code.getfield().setField("pcStateManager", SMTYPE);
/* 4206 */       IfInstruction ifnull = code.ifnull();
/* 4207 */       loadManagedInstance(code, false);
/* 4208 */       code.getfield().setField("pcStateManager", SMTYPE);
/* 4209 */       code.aload().setParam(0);
/* 4210 */       code.invokeinterface().setMethod(SMTYPE, "writeDetached", Boolean.TYPE, outargs);
/*      */ 
/* 4212 */       go2 = code.ifeq();
/* 4213 */       code.vreturn();
/*      */ 
/* 4217 */       Class[] objargs = { Object.class };
/* 4218 */       ifnull.setTarget(code.aload().setParam(0));
/* 4219 */       loadManagedInstance(code, false);
/* 4220 */       code.invokevirtual().setMethod("pcGetDetachedState", Object.class, null);
/*      */ 
/* 4222 */       code.invokeinterface().setMethod(ObjectOutput.class, "writeObject", Void.TYPE, objargs);
/*      */ 
/* 4225 */       code.aload().setParam(0);
/* 4226 */       code.constant().setValue((Object)null);
/* 4227 */       code.invokeinterface().setMethod(ObjectOutput.class, "writeObject", Void.TYPE, objargs);
/*      */     }
/*      */ 
/* 4230 */     if (go2 != null) {
/* 4231 */       go2.setTarget(code.nop());
/*      */     }
/*      */ 
/* 4234 */     FieldMetaData[] fmds = this._meta.getFields();
/* 4235 */     for (int i = 0; i < fmds.length; i++) {
/* 4236 */       if (!fmds[i].isTransient()) {
/* 4237 */         writeExternal(code, fmds[i].getName(), fmds[i].getDeclaredType(), fmds[i]);
/*      */       }
/*      */     }
/*      */ 
/* 4241 */     code.vreturn();
/* 4242 */     code.calculateMaxStack();
/* 4243 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void addWriteUnmanaged(Collection unmgd, boolean parentDetachable)
/*      */     throws NoSuchMethodException
/*      */   {
/* 4252 */     Class[] outargs = { ObjectOutput.class };
/* 4253 */     BCMethod meth = this._pc.declareMethod("pcWriteUnmanaged", Void.TYPE, outargs);
/*      */ 
/* 4255 */     meth.makeProtected();
/* 4256 */     Exceptions exceps = meth.getExceptions(true);
/* 4257 */     exceps.addException(IOException.class);
/* 4258 */     Code code = meth.getCode(true);
/*      */ 
/* 4261 */     if (parentDetachable) {
/* 4262 */       loadManagedInstance(code, false);
/* 4263 */       code.aload().setParam(0);
/* 4264 */       code.invokespecial().setMethod(getType(this._meta.getPCSuperclassMetaData()), "pcWriteUnmanaged", Void.TYPE, outargs);
/*      */     }
/*      */ 
/* 4271 */     for (Iterator itr = unmgd.iterator(); itr.hasNext(); ) {
/* 4272 */       BCField field = (BCField)itr.next();
/* 4273 */       writeExternal(code, field.getName(), field.getType(), null);
/*      */     }
/* 4275 */     code.vreturn();
/* 4276 */     code.calculateMaxStack();
/* 4277 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   private void writeExternal(Code code, String fieldName, Class type, FieldMetaData fmd)
/*      */     throws NoSuchMethodException
/*      */   {
/*      */     String methName;
/* 4288 */     if (type.isPrimitive()) {
/* 4289 */       String methName = type.getName();
/* 4290 */       methName = new StringBuilder().append(methName.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(methName.substring(1)).toString();
/*      */ 
/* 4292 */       methName = new StringBuilder().append("write").append(methName).toString();
/*      */     } else {
/* 4294 */       methName = "writeObject";
/*      */     }
/*      */ 
/* 4297 */     code.aload().setParam(0);
/* 4298 */     loadManagedInstance(code, false);
/* 4299 */     if (fmd == null)
/* 4300 */       getfield(code, null, fieldName);
/*      */     else
/* 4302 */       addGetManagedValueCode(code, fmd);
/* 4303 */     Class[] args = { type };
/* 4304 */     if ((type == Byte.TYPE) || (type == Character.TYPE) || (type == Short.TYPE))
/* 4305 */       args[0] = Integer.TYPE;
/* 4306 */     else if (!type.isPrimitive())
/* 4307 */       args[0] = Object.class;
/* 4308 */     code.invokeinterface().setMethod(ObjectOutput.class, methName, Void.TYPE, args);
/*      */   }
/*      */ 
/*      */   private void addGetManagedValueCode(Code code, FieldMetaData fmd)
/*      */     throws NoSuchMethodException
/*      */   {
/* 4315 */     addGetManagedValueCode(code, fmd, true);
/*      */   }
/*      */ 
/*      */   private void addGetManagedValueCode(Code code, FieldMetaData fmd, boolean fromSameClass)
/*      */     throws NoSuchMethodException
/*      */   {
/* 4339 */     if ((getRedefine()) || (isFieldAccess(fmd))) {
/* 4340 */       getfield(code, null, fmd.getName());
/* 4341 */     } else if (getCreateSubclass())
/*      */     {
/* 4346 */       if (fromSameClass) {
/* 4347 */         Method meth = (Method)fmd.getBackingMember();
/* 4348 */         code.invokespecial().setMethod(meth);
/*      */       } else {
/* 4350 */         getfield(code, null, fmd.getName());
/*      */       }
/*      */     }
/*      */     else {
/* 4354 */       Method meth = (Method)fmd.getBackingMember();
/* 4355 */       code.invokevirtual().setMethod(new StringBuilder().append("pc").append(meth.getName()).toString(), meth.getReturnType(), meth.getParameterTypes());
/*      */     }
/*      */   }
/*      */ 
/*      */   private void addSetManagedValueCode(Code code, FieldMetaData fmd)
/*      */     throws NoSuchMethodException
/*      */   {
/* 4376 */     if ((getRedefine()) || (isFieldAccess(fmd)))
/* 4377 */       putfield(code, null, fmd.getName(), fmd.getDeclaredType());
/* 4378 */     else if (getCreateSubclass())
/*      */     {
/* 4381 */       code.invokespecial().setMethod(this._managedType.getType(), getSetterName(fmd), Void.TYPE, new Class[] { fmd.getDeclaredType() });
/*      */     }
/*      */     else
/*      */     {
/* 4386 */       code.invokevirtual().setMethod(new StringBuilder().append("pc").append(getSetterName(fmd)).toString(), Void.TYPE, new Class[] { fmd.getDeclaredType() });
/*      */     }
/*      */   }
/*      */ 
/*      */   private Instruction loadManagedInstance(Code code, boolean forStatic, FieldMetaData fmd)
/*      */   {
/* 4401 */     if ((forStatic) && (isFieldAccess(fmd)))
/* 4402 */       return code.aload().setParam(0);
/* 4403 */     return code.aload().setThis();
/*      */   }
/*      */ 
/*      */   private Instruction loadManagedInstance(Code code, boolean forStatic)
/*      */   {
/* 4414 */     return loadManagedInstance(code, forStatic, null);
/*      */   }
/*      */ 
/*      */   private int getAccessorParameterOffset(FieldMetaData fmd) {
/* 4418 */     return isFieldAccess(fmd) ? 1 : 0;
/*      */   }
/*      */ 
/*      */   boolean isPropertyAccess(ClassMetaData meta)
/*      */   {
/* 4425 */     return (meta != null) && ((meta.isMixedAccess()) || (AccessCode.isProperty(meta.getAccessType())));
/*      */   }
/*      */ 
/*      */   boolean isPropertyAccess(FieldMetaData fmd)
/*      */   {
/* 4433 */     return (fmd != null) && (AccessCode.isProperty(fmd.getAccessType()));
/*      */   }
/*      */ 
/*      */   boolean isFieldAccess(FieldMetaData fmd)
/*      */   {
/* 4440 */     return (fmd != null) && (AccessCode.isField(fmd.getAccessType()));
/*      */   }
/*      */ 
/*      */   private BCMethod createGetMethod(FieldMetaData fmd)
/*      */   {
/* 4449 */     if (isFieldAccess(fmd))
/*      */     {
/* 4451 */       BCField field = this._pc.getDeclaredField(fmd.getName());
/* 4452 */       BCMethod getter = this._pc.declareMethod(new StringBuilder().append("pcGet").append(fmd.getName()).toString(), fmd.getDeclaredType().getName(), new String[] { this._pc.getName() });
/*      */ 
/* 4454 */       getter.setAccessFlags(field.getAccessFlags() & 0xFFFFFF7F & 0xFFFFFFBF);
/*      */ 
/* 4456 */       getter.setStatic(true);
/* 4457 */       getter.setFinal(true);
/* 4458 */       return getter;
/*      */     }
/*      */ 
/* 4464 */     Method meth = (Method)fmd.getBackingMember();
/* 4465 */     BCMethod getter = this._pc.getDeclaredMethod(meth.getName(), meth.getParameterTypes());
/*      */ 
/* 4467 */     BCMethod newgetter = this._pc.declareMethod(new StringBuilder().append("pc").append(meth.getName()).toString(), meth.getReturnType(), meth.getParameterTypes());
/*      */ 
/* 4469 */     newgetter.setAccessFlags(getter.getAccessFlags());
/* 4470 */     newgetter.makeProtected();
/* 4471 */     transferCodeAttributes(getter, newgetter);
/* 4472 */     return getter;
/*      */   }
/*      */ 
/*      */   private BCMethod createSetMethod(FieldMetaData fmd)
/*      */   {
/* 4481 */     if (isFieldAccess(fmd))
/*      */     {
/* 4483 */       BCField field = this._pc.getDeclaredField(fmd.getName());
/* 4484 */       BCMethod setter = this._pc.declareMethod(new StringBuilder().append("pcSet").append(fmd.getName()).toString(), Void.TYPE, new Class[] { getType(this._meta), fmd.getDeclaredType() });
/*      */ 
/* 4486 */       setter.setAccessFlags(field.getAccessFlags() & 0xFFFFFF7F & 0xFFFFFFBF);
/*      */ 
/* 4488 */       setter.setStatic(true);
/* 4489 */       setter.setFinal(true);
/* 4490 */       return setter;
/*      */     }
/*      */ 
/* 4496 */     BCMethod setter = this._pc.getDeclaredMethod(getSetterName(fmd), new Class[] { fmd.getDeclaredType() });
/*      */ 
/* 4498 */     BCMethod newsetter = this._pc.declareMethod(new StringBuilder().append("pc").append(setter.getName()).toString(), setter.getReturnName(), setter.getParamNames());
/*      */ 
/* 4500 */     newsetter.setAccessFlags(setter.getAccessFlags());
/* 4501 */     newsetter.makeProtected();
/* 4502 */     transferCodeAttributes(setter, newsetter);
/* 4503 */     return setter;
/*      */   }
/*      */ 
/*      */   private void addGetEnhancementContractVersionMethod()
/*      */   {
/* 4508 */     BCMethod method = this._pc.declareMethod("pcGetEnhancementContractVersion", Integer.TYPE, null);
/*      */ 
/* 4510 */     method.makePublic();
/* 4511 */     Code code = method.getCode(true);
/* 4512 */     code.constant().setValue(ENHANCER_VERSION);
/* 4513 */     code.ireturn();
/* 4514 */     code.calculateMaxStack();
/* 4515 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   public Class getType(ClassMetaData meta)
/*      */   {
/* 4523 */     if (meta.getInterfaceImpl() != null)
/* 4524 */       return meta.getInterfaceImpl();
/* 4525 */     return meta.getDescribedType();
/*      */   }
/*      */ 
/*      */   private static void transferCodeAttributes(BCMethod from, BCMethod to)
/*      */   {
/* 4532 */     Code code = from.getCode(false);
/* 4533 */     if (code != null) {
/* 4534 */       to.addAttribute(code);
/* 4535 */       from.removeCode();
/*      */     }
/*      */ 
/* 4538 */     Exceptions exceps = from.getExceptions(false);
/* 4539 */     if (exceps != null)
/* 4540 */       to.addAttribute(exceps);
/*      */   }
/*      */ 
/*      */   public static void main(String[] args)
/*      */   {
/* 4584 */     Options opts = new Options();
/* 4585 */     args = opts.setFromCmdLine(args);
/* 4586 */     if (!run(args, opts))
/* 4587 */       System.err.println(_loc.get("enhance-usage"));
/*      */   }
/*      */ 
/*      */   public static boolean run(String[] args, Options opts)
/*      */   {
/* 4595 */     return Configurations.runAgainstAllAnchors(opts, new Configurations.Runnable()
/*      */     {
/*      */       public boolean run(Options opts)
/*      */         throws IOException
/*      */       {
/* 4600 */         OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
/*      */         try
/*      */         {
/* 4603 */           return PCEnhancer.run(conf, this.val$args, opts);
/*      */         }
/*      */         finally
/*      */         {
/* 4607 */           conf.close();
/*      */         }
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public static boolean run(OpenJPAConfiguration conf, String[] args, Options opts)
/*      */     throws IOException
/*      */   {
/* 4620 */     Flags flags = new Flags();
/* 4621 */     flags.directory = Files.getFile(opts.removeProperty("directory", "d", null), null);
/* 4622 */     flags.addDefaultConstructor = opts.removeBooleanProperty("addDefaultConstructor", "adc", flags.addDefaultConstructor);
/*      */ 
/* 4624 */     flags.tmpClassLoader = opts.removeBooleanProperty("tmpClassLoader", "tcl", flags.tmpClassLoader);
/*      */ 
/* 4626 */     flags.enforcePropertyRestrictions = opts.removeBooleanProperty("enforcePropertyRestrictions", "epr", flags.enforcePropertyRestrictions);
/*      */ 
/* 4631 */     BytecodeWriter writer = (BytecodeWriter)opts.get(new StringBuilder().append(PCEnhancer.class.getName()).append("#bytecodeWriter").toString());
/*      */ 
/* 4634 */     Configurations.populateConfiguration(conf, opts);
/* 4635 */     return run(conf, args, flags, null, writer, null);
/*      */   }
/*      */ 
/*      */   public static boolean run(OpenJPAConfiguration conf, String[] args, Flags flags, MetaDataRepository repos, BytecodeWriter writer, ClassLoader loader)
/*      */     throws IOException
/*      */   {
/* 4646 */     if (loader == null) {
/* 4647 */       loader = conf.getClassResolverInstance().getClassLoader(PCEnhancer.class, null);
/*      */     }
/* 4649 */     if (flags.tmpClassLoader) {
/* 4650 */       loader = (ClassLoader)AccessController.doPrivileged(J2DoPrivHelper.newTemporaryClassLoaderAction(loader));
/*      */     }
/* 4652 */     if (repos == null) {
/* 4653 */       repos = conf.newMetaDataRepositoryInstance();
/* 4654 */       repos.setSourceMode(1);
/*      */     }
/*      */ 
/* 4657 */     Log log = conf.getLog("openjpa.Tool");
/*      */     Collection classes;
/* 4659 */     if ((args == null) || (args.length == 0)) {
/* 4660 */       log.info(_loc.get("running-all-classes"));
/* 4661 */       Collection classes = repos.getPersistentTypeNames(true, loader);
/* 4662 */       if (classes == null) {
/* 4663 */         log.warn(_loc.get("no-class-to-enhance"));
/* 4664 */         return false;
/*      */       }
/*      */     } else {
/* 4667 */       ClassArgParser cap = conf.getMetaDataRepositoryInstance().getMetaDataFactory().newClassArgParser();
/*      */ 
/* 4669 */       cap.setClassLoader(loader);
/* 4670 */       classes = new HashSet();
/* 4671 */       for (int i = 0; i < args.length; i++) {
/* 4672 */         classes.addAll(Arrays.asList(cap.parseTypes(args[i])));
/*      */       }
/*      */     }
/* 4675 */     Project project = new Project();
/*      */ 
/* 4679 */     for (Iterator itr = classes.iterator(); itr.hasNext(); ) {
/* 4680 */       Object o = itr.next();
/* 4681 */       if (log.isTraceEnabled())
/* 4682 */         log.trace(_loc.get("enhance-running", o));
/*      */       BCClass bc;
/*      */       BCClass bc;
/* 4684 */       if ((o instanceof String))
/* 4685 */         bc = project.loadClass((String)o, loader);
/*      */       else
/* 4687 */         bc = project.loadClass((Class)o);
/* 4688 */       PCEnhancer enhancer = new PCEnhancer(conf, bc, repos, loader);
/* 4689 */       if (writer != null)
/* 4690 */         enhancer.setBytecodeWriter(writer);
/* 4691 */       enhancer.setDirectory(flags.directory);
/* 4692 */       enhancer.setAddDefaultConstructor(flags.addDefaultConstructor);
/* 4693 */       int status = enhancer.run();
/* 4694 */       if (status == 0) {
/* 4695 */         if (log.isTraceEnabled())
/* 4696 */           log.trace(_loc.get("enhance-norun"));
/* 4697 */       } else if (status == 4) {
/* 4698 */         if (log.isTraceEnabled())
/* 4699 */           log.trace(_loc.get("enhance-interface"));
/* 4700 */       } else if (status == 2) {
/* 4701 */         if (log.isTraceEnabled())
/* 4702 */           log.trace(_loc.get("enhance-aware"));
/* 4703 */         enhancer.record();
/*      */       } else {
/* 4705 */         enhancer.record();
/* 4706 */       }project.clear();
/*      */     }
/* 4708 */     return true;
/*      */   }
/*      */ 
/*      */   private void addGetIDOwningClass()
/*      */     throws NoSuchMethodException
/*      */   {
/* 4733 */     BCMethod method = this._pc.declareMethod("pcGetIDOwningClass", Class.class, null);
/*      */ 
/* 4735 */     Code code = method.getCode(true);
/*      */ 
/* 4737 */     code.classconstant().setClass(getType(this._meta));
/* 4738 */     code.areturn();
/*      */ 
/* 4740 */     code.calculateMaxStack();
/* 4741 */     code.calculateMaxLocals();
/*      */   }
/*      */ 
/*      */   public static boolean checkEnhancementLevel(Class<?> cls, Log log)
/*      */   {
/* 4759 */     if ((cls == null) || (log == null)) {
/* 4760 */       return false;
/*      */     }
/* 4762 */     PersistenceCapable pc = PCRegistry.newInstance(cls, null, false);
/* 4763 */     if (pc == null) {
/* 4764 */       return false;
/*      */     }
/* 4766 */     if (pc.pcGetEnhancementContractVersion() < ENHANCER_VERSION) {
/* 4767 */       log.info(_loc.get("down-level-enhanced-entity", new Object[] { cls.getName(), Integer.valueOf(pc.pcGetEnhancementContractVersion()), Integer.valueOf(ENHANCER_VERSION) }));
/*      */ 
/* 4769 */       return true;
/*      */     }
/* 4771 */     return false;
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  154 */     Class[] classes = Services.getImplementorClasses(AuxiliaryEnhancer.class, (ClassLoader)AccessController.doPrivileged(J2DoPrivHelper.getClassLoaderAction(AuxiliaryEnhancer.class)));
/*      */ 
/*  156 */     List auxEnhancers = new ArrayList(classes.length);
/*  157 */     for (int i = 0; i < classes.length; i++)
/*      */       try {
/*  159 */         auxEnhancers.add(AccessController.doPrivileged(J2DoPrivHelper.newInstanceAction(classes[i])));
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*      */       }
/*  164 */     _auxEnhancers = (AuxiliaryEnhancer[])auxEnhancers.toArray(new AuxiliaryEnhancer[auxEnhancers.size()]);
/*      */ 
/*  167 */     int rev = 0;
/*  168 */     Properties revisionProps = new Properties();
/*      */     try {
/*  170 */       InputStream in = PCEnhancer.class.getResourceAsStream("/META-INF/org.apache.openjpa.revision.properties");
/*  171 */       if (in != null) {
/*      */         try {
/*  173 */           revisionProps.load(in);
/*      */         } finally {
/*  175 */           in.close();
/*      */         }
/*      */       }
/*  178 */       String prop = revisionProps.getProperty("openjpa.enhancer.revision");
/*  179 */       rev = SVNUtils.svnInfoToInteger(prop);
/*      */     } catch (Exception e) {
/*      */     }
/*  182 */     if (rev > 0) {
/*  183 */       ENHANCER_VERSION = rev;
/*      */     }
/*      */     else
/*      */     {
/*  187 */       ENHANCER_VERSION = 2;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static abstract interface AuxiliaryEnhancer
/*      */   {
/*      */     public abstract void run(BCClass paramBCClass, ClassMetaData paramClassMetaData);
/*      */ 
/*      */     public abstract boolean skipEnhance(BCMethod paramBCMethod);
/*      */   }
/*      */ 
/*      */   public static class Flags
/*      */   {
/* 4716 */     public File directory = null;
/* 4717 */     public boolean addDefaultConstructor = true;
/* 4718 */     public boolean tmpClassLoader = true;
/* 4719 */     public boolean enforcePropertyRestrictions = false;
/*      */   }
/*      */ }

/* Location:           C:\Users\srybak\dev\java\projects\GI\repository\5.12.0.0.x-acf\corelib\openjpa-2.1.1-CUSTOMIZED\
 * Qualified Name:     org.apache.openjpa.enhance.PCEnhancer
 * JD-Core Version:    0.6.2
 */