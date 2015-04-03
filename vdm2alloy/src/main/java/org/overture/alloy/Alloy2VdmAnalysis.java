/*
 * Copyright (C) 2012 Kenneth Lausdahl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */
package org.overture.alloy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import org.overture.alloy.ast.AlloyExp;
import org.overture.alloy.ast.AlloyLetExp;
import org.overture.alloy.ast.AlloyTypeBind;
import org.overture.alloy.ast.Fact;
import org.overture.alloy.ast.Fun;
import org.overture.alloy.ast.ModuleHeader;
import org.overture.alloy.ast.Part;
import org.overture.alloy.ast.Pred;
import org.overture.alloy.ast.Run;
import org.overture.alloy.ast.Sig;
import org.overture.alloy.ast.Sig.FieldType;
import org.overture.alloy.ast.Sig.FieldType.Prefix;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptorQuestionAnswer;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.*;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.VDMToken;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.ARecordPattern;
import org.overture.ast.patterns.ASetMultipleBind;
import org.overture.ast.patterns.ATuplePattern;
import org.overture.ast.patterns.PMultipleBind;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.statements.AExternalClause;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.ACharBasicType;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.AInMapMapType;
import org.overture.ast.types.AMapMapType;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.AQuoteType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.ATokenBasicType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SBasicType;
import org.overture.ast.types.SInvariantType;
import org.overture.ast.types.SMapType;
import org.overture.ast.types.SNumericBasicType;
import org.overture.ast.types.SSeqType;

public class Alloy2VdmAnalysis
		extends
		DepthFirstAnalysisAdaptorQuestionAnswer<Context, Alloy2VdmAnalysis.AlloyPart>
{
	private static final long serialVersionUID = 1L;
	final public List<Part> components = new Vector<Part>();

	public class AlloyPart
	{
		public String exp = "";
		public Queue<AlloyExp> predicates = new LinkedList<AlloyExp>();
		public Queue<AlloyExp> topLevel = new LinkedList<AlloyExp>();
		public Queue<AlloyTypeBind> typeBindings = new LinkedList<AlloyTypeBind>();

		public AlloyPart(String exp)
		{
			this.exp = exp;
		}

		public AlloyPart()
		{
			this.exp = "";
		}

		public void merge(AlloyPart p)
		{
			mergeReturns(this, p);
		}

		@Override
		public String toString()
		{
			return exp;
		}

		public void appendPredicates()
		{
			for (AlloyExp predicates : this.predicates)
			{
				this.exp += " " + predicates;
			}
			this.predicates.clear();
		}

		public String toPartBody()
		{
			String tmp = exp;
			for (AlloyExp expression : topLevel)
			{
				tmp += "\n\t" + expression;
			}
			return tmp;
		}
	}

	// public List<String> result = new Vector<String>();
	Set<INode> trnaslated = new HashSet<INode>();

	// String expression = "";
	private String moduleName;

	public Alloy2VdmAnalysis(String name)
	{
		this.moduleName = name;
	}

	@Override
	public AlloyPart caseAModuleModules(AModuleModules node, Context question)
			throws AnalysisException
	{
		// result.add("module " + moduleName + "\n");
		// result.add("open util/relation\n");
		this.components.add(new ModuleHeader(moduleName, "util/relation", "vdmutil"));

		BasicTokenSearch basicTokens = new BasicTokenSearch();
		node.apply(basicTokens);
		for (Entry<String, INode> entry : basicTokens.mkbasicToken.entrySet())
		{
			Sig s = new Sig(entry.getKey());
			s.isOne = true;
			this.components.add(s);
		}

		return super.caseAModuleModules(node, question);
	}

	@Override
	public AlloyPart caseATypeDefinition(ATypeDefinition node, Context ctxt)
			throws AnalysisException
	{
		if (trnaslated.contains(node))
		{
			return null;
		}
		trnaslated.add(node);

		ctxt.merge(createType(node.getType(), ctxt));

		return null;
	}

	private void createTypeInvariant(ATypeDefinition def, Sig sig, Context ctxt,PType type) // add param to know sig type.... type of sig  = type
			throws AnalysisException
	{
            if (def.getInvdef() != null) {
              //  String body = "all ";
                AlloyPart pattern = def.getInvPattern().apply(this, ctxt);
                String body = sig.name +" = { " + pattern.exp + " : " + type.toString() + " | ";
                Context invCtxt = new Context(ctxt);
                invCtxt.addVariable(pattern.exp, def.getType());
                body += def.getInvExpression().apply(this, invCtxt).exp + " }";
                Fact f = new Fact(sig.name + "Inv", body);
                this.components.add(f);
            }

	}

    private void createInvariantTypes(Sig sig) // method to create fact in types
            throws AnalysisException
    {
            System.out.println("Invariante: " + sig.getQuotes().toString());
            Fact f = new Fact(sig.name + "Type",toList(sig.getQuotes(), "+"),sig.name);
            this.components.add(f);


    }


	private Context createType(PType type, Context outer)
			throws AnalysisException
	{
		Context ctxt = new Context();
		if (outer.getSig(getTypeName(type)) != null)
		{
			return ctxt;
		}

		if (type instanceof SInvariantType)
		{
			SInvariantType invType = (SInvariantType) type;
			if (invType instanceof ANamedInvariantType)
			{
				ctxt.merge(createNamedType((ANamedInvariantType) invType, outer));
				return ctxt;
			} else if (invType instanceof ARecordInvariantType)
			{
				ARecordInvariantType recordType = (ARecordInvariantType) type;
				Sig s = new Sig(recordType.getName().getName());

				for (AFieldField f : recordType.getFields())
				{
					ctxt.merge(createType(f.getType(), outer));
					s.addField(f.getTag(), getFieldType(f.getType()));
					s.constraints.addAll(getFieldConstraints(f, s.name));
				}
				Context invCtxt = new Context(ctxt);
				if (recordType.getInvDef() != null)
				{
					AlloyPart invPart = recordType.getInvDef().getParamPatternList().get(0).get(0).apply(this, invCtxt);
					boolean hasLet = !invPart.exp.isEmpty();
					invPart.merge(recordType.getInvDef().getBody().apply(this, invCtxt));
					if (hasLet)
					{
						invPart.exp = "( " + invPart.exp + ")";
					}
					s.constraints.add(invPart.exp);
				}
				ctxt.addType(recordType, s);
				this.components.add(s);
				return ctxt;
			}
		} else if (type instanceof AQuoteType)
		{
			AQuoteType qt = (AQuoteType) type;

			String name = qt.getValue().getValue().toUpperCase();
			Sig s = new Sig(name);
			s.isOne = true;
			ctxt.addType(qt, s);
			this.components.add(s);
			return ctxt;
		} else if (type instanceof SBasicType)
		{
			if (type instanceof ABooleanBasicType)
			{

			} else if (type instanceof ATokenBasicType || type instanceof ACharBasicType)
			{
				Sig s = new Sig(getTypeName(type));
				ctxt.addType(type, s);
				this.components.add(s);
			} else if (type instanceof SNumericBasicType)
			{

			}
			return ctxt;
		} else if (type instanceof SSeqType)
		{
			// SSeqType stype = (SSeqType) type;
			// result.add("sig "+getTypeName(type))
			return ctxt;
		} else if (type instanceof AProductType)
		{
			Sig s = new Sig(getTypeName(type));
			Sig.FieldType ftype = null;
			for (Iterator<PType> itr = ((AProductType) type).getTypes().descendingIterator(); itr.hasNext();)
			{
				String fname = getTypeName(itr.next());
				if (ftype == null)
				{
					ftype = new Sig.FieldType(fname, Prefix.undefined);
				} else
				{
					ftype = new Sig.MapFieldType(fname, Prefix.undefined, ftype);
				}
			}
			s.addField("x", ftype);
			s.isWrapper = true;
			ctxt.addType(type, s);
			this.components.add(s);
			return ctxt;
		}

		// switch (type.kindPType())
		// {
		// case INVARIANT:
		// {
		// SInvariantType invType = (SInvariantType) type;
		// switch (invType.kindSInvariantType())
		// {
		// case NAMED:
		// {
		// ctxt.merge(createNamedType((ANamedInvariantType) invType, outer));
		// return ctxt;
		// }
		// case RECORD:
		// {
		// ARecordInvariantType recordType = (ARecordInvariantType) type;
		// Sig s = new Sig(recordType.getName().name);
		//
		// for (AFieldField f : recordType.getFields())
		// {
		// ctxt.merge(createType(f.getType(), outer));
		// s.addField(f.getTag(), getFieldType(f.getType()));
		// s.constraints.addAll(getFieldConstraints(f, s.name));
		// }
		// Context invCtxt = new Context(ctxt);
		// if (recordType.getInvDef() != null)
		// {
		// AlloyPart invPart = recordType.getInvDef().getParamPatternList().get(0).get(0).apply(this, invCtxt);
		// boolean hasLet = !invPart.exp.isEmpty();
		// invPart.merge(recordType.getInvDef().getBody().apply(this, invCtxt));
		// if (hasLet)
		// {
		// invPart.exp = "( " + invPart.exp + ")";
		// }
		// s.constraints.add(invPart.exp);
		// }
		// ctxt.addType(recordType, s);
		// this.components.add(s);
		// return ctxt;
		// }
		//
		// }
		// }
		// case QUOTE:
		// {
		// AQuoteType qt = (AQuoteType) type;
		// String name = qt.getValue().value.toUpperCase();
		// Sig s = new Sig(name);
		// s.isOne = true;
		// ctxt.addType(qt, s);
		// this.components.add(s);
		// return ctxt;
		// }
		// case BASIC:
		// {
		// switch (((SBasicType) type).kindSBasicType())
		// {
		// case BOOLEAN:
		// break;
		// case TOKEN:
		// case CHAR:
		// {
		// Sig s = new Sig(getTypeName(type));
		// ctxt.addType(type, s);
		// this.components.add(s);
		// }
		// case NUMERIC:
		// break;
		//
		// }
		// return ctxt;
		// }
		//
		// case SEQ:
		// {
		// // SSeqType stype = (SSeqType) type;
		// // result.add("sig "+getTypeName(type))
		// return ctxt;
		// }
		//
		// case PRODUCT:
		// {
		// Sig s = new Sig(getTypeName(type));
		// Sig.FieldType ftype = null;
		// for (Iterator<PType> itr = ((AProductType) type).getTypes().descendingIterator(); itr.hasNext();)
		// {
		// String fname = getTypeName(itr.next());
		// if (ftype == null)
		// {
		// ftype = new Sig.FieldType(fname, Prefix.undefined);
		// } else
		// {
		// ftype = new Sig.MapFieldType(fname, Prefix.undefined, ftype);
		// }
		// }
		// s.addField("x", ftype);
		// s.isWrapper = true;
		// ctxt.addType(type, s);
		// this.components.add(s);
		// return ctxt;
		// }
		// }
		return ctxt;
	}

	private Context createNamedType(ANamedInvariantType namedType, Context ctxt)
			throws AnalysisException
	{
		// switch (namedType.getType().kindPType())
		// {
		if (namedType.getType() instanceof SBasicType)
		{
			Sig s = new Sig(namedType.getName().getName());
			ctxt.merge(createType(namedType.getType(), ctxt));
			s.supers.add(ctxt.getSig(namedType.getType()));
			ctxt.addType(namedType, s);
			this.components.add(s);
			// break;
		}

		// case BASIC:
		// {
		// SBasicType bt = (SBasicType) t.getType();
		// switch (bt.kindSBasicType())
		// {
		// case TOKEN:
		// // result.add("sig " + t.getName().name + "{}");
		// Sig s = new Sig(node.getName().name);
		// ctxt.addType(bt, s);
		// this.components.add(s);
		// break;
		//
		// }
		// }
		// break;

		// case QUOTE:
		//
		// break;
		if (namedType.getType() instanceof AQuoteType) // new method to single quote types
		{
            AQuoteType qt = (AQuoteType)namedType.getType();
            createType(qt, ctxt);
            //System.out.println("CENSA "+namedType.getType().toString());
            Sig s = new Sig(namedType.getName().getName(),true); // create sig in univ{}
            List<String> quotes = new Vector<String>();
            quotes.add(qt.getValue().getValue().toUpperCase());
            s.setInTypes(quotes);
            this.components.add(s);
            createInvariantTypes(s);//fact
            System.out.println("CENSA "+s.toString());
		}

		// case UNION:
		if (namedType.getType() instanceof AUnionType)
		{
			AUnionType ut = (AUnionType) namedType.getType();
			List<String> quotes = new Vector<String>();
			for (PType ute : ut.getTypes())
			{
				if (ute instanceof AQuoteType)
				{
					AQuoteType qt = (AQuoteType) ute;
					String name = qt.getValue().getValue().toUpperCase();
					quotes.add(name);
					createType(ute, ctxt);
				} else if (ute instanceof ANamedInvariantType)
				{
					ANamedInvariantType nit = (ANamedInvariantType) ute;
					quotes.add(nit.getName().getName());
				}
			}
//            System.out.println("type: "+namedType.getName().getName()+"   Quotes: "+quotes.toString());

            Sig s = new Sig(namedType.getName().getName());
            s.setInTypes(quotes);
            ctxt.addType(ut, s);

            Sig sUniv = new Sig(namedType.getName().getName(),true); // create sig in univ{}
            this.components.add(sUniv);
            createInvariantTypes(s);//fact
            //System.out.println("CENSA1111 "+sUniv.toString());


            //this.components.add(s);
		}
		// break;

		// case SEQ:
		if (namedType.getType() instanceof SSeqType)
		{
			SSeqType stype = (SSeqType) namedType.getType();
			ctxt.merge(createType(stype.getSeqof(), ctxt));

			Sig s = new Sig(namedType.getName().getName());
			s.addField("x", getFieldType(stype));
			s.isWrapper = true;
			ctxt.addType(stype, s);
			this.components.add(s);
			this.components.add(new Fact(namedType.getName().getName() + "Set", "all c1,c2 : "
					+ namedType.getName().getName()
					+ " | c1.x = c2.x implies c1=c2"));
			// break;
		}

		// case SET:
		if (namedType.getType() instanceof ASetType)
		{
			ASetType stype = (ASetType) namedType.getType();
			ctxt.merge(createType(stype.getSetof(), ctxt));
			Sig s = new Sig(namedType.getName().getName());

			if (stype.getSetof() instanceof AProductType)
			{
				Sig superSig = ctxt.getSig(stype.getSetof());
				s.supers.add(superSig);
				s.isWrapper = superSig.isWrapper;
			} else
			{
				s.addField("x", getFieldType(stype));
				s.isWrapper = true;

				this.components.add(new Fact(namedType.getName().getName()
						+ "Set", "all c1,c2 : " + namedType.getName().getName()
						+ " | c1.x = c2.x implies c1=c2"));
			}
			ctxt.addType(stype, s);
			this.components.add(s);
			// createTypeInvariant(node, s, ctxt);
			// break;
		}
		// }

		if (namedType.parent() instanceof ATypeDefinition)
		{
			ATypeDefinition def = (ATypeDefinition) namedType.parent();
			if (ctxt.getSig(namedType) != null)
			{
               /* Sig sUniv = new Sig(namedType.getName().getName(), true); // create sig in univ{}
                if(!this.components.contains(sUniv)) {
                    ctxt.addType(def.getType(), sUniv);
                    this.components.add(sUniv);
                }*/

				createTypeInvariant(def, ctxt.getSig(namedType), ctxt,namedType.getType());
			}
		}
		return ctxt;
	}

	String getTypeName(PType type)
	{
		// switch (type.kindPType())
		// {
		// case SEQ:
		if (type instanceof SSeqType)
		{
			SSeqType stype = (SSeqType) type;
			return "seq " + getTypeName(stype.getSeqof());
		}

		// case SET:
		if (type instanceof ASetType)
		{
			ASetType stype = (ASetType) type;
			return "set " + getTypeName(stype.getSetof());
		}

		// case INVARIANT:
		if (type instanceof SInvariantType)
		{
			SInvariantType itype = (SInvariantType) type;
			// switch (itype.kindSInvariantType())
			// {
			// case NAMED:
			if (itype instanceof ANamedInvariantType)
				return ((ANamedInvariantType) itype).getName().getName();

			// case RECORD:
			if (itype instanceof ARecordInvariantType)
				return ((ARecordInvariantType) itype).getName().getName();
			// }
		}

		// case BASIC:
		if (type instanceof SBasicType)
		{
			return ((SBasicType) type)+"";
		}

		// case PRODUCT:
		if (type instanceof AProductType)
		{
			AProductType pType = (AProductType) type;
			String name = "";
			for (PType t : pType.getTypes())
			{
				name += getTypeName(t);
			}
			return name;
		}

		// case MAP:
		if (type instanceof SMapType)
		{
			FieldType s = getFieldType(type);
			return s.toString();
		}
		// SMapType mType = (SMapType) type;
		// return getTypeName(mType.getFrom())+getTypeName(mType.getTo());

		// }
		return "unknownTypeName";
	}

	private FieldType getFieldType(PType t)
	{
		// switch (t.kindPType())
		// {
		// case MAP:
		if (t instanceof SMapType)
		{
			SMapType ftype = (SMapType) t;
			return new Sig.MapFieldType(ftype.getFrom().toString(), (ftype instanceof SMapType ? FieldType.Prefix.undefined
					: FieldType.Prefix.lone), new Sig.FieldType(ftype.getTo().toString(), FieldType.Prefix.lone));
		}
		// case SET:
		if (t instanceof ASetType)
		{
			ASetType stype = (ASetType) t;
			return new Sig.FieldType(getTypeName(stype.getSetof()), Sig.FieldType.Prefix.set);
		}

		// case SEQ:
		if (t instanceof SSeqType)
		{
			SSeqType stype = (SSeqType) t;
			return new Sig.FieldType(getTypeName(stype.getSeqof()), Sig.FieldType.Prefix.seq);
		}

		// case INVARIANT:
		if (t instanceof SInvariantType)
		{
			SInvariantType invType = (SInvariantType) t;
			// switch (invType.kindSInvariantType())
			// {
			// case NAMED:
			if (invType instanceof ANamedInvariantType)
				return new Sig.FieldType(((ANamedInvariantType) invType).getName().getName());
			// case RECORD:
			if (invType instanceof ARecordInvariantType)
				return new Sig.FieldType(((ARecordInvariantType) invType).getName().getName());

			// }
			// }
		} 
		return null;
	}

	public List<String> getFieldConstraints(AFieldField field, String sig)
	{
		final List<String> constraints = new Vector<String>();
		if (field.getType() instanceof SMapType)
		{
			SMapType ftype = (SMapType) field.getType();
			// switch (ftype.kindSMapType())
			// {
			// case INMAP:
			if (ftype instanceof AInMapMapType)
				constraints.add(" /*" + sig + "." + field.getTag()
						+ " is an INMAP */ " + "injective[" + field.getTag()
						+ "," + sig + "] and functional[" + field.getTag()
						+ "," + sig + "]");
			// break;
			// case MAP:
			if (ftype instanceof AMapMapType)
				constraints.add(" /*" + sig + "." + field.getTag()
						+ " is a MAP   */ " + "functional[" + field.getTag()
						+ "," + sig + "]");
			// break;

			// }
		}
		return constraints;
	}

	@Override
	public AlloyPart caseAValueDefinition(AValueDefinition node, Context ctxt)
			throws AnalysisException
	{
		// switch (node.getType().kindPType())
		// {
		if (node.getType() instanceof SBasicType
				|| node.getType() instanceof SInvariantType)
		{
			// case BASIC:
			// case INVARIANT:
			// {
			String name = node.getPattern().toString();// todo
			Sig s = new Sig(name);
			ctxt.merge(createType(node.getType(), ctxt));
			// System.out.println("Type is: "+ node.getType()+" Found sig: "+ctxt.getSig(node.getType()).name);
			s.supers.add(ctxt.getSig(node.getType()));
			s.isOne = true;
			ctxt.addVariable(name, node.getType());
			this.components.add(s);
			// break;
			// return;
		} else
		// default:
		{
			System.out.println("Skipping value: \""
					+ node.getPattern().toString()
					+ "\" it should be generated as a function");
		}

		// }

		return new AlloyPart();
	}

	@Override
	public AlloyPart caseAStateDefinition(AStateDefinition node,
			Context question) throws AnalysisException
	{
		if (trnaslated.contains(node))
		{
			return null;
		}
		trnaslated.add(node);
		String name = node.getName().getName();
		Sig s = new Sig(name);

		for (Iterator<AFieldField> itr = node.getFields().iterator(); itr.hasNext();)
		{
			AFieldField f = itr.next();

			if (f.getType() instanceof SMapType)
			{
				SMapType ftype = (SMapType) f.getType();
				s.addField(f.getTag(), getFieldType(ftype)); /*
															 * new Sig.MapFieldType(ftype.getFrom().toString(),
															 * (ftype.kindSMapType() == EMapType.MAP ?
															 * FieldType.Prefix.undefined : FieldType.Prefix.lone), new
															 * Sig.FieldType(ftype.getTo().toString(),
															 * FieldType.Prefix.lone)));
															 */
				// switch (ftype.kindSMapType())
				// {
				// case INMAP:
				// s.constraints.add("/*" + name + "." + f.getTag()
				// + " is an INMAP */ " + "injective["
				// + f.getTag() + "," + s.name
				// + "] and functional[" + f.getTag() + ","
				// + s.name + "]");
				// break;
				// case MAP:
				// s.constraints.add("/*" + name + "." + f.getTag()
				// + " is a MAP   */ " + "functional["
				// + f.getTag() + "," + s.name + "]");
				// break;
				//
				// }
				s.constraints.addAll(getFieldConstraints(f, s.name));
			}
		}
		question.clearState();
		for (AFieldField f : node.getFields())
		{
			question.addState(f.getTag(), f.getTag());
		}
		if (node.getInvExpression() != null)
		{
			s.constraints.add(node.getInvExpression().apply(this, question).toPartBody());
		}
		this.components.add(s);
		return null;
	}

	public static String toList(List<String> quotes, String seperator)
	{
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> itr = quotes.iterator(); itr.hasNext();)
		{
			sb.append(itr.next());
			if (itr.hasNext())
			{
				sb.append(" " + seperator + " ");
			}
		}
		return sb.toString();
	}

	@Override
	public AlloyPart caseAImplicitOperationDefinition(
			AImplicitOperationDefinition node, Context question)
			throws AnalysisException
	{
		String stateId = node.getState().getName().getName().substring(0, 1).toLowerCase();
		String stateSigName = node.getState().getName().getName();
		String preStateId = stateId;
		String postStateId = preStateId + "'";
		String arguments = preStateId + " : " + stateSigName + ", "
				+ postStateId + " : " + stateSigName;

		List<String> stateFields = new Vector<String>();
		Context ctxt = new Context(question);

		for (Iterator<AFieldField> itr = node.getState().getFields().iterator(); itr.hasNext();)
		{
			AFieldField f = itr.next();
			stateFields.add(f.getTag());

		}

		boolean isfirst = true;
		for (Iterator<APatternListTypePair> itr = node.getParameterPatterns().iterator(); itr.hasNext();)
		{
			if (isfirst)
			{
				arguments += ", ";
				isfirst = false;
			}

			APatternListTypePair pl = itr.next();

			arguments += pl.getPatterns().get(0) + ": " + pl.getType();
			ctxt.addVariable(pl.getPatterns().get(0).toString(), pl.getType());
			if (itr.hasNext())
			{
				arguments += ", ";
			}
		}

		// frame conditions
		List<String> readOnlyState = new Vector<String>();
		readOnlyState.addAll(stateFields);
		for (AExternalClause framecondition : node.getExternals())
		{
			if (framecondition.getMode().getType() == VDMToken.WRITE)
			{
				for (ILexNameToken id : framecondition.getIdentifiers())
				{
					readOnlyState.remove(id.getName());
				}

			}
		}

		StringBuilder sb = new StringBuilder();

		if (!readOnlyState.isEmpty())
		{
			sb.append("\n\t /* Frame conditions */");
			for (String id : readOnlyState)
			{
				sb.append("\n\t" + postStateId + "." + id + " = " + preStateId
						+ "." + id);
			}
		}

		// expression = "";
		question.clearState();
		for (String f : stateFields)
		{
			question.addState(f, preStateId + "." + f);
		}

		if (node.getPrecondition() != null)
		{
			sb.append("\n\t /* Pre conditions */");
			sb.append("\n\t"
					+ node.getPrecondition().apply(this, ctxt).toPartBody());
		}
		// expression = "";
		question.clearState();
		for (String f : stateFields)
		{
			question.addState(f, postStateId + "." + f);
			question.addState(f + "~", preStateId + "." + f);
		}

		sb.append("\n\t /* Post conditions */");
		sb.append("\n\t"
				+ node.getPostcondition().apply(this, ctxt).toPartBody());

		this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));
		this.components.add(new Run(node.getName().getName()));
		return null;
	}

	@Override
	public AlloyPart caseAImplicitFunctionDefinition(
			AImplicitFunctionDefinition node, Context question)
			throws AnalysisException
	{
		Context ctxt = new Context(question);
		String arguments = "";
		int i = 0;
		for (Iterator<APatternListTypePair> itr = node.getParamPatterns().iterator(); itr.hasNext();)
		{
			APatternListTypePair p = itr.next();
			arguments += p.getPatterns().get(0) + ": "
					+ getTypeName(p.getType());
			ctxt.addVariable(p.getPatterns().get(0).toString(), node.getType().getParameters().get(i));
			i++;
		}

		if (!(node.getType().getResult() instanceof ABooleanBasicType))
		{
			String returnName = node.getResult().getPattern().toString();
			PType returnType = node.getResult().getType();
			ctxt.addVariable(returnName, returnType);
			if (arguments.isEmpty())
			{
				arguments = returnName + " : " + returnType;
			} else
			{
				arguments += ", " + returnName + " : " + returnType;
			}
		}

		question.clearState();
		StringBuilder sb = new StringBuilder();

		if (node.getPrecondition() != null)
		{
			sb.append("\n\t /* Pre conditions */");
			sb.append("\n\t"
					+ node.getPrecondition().apply(this, ctxt).toPartBody());
		}
		question.clearState();

		sb.append("\n\t /* Post conditions */");
		sb.append("\n\t"
				+ node.getPostcondition().apply(this, ctxt).toPartBody());

		this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));
		this.components.add(new Run(node.getName().getName()));
		return null;
	}

	@Override
	public AlloyPart caseAExplicitFunctionDefinition(
			AExplicitFunctionDefinition node, Context question)
			throws AnalysisException
	{
		if (node.getIsTypeInvariant())
		{
			return null;
		}
		Context ctxt = new Context(question);
		String arguments = "";

		List<String> lets = new Vector<String>();

		for (int i = 0; i < node.getType().getParameters().size(); i++)
		{
			PPattern p = node.getParamPatternList().get(0).get(i);
			String argumentName = null;
			if (p instanceof ARecordPattern)
			{
				argumentName = getNewName();
				StringBuffer letArgumentWrapper = new StringBuffer();
				// letArgumentWrapper.append("\n\t( let ");
				ARecordPattern rp = (ARecordPattern) p;
				for (int j = 0; j < rp.getPlist().size(); j++)
				{
					ARecordInvariantType t = (ARecordInvariantType) node.getType().getParameters().get(i);
					letArgumentWrapper.append(rp.getPlist().get(j) + " = "
							+ argumentName + "."
							+ t.getFields().get(j).getTag());
					ctxt.addVariable(rp.getPlist().get(j).toString(), t.getFields().get(j).getType());
					if (j < rp.getPlist().size() - 1)
					{
						letArgumentWrapper.append(", ");
					}
				}
				// letArgumentWrapper.append(" | \n\t");
				lets.add(letArgumentWrapper.toString());
			} else
			{
				argumentName = p.toString();
			}
			String pt = getTypeName(node.getType().getParameters().get(i));
			arguments += argumentName + ": " + pt;
			ctxt.addVariable(argumentName, node.getType().getParameters().get(i));
			if (i < node.getType().getParameters().size() - 1)
			{
				arguments += ", ";
			}
		}

		if (node.getType().getResult() instanceof ARecordInvariantType)// node.apply(new CheckMkAnalysis()))
		{
			ctxt = new PredicatContext(ctxt, getNewName(), node.getType().getResult());
		}

		question.clearState();
		StringBuilder sb = new StringBuilder();

		if (node.getBody() != null)
		{
			sb.append("\n\t /* Body */");
			sb.append("\n\t" + node.getBody().apply(this, ctxt).toPartBody());
		}

		if (node.getPrecondition() != null)
		{
			question.clearState();
			sb.append(" and ");
			sb.append("\n\t /* Pre conditions */");
			sb.append("\n\t"
					+ node.getPrecondition().apply(this, ctxt).toPartBody());
			// sb.append(")");
		}
		if (node.getPostcondition() != null)
		{
			question.clearState();

			sb.append("\n\t /* Post conditions */");
			sb.append("\n\t"
					+ node.getPostcondition().apply(this, ctxt).toPartBody());
		}

		String body = sb.toString();
		sb = new StringBuilder();
		for (String let : lets)
		{
			sb.append("( let " + let + " | ");
		}
		sb.append(body);
		for (int j = 0; j < lets.size(); j++)
		{
			sb.append(")");
		}

		if (node.getType().getResult() instanceof ABooleanBasicType)
		{
			this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));

		} else if (node.getType().getResult() instanceof ARecordInvariantType)
		{
			PredicatContext pCtxt = (PredicatContext) ctxt;
			arguments += ", " + pCtxt.getReturnName() + ": "
					+ pCtxt.getSig(pCtxt.getReturnType()).name;
			this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));
		} else
		{
			this.components.add(new Fun(node.getName().getName(), arguments, sb.toString(), getTypeName(node.getType().getResult())));
		}
		this.components.add(new Run(node.getName().getName()));
		return null;
	}

	int nextNameId = 0;

	private String getNewName()
	{
		return "var" + nextNameId++;
	}

	public AlloyPart caseATupleExp(org.overture.ast.expressions.ATupleExp node,
			Context question) throws AnalysisException
	{
		if (node.getAncestor(AInSetBinaryExp.class) != null)
		{
			AlloyPart p = new AlloyPart();

			Sig s = question.getSig(node.getType());
			if (!s.getFieldNames().isEmpty()
					&& s.getField(s.getFieldNames().get(0)).size() == node.getArgs().size())
			{
				List<String> fields = new Vector<String>();
				for (int i = 0; i < node.getArgs().size(); i++)
				{
					PExp a = node.getArgs().get(i);
					AlloyPart ap = a.apply(this, question);
					if (question.containsVariable(ap.exp))
					{
						fields.add(ap.exp);
					}

				}
				p.exp += toList(fields, "->") + " ";

			}
			return p;
		}

		return defaultInPExp(node, question);
	};

	@Override
	public AlloyPart caseALetBeStExp(ALetBeStExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("some ");
		Context ctxt = new Context(question);
		p.merge(node.getBind().apply(this, ctxt));
		p.exp += " | ";
		p.merge(node.getSuchThat().apply(this, ctxt));
		p.exp += " and ";
		p.merge(node.getValue().apply(this, ctxt));
		return p;
	}

	@Override
	public AlloyPart caseATuplePattern(ATuplePattern node, Context question)
			throws AnalysisException
	{
		ASetMultipleBind smb = null;
		if ((smb = node.getAncestor(ASetMultipleBind.class)) != null
				&& smb.getSet().getType() instanceof ANamedInvariantType)
		{
			AlloyPart p = new AlloyPart();
			Map<String, PType> variables = new HashMap<String, PType>();

			ANamedInvariantType aNamedInvariantType = (ANamedInvariantType) smb.getSet().getType();
			Sig s = question.getSig(aNamedInvariantType.getName().getName());

			AProductType ptype = (AProductType) ((ASetType) aNamedInvariantType.getType()).getSetof();
			if (s.getFieldNames().size() == 1)
			{
				for (int i = 0; i < node.getPlist().size(); i++)
				{
					PPattern a = node.getPlist().get(i);
					AlloyPart ap = a.apply(this, question);
					PType apType = ptype.getTypes().get(i);
					variables.put(ap.exp, apType);
					p.exp += ap.exp;
					if (i < node.getPlist().size() - 1)
					{
						p.exp += "->";
					}

					if (!question.containsVariable(ap.exp))
					{
						p.typeBindings.add(new AlloyTypeBind(ap.exp, question.getSig(apType)));
						question.addVariable(ap.exp, apType);
					}
				}

			}
			return p;
		}
		return super.caseATuplePattern(node, question);
	}

	@Override
	public AlloyPart caseAMkTypeExp(AMkTypeExp node, Context question)
			throws AnalysisException
	{
		if (question instanceof PredicatContext)
		{
			PredicatContext ctxt = (PredicatContext) question;
			AlloyPart p = new AlloyPart("(");
			ARecordInvariantType type = (ARecordInvariantType) ctxt.getReturnType();

			for (int i = 0; i < type.getFields().size(); i++)
			{
				p.exp += ctxt.getReturnName() + "."
						+ type.getFields().get(i).getTag() + "= ";
				p.merge(node.getArgs().get(i).apply(this, question));

				if (i < type.getFields().size() - 1)
				{
					p.exp += " and ";
				}
			}
			p.exp += ")";
			return p;
		}
		return super.caseAMkTypeExp(node, question);
	}

	@Override
	public AlloyPart caseAMkBasicExp(AMkBasicExp node, Context question)
			throws AnalysisException
	{
		String name = BasicTokenSearch.getName(node);
		for (Part p : this.components)
		{
			if (p instanceof Sig && ((Sig) p).name.equals(name))
			{
				return new AlloyPart(name);
			}
		}
		return super.caseAMkBasicExp(node, question);
	}

	@Override
	public AlloyPart caseARecordPattern(ARecordPattern node, Context question)
			throws AnalysisException
	{
		List<String> fieldNames = new Vector<String>();
		for (PPattern p : node.getPlist())
		{
			fieldNames.add(p.toString());
		}

		List<String> tfieldNames = new Vector<String>();
		for (AFieldField f : ((ARecordInvariantType) node.getType()).getFields())
		{
			tfieldNames.add(f.getTag());
		}

		if (tfieldNames.equals(fieldNames))
		{
			for (AFieldField f : ((ARecordInvariantType) node.getType()).getFields())
			{
				question.addVariable(f.getTag(), f.getType());
			}
			return new AlloyPart();
		} else
		{
			boolean parentIsDef = node.parent() instanceof PDefinition;
			String varName = null;
			AlloyPart p = new AlloyPart(" let ");
			if (!parentIsDef)
			{
				varName = getNewName();
				// p.typeBindings.add(new AlloyTypeBind(varName, question.getSig(node.getType())));
				p = new AlloyPart(varName);
				String let = "( let ";
				Map<String, PType> variables = new HashMap<String, PType>();
				for (int i = 0; i < fieldNames.size(); i++)
				{

					let += fieldNames.get(i) + " = "
							+ (!parentIsDef ? varName + "." : "")
							+ tfieldNames.get(i);
					PType type = ((ARecordInvariantType) node.getType()).getFields().get(i).getType();
					question.addVariable(fieldNames.get(i), type);
					variables.put(fieldNames.get(i), type);
					if (i < fieldNames.size() - 1)
					{
						let += ", ";
					}

				}
				let += " | ";
				p.predicates.add(new AlloyLetExp(let, variables));
				return p;
			}
			for (int i = 0; i < fieldNames.size(); i++)
			{
                //LETSSSSS
                if(!fieldNames.get(i).equals("-")) {//ACRESCENTEI ISTO
                    p.exp += fieldNames.get(i) + " = "
                            + (!parentIsDef ? varName + "." : "")
                            + tfieldNames.get(i);
                    question.addVariable(fieldNames.get(i), ((ARecordInvariantType) node.getType()).getFields().get(i).getType());
                    if (i < fieldNames.size() - 1) {
                        p.exp += ", ";
                    }
                }
			}
			p.exp += " | ";
			return p;
		}

	}

	@Override
	public AlloyPart caseAVariableExp(AVariableExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart();
		String name = node.getName().getName()
				+ (node.getName().isOld() ? "~" : "");
		String exp = "";
		if (question.containsState(name))
		{
			exp += question.getState(name);
		} else
		{
			if (!question.containsVariable(name)
					&& !(node.getType() instanceof AFunctionType))
			{
				Sig sig = question.getSig(node.getType());
				if (sig != null)
				{
					if (sig.getFieldNames().size() == 1)
					{
						exp += name + "." + sig.getFieldNames().get(0);
					}
				}
			} else
			{
				exp += name;
			}
		}
		if (exp.isEmpty())
		{
			System.err.println("no name for: " + node + " found "
					+ node.getLocation());
		}
		p.exp += exp;
		return p;
	}

	@Override
	public AlloyPart caseAFieldExp(AFieldExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart(node.getObject() + "." + node.getField());
		return p;
	}

	@Override
	public AlloyPart caseAMapDomainUnaryExp(AMapDomainUnaryExp node,
			Context question) throws AnalysisException
	{
		// AlloyPart p = new AlloyPart("(");
		// p.merge(node.getExp().apply(this, question));
		// p.exp += ").univ";

		AlloyPart p = new AlloyPart("dom[");
		p.merge(node.getExp().apply(this, question));
		p.exp += "]";
		return p;
	}

	public AlloyPart caseAMapRangeUnaryExp(
			org.overture.ast.expressions.AMapRangeUnaryExp node,
			Context question) throws AnalysisException
	{
		AlloyPart p = new AlloyPart("ran[");
		p.merge(node.getExp().apply(this, question));
		p.exp += "]";
		return p;
	};

	public AlloyPart caseANotUnaryExp(
			org.overture.ast.expressions.ANotUnaryExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("not ");
		p.merge(node.getExp().apply(this, question));
		return p;
	};

	@Override
	public AlloyPart caseADistUnionUnaryExp(ADistUnionUnaryExp node,
			Context question) throws AnalysisException
	{

		if (node.getExp() instanceof ASetCompSetExp)
		{
			AlloyPart p = new AlloyPart("toSet[ ");
			AlloyPart setcomprehension = node.getExp().apply(this, question);
			p.merge(setcomprehension);
			p.exp += "]";
			return p;
		} else
		{
			AlloyPart p = new AlloyPart("(");
			p.merge(node.getExp().apply(this, question));
			Sig eType = question.getSig(node.getExp().getType());
			if (eType.isWrapper)
			{
				Sig nestedT = question.getSig(eType.getField(eType.getFieldNames().get(0)).sigTypeName);
				p.exp += "." + eType.getFieldNames().get(0) + ")."
						+ nestedT.getFieldNames().get(0);
			}

			return p;
		}
	}

	@Override
	public AlloyPart caseASetEnumSetExp(ASetEnumSetExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("(");
		for (Iterator<PExp> itr = node.getMembers().iterator(); itr.hasNext();)
		{
			p.merge(itr.next().apply(this, question));
			if (itr.hasNext())
			{
				p.exp += " + ";
			}

		}
		p.exp += ")";
		return p;
	}

	@Override
	public AlloyPart caseASetCompSetExp(ASetCompSetExp node, Context question)
			throws AnalysisException
	{
		// first visit bind to build context
		Context setCompCtxt = new Context(question);
		AlloyPart pbind = new AlloyPart();
		for (Iterator<PMultipleBind> itr = node.getBindings().iterator(); itr.hasNext();)
		{
			AlloyPart bindPart = itr.next().apply(this, setCompCtxt);
			pbind.merge(bindPart);
			if (itr.hasNext())
			{
				pbind.exp += " , ";
			}
		}

		AlloyPart p = new AlloyPart("{");

		if (node.getFirst() instanceof ASetEnumSetExp)
		{
			ASetEnumSetExp setEnum = (ASetEnumSetExp) node.getFirst();

			for (Iterator<PExp> itr = setEnum.getMembers().iterator(); itr.hasNext();)
			{
				PExp exp = itr.next();

				AlloyPart ep = exp.apply(this, setCompCtxt);
				for (AlloyTypeBind bind : pbind.typeBindings)
				{
					if (bind.var.equals(ep.exp))
					{
						p.exp += bind.exp;
						if (itr.hasNext())
						{
							p.exp += ", ";
						}
						break;
					}
				}

			}
			pbind.typeBindings.clear();
		} else
		{
			p.merge(node.getFirst().apply(this, setCompCtxt));
		}

		p.exp += " | ";

		p.merge(pbind);
		p.exp += "}";
		return p;
	}

	@Override
	public AlloyPart caseAQuoteLiteralExp(AQuoteLiteralExp node,
			Context question) throws AnalysisException
	{
		return new AlloyPart(node.getValue().getValue().toUpperCase());
	}

	@Override
	public AlloyPart defaultInINode(INode node, Context question)
			throws AnalysisException
	{
        //System.out.println("Numero "+node.toString());

        if (node instanceof PExp)
		{
          	//return new AlloyPart(" /* NOT Translated("+ node.getClass().getSimpleName() + ")*/");
            return new AlloyPart(node.toString()+"]");
		}
		return null;
	}

	public AlloyPart defaultSBinaryExp(SBinaryExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("(");
       // System.out.println("Numero "+node.getClass().toString());
		p.merge(node.getLeft().apply(this, question));
       // System.out.println("Numero "+node.toString());
		if (node instanceof SBinaryExp)

		// switch (node.kindSBinaryExp())
		{
           // System.out.println("Numero "+node.getType().toString()+" name : " +node.toString());
            if(node instanceof AGreaterEqualNumericBinaryExp){
                System.out.println("Numero é Este : "+p.predicates.toString());
                p.exp="gte["+p.exp+",";
                //p.exp+=" >= ";
            }else if (node instanceof SBooleanBinaryExp)
			// case BOOLEAN:
			{
				SBooleanBinaryExp exp = (SBooleanBinaryExp) node;
				// switch (exp.kindSBooleanBinaryExp())
				// {
				// case AND:


                if (node instanceof AAndBooleanBinaryExp)
				{
					p.exp += " and ";
				}
				// case EQUIVALENT:
				else if (node instanceof AEquivalentBooleanBinaryExp)
				{
					p.exp += " = ";
				}
				// case IMPLIES:
				else if (node instanceof AImpliesBooleanBinaryExp)
				{//System.out.println("Numero "+node.toString());
					p.exp += " implies ";
				}
				// case OR:
				else if (node instanceof AOrBooleanBinaryExp)
				{
					p.exp += " or ";
				}

				// }
			} else if (node instanceof ACompBinaryExp)
			{

			}
			// break;
			// case COMP:
			// break;
			// case DOMAINRESBY:
			// break;
			else if (node instanceof ADomainResByBinaryExp)
			{
			} else if (node instanceof ADomainResToBinaryExp)
			{
			}
			// case DOMAINRESTO:
			// break;
			else if (node instanceof AEqualsBinaryExp)
			// case EQUALS:
			{
				p.exp += " = ";
			} else if (node instanceof AInSetBinaryExp)
			{
				// case INSET:
				// p.exp += " in ";
				throw new AnalysisException("should not go here");
			} else if (node instanceof AMapUnionBinaryExp)
			{
				// case MAPUNION:
				// break;
			}
			// case NOTEQUAL:
			else if (node instanceof ANotEqualBinaryExp)
			{
				p.exp += " != ";
			} else if (node instanceof ANotInSetBinaryExp)
			{
				// case NOTINSET:
				// break;
			} else if (node instanceof SNumericBinaryExp)
			{

				// case NUMERIC:
				// break;
			} else if (node instanceof APlusPlusBinaryExp)
			{
				// case PLUSPLUS:
				p.exp += " ++ ";
				// break;
			}
			// case PROPERSUBSET:
			else if (node instanceof AProperSubsetBinaryExp)
			{
				// break;
			}
			// case RANGERESBY:
			// break;
			else if (node instanceof ARangeResByBinaryExp)
			{

			} else if (node instanceof ARangeResToBinaryExp)
			{
				// case RANGERESTO:
				p.exp += " :> ";
				// break;
			}
			// case SEQCONCAT:
			// break;
			else if (node instanceof ASeqConcatBinaryExp)
			{

			} else if (node instanceof ASetDifferenceBinaryExp)
			{

			}
			// case SETDIFFERENCE:
			// break;
			else if (node instanceof ASetIntersectBinaryExp)
			{
				// case SETINTERSECT:
				p.exp += " & ";
				// break;
			} else if (node instanceof ASetUnionBinaryExp)
			{
				// case SETUNION:
				p.exp += " + ";
				// break;
			} else if (node instanceof AStarStarBinaryExp)
			{
				// case STARSTAR:
				// break;
			} else if (node instanceof ASubsetBinaryExp)
			{
				// case SUBSET:
				// break;

			}
		}

		p.merge(node.getRight().apply(this, question));
		p.exp += ")";
		return p;
	};

	@Override
	public AlloyPart caseAMapEnumMapExp(AMapEnumMapExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("(");

		for (Iterator<AMapletExp> itr = node.getMembers().iterator(); itr.hasNext();)
		{
			AMapletExp maplet = itr.next();

			p.merge(maplet.getLeft().apply(this, question));
			p.exp += " -> ";
			p.merge(maplet.getRight().apply(this, question));
			if (itr.hasNext())
			{
				p.exp += " + ";
			}
		}

		p.exp += ")";
		return p;
	}

	@Override
	public AlloyPart caseADomainResByBinaryExp(ADomainResByBinaryExp node,
			Context question) throws AnalysisException
	{
		AlloyPart p = new AlloyPart("(");
		p.exp += "univ -";
		p.merge(node.getLeft().apply(this, question));
		p.exp += ")";
		p.exp += " <: ";
		p.merge(node.getRight().apply(this, question));
		return p;
	}

	@Override
	public AlloyPart caseAApplyExp(AApplyExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart();
		if (node.getRoot().getType() instanceof SMapType
				&& node.getAncestor(AStateDefinition.class) == null)
		{
			AlloyPart p1 = new AlloyPart();
			p1.merge(node.getArgs().get(0).apply(this, question));
			p1.exp += " in dom[";
			p1.merge(node.getRoot().apply(this, question));
			p1.exp += "]";
			p.topLevel.add(new AlloyExp(" and /*Map domain pre condition */ \n\t"
					+ p1.exp));
		}

		p.merge(node.getRoot().apply(this, question));
		p.exp += "[";
		for (Iterator<PExp> itr = node.getArgs().iterator(); itr.hasNext();)
		{
			p.merge(itr.next().apply(this, question));
			if (itr.hasNext())
			{
				p.exp += ", ";
			}
		}
		p.exp += "] ";
		return p;
	}

	@Override
	public AlloyPart caseAMapInverseUnaryExp(AMapInverseUnaryExp node,
			Context question) throws AnalysisException
	{
		// Only supported if directly before an apply
		AlloyPart p = new AlloyPart("~(");
		mergeReturns(p, node.getExp().apply(this, question));
		p.exp += ")";
		return p;
	}

	@Override
	public AlloyPart caseAForAllExp(AForAllExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("( all ");
		Context ctxt = new Context(question);
		AlloyPart bindPart = (node.getBindList().get(0).apply(this, ctxt));// TODO
		boolean hasTypeBindings = !bindPart.typeBindings.isEmpty();
		if (bindPart.typeBindings.isEmpty())
		{
			p.merge(bindPart);
			p.exp += " | ";
		} else
		{
			for (Iterator<AlloyTypeBind> itr = bindPart.typeBindings.iterator(); itr.hasNext();)
			{
				p.exp += itr.next().exp;
				if (itr.hasNext())
				{
					p.exp += ", ";
				}
			}
			p.exp += " | (";
			bindPart.typeBindings.clear();
			p.merge(bindPart);
			p.exp += " implies ";
		}
		boolean hasLet = false;
		if (!p.predicates.isEmpty())
		{

			for (AlloyExp exp : p.predicates)
			{
				if (exp instanceof AlloyLetExp)
				{
					hasLet = true;
					break;
				}
			}

			p.appendPredicates();
			if (!hasLet)
			{
				p.exp += " and ";
			}
		}

		p.merge(node.getPredicate().apply(this, ctxt));
		if (hasLet)
		{
			p.exp += ")";
		}
		p.exp += ")";
		if (hasTypeBindings)
		{
			p.exp += ")";
		}
		return p;
	}

	public AlloyPart caseAExistsExp(
			org.overture.ast.expressions.AExistsExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("some ");
		mergeReturns(p, node.getBindList().get(0).apply(this, question));// TODO
		p.exp += " | ";
		mergeReturns(p, node.getPredicate().apply(this, question));
		return p;
	};

	@Override
	public AlloyPart caseASetMultipleBind(ASetMultipleBind node,
			Context question) throws AnalysisException
	{
		AlloyPart p = createNewReturnValue(node, question);

		for (Iterator<PPattern> iterator = node.getPlist().iterator(); iterator.hasNext();)
		{
			PPattern e = iterator.next();
			if (!_visitedNodes.contains(e))
			{
				AlloyPart sp = e.apply(this, question);
				p.merge(sp);
				question.addVariable(sp.exp, getBoundType(node.getSet().getType()));
				for (AlloyExp sub : sp.predicates)
				{
					if (sub instanceof AlloyLetExp)
					{
						question.addVariables(((AlloyLetExp) sub).variables);
					}
				}
				if (iterator.hasNext())
				{
					p.merge(new AlloyPart(", "));
				}
			}
		}
		boolean isTupeBind = false;
		for (PPattern pattern : node.getPlist())
		{
			if (pattern instanceof ATuplePattern)
			{
				isTupeBind = true;
				break;
			}
		}

		if (isTupeBind)
		{
			p.exp += " in ";
		} else
		{
			p.exp += " : ";
		}
		if (node.getSet() != null && !_visitedNodes.contains(node.getSet()))
		{
			p.merge(getBind(node.getSet(), question));// node.getSet().apply(this, question));
		}
		return p;
	}

	AlloyPart getBind(PExp bindto, Context ctxt) throws AnalysisException
	{
		AlloyPart p = new AlloyPart();
		p.merge(bindto.apply(this, ctxt));

		Sig s = ctxt.getSig(bindto.getType());
		if (s != null && bindto instanceof AVariableExp && s.isWrapper)
		{
			p.exp += "." + s.getFieldNames().get(0);
		}
		return p;
	}

	public PType getBoundType(PType type)
	{
		if (type instanceof ASetType)
		{
			return ((ASetType) type).getSetof();
		} else if (type instanceof SSeqType)
		{
			return ((SSeqType) type).getSeqof();
		}
		return type;
	}

	@Override
	public AlloyPart caseAIdentifierPattern(AIdentifierPattern node,
			Context question) throws AnalysisException
	{
		return new AlloyPart(node.getName().getName());
	}

	@Override
	public AlloyPart mergeReturns(AlloyPart original, AlloyPart new_)
	{
		if (original == null || new_ == null)
		{
			return null;
		}
		original.exp += new_.exp;
		original.predicates.addAll(new_.predicates);
		original.topLevel.addAll(new_.topLevel);
		original.typeBindings.addAll(new_.typeBindings);
		return original;
	}

	@Override
	public AlloyPart createNewReturnValue(INode node, Context question)
	{
		return new AlloyPart();
	}

	@Override
	public AlloyPart createNewReturnValue(Object node, Context question)
	{
		return new AlloyPart();
	}

	/**/
	public AlloyPart caseAPlusPlusBinaryExp(
			org.overture.ast.expressions.APlusPlusBinaryExp node,
			Context question) throws AnalysisException
	{
		return defaultSBinaryExp(node, question);
	};

	public AlloyPart caseAEqualsBinaryExp(
			org.overture.ast.expressions.AEqualsBinaryExp node, Context question)
			throws AnalysisException
	{
		if (node.getRight() instanceof ASetEnumSetExp
				&& ((ASetEnumSetExp) node.getRight()).getMembers().isEmpty())
		{
			AlloyPart p = new AlloyPart("no ");
			p.merge(node.getLeft().apply(this, question));
			return p;
		} else if (node.getLeft() instanceof ASetEnumSetExp
				&& ((ASetEnumSetExp) node.getLeft()).getMembers().isEmpty())
		{
			AlloyPart p = new AlloyPart("no ");
			p.merge(node.getRight().apply(this, question));
			return p;
		}
		return defaultSBinaryExp(node, question);
	};

	public AlloyPart caseARangeResToBinaryExp(
			org.overture.ast.expressions.ARangeResToBinaryExp node,
			Context question) throws AnalysisException
	{
		return defaultSBinaryExp(node, question);
	};

	public AlloyPart caseAImpliesBooleanBinaryExp(
			org.overture.ast.expressions.AImpliesBooleanBinaryExp node,
			Context question) throws AnalysisException
	{
		return defaultSBinaryExp(node, question);
	};

	public AlloyPart caseAInSetBinaryExp(
			org.overture.ast.expressions.AInSetBinaryExp node, Context question)
			throws AnalysisException
	{

		AlloyPart p = new AlloyPart("(");
		String bind = "";

		bind = " in ";

		p.merge(node.getLeft().apply(this, question));
		p.exp += bind;
		p.merge(getBind(node.getRight(), question));
		if (!p.predicates.isEmpty())
		{
			p.exp += " |";
		}
		p.appendPredicates();
		p.exp += ")";
		return p;
	};

	public AlloyPart caseAAndBooleanBinaryExp(
			org.overture.ast.expressions.AAndBooleanBinaryExp node,
			Context question) throws AnalysisException
	{
		return defaultSBinaryExp(node, question);
	};

	public AlloyPart caseAOrBooleanBinaryExp(AOrBooleanBinaryExp node,
			Context question) throws AnalysisException
	{
		return defaultSBinaryExp(node, question);
	};

	public AlloyPart caseANotEqualBinaryExp(
			org.overture.ast.expressions.ANotEqualBinaryExp node,
			Context question) throws AnalysisException
	{
		return defaultSBinaryExp(node, question);
	};

	public AlloyPart caseASetIntersectBinaryExp(
			org.overture.ast.expressions.ASetIntersectBinaryExp node,
			Context question) throws AnalysisException
	{
		return defaultSBinaryExp(node, question);
	};

    public AlloyPart caseAIntLiteralExp(
            org.overture.ast.expressions.AIntLiteralExp node,
            Context question) throws AnalysisException
    {
        return defaultInINode(node, question);
    };

    public AlloyPart caseAGreaterEqualNumericBinaryExp(AGreaterEqualNumericBinaryExp node,
                                             Context question) throws AnalysisException
    {
        return defaultSBinaryExp(node, question);
    };


    @Override
	public AlloyPart caseASetUnionBinaryExp(ASetUnionBinaryExp node,
			Context question) throws AnalysisException
	{// TODO: not checked
		return defaultSBinaryExp(node, question);
	}
}
