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

import org.overture.alloy.Sig.FieldType;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptorQuestionAnswer;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.expressions.AApplyExp;
import org.overture.ast.expressions.ADistUnionUnaryExp;
import org.overture.ast.expressions.ADomainResByBinaryExp;
import org.overture.ast.expressions.AForAllExp;
import org.overture.ast.expressions.AInSetBinaryExp;
import org.overture.ast.expressions.AMapDomainUnaryExp;
import org.overture.ast.expressions.AMapEnumMapExp;
import org.overture.ast.expressions.AMapInverseUnaryExp;
import org.overture.ast.expressions.AMapletExp;
import org.overture.ast.expressions.AOrBooleanBinaryExp;
import org.overture.ast.expressions.AQuoteLiteralExp;
import org.overture.ast.expressions.ASetCompSetExp;
import org.overture.ast.expressions.ASetEnumSetExp;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.expressions.EExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.expressions.SBinaryExp;
import org.overture.ast.expressions.SBooleanBinaryExp;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.lex.VDMToken;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.ASetMultipleBind;
import org.overture.ast.patterns.ATuplePattern;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.statements.AExternalClause;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.AQuoteType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.EMapType;
import org.overture.ast.types.EType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SBasicType;
import org.overture.ast.types.SInvariantType;
import org.overture.ast.types.SMapType;
import org.overture.ast.types.SSeqType;
import org.overture.typechecker.TypeComparator;

public class Alloy2VdmAnalysis
		extends
		DepthFirstAnalysisAdaptorQuestionAnswer<Alloy2VdmAnalysis.Context, Alloy2VdmAnalysis.AlloyPart>
{
	private static final long serialVersionUID = 1L;

	public class AlloyPart
	{
		public String exp = "";
		public Queue<AlloyExp> predicates = new LinkedList<AlloyExp>();

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

	}

	public static class Context
	{
		final Map<PType, Sig> types = new HashMap<PType, Sig>();
		final Map<String, PType> variables = new HashMap<String, PType>();

		@Override
		public String toString()
		{
			StringBuffer sb = new StringBuffer();

			for (Entry<PType, Sig> entry : types.entrySet())
			{
				sb.append(entry.getKey().toString() + " -> "
						+ entry.getValue().name + "\n");
			}
			return sb.toString();
		}

		public Sig getSig(PType type)
		{
			for (Entry<PType, Sig> entry : types.entrySet())
			{
				if (TypeComparator.compatible(entry.getKey(), type))
				{
					return entry.getValue();
				}
			}
			return null;
		}

		public Sig getSig(String sigTypeName)
		{
			for (Entry<PType, Sig> entry : types.entrySet())
			{
				if (entry.getValue().name.equals(sigTypeName))
				{
					return entry.getValue();
				}
			}
			return null;
		}

		public void merge(Context ctxt)
		{
			if (ctxt != null)
			{
				this.types.putAll(ctxt.types);
			}
		}

	}

	public List<String> result = new Vector<String>();
	Set<INode> trnaslated = new HashSet<INode>();

	Map<String, String> stateMap = new HashMap<String, String>();
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
		result.add("module " + moduleName + "\n");
		result.add("open util/relation\n");
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

		if (node.getType() instanceof ANamedInvariantType)
		{
			ANamedInvariantType t = (ANamedInvariantType) node.getType();
			switch (t.getType().kindPType())
			{
				case BASIC:
				{
					SBasicType bt = (SBasicType) t.getType();
					switch (bt.kindSBasicType())
					{
						case TOKEN:
							result.add("sig " + t.getName().name + "{}");
							Sig s = new Sig(node.getName().name);
							ctxt.types.put(bt, s);
							break;

					}
				}
					break;

				case QUOTE:

					break;

				case UNION:
				{
					AUnionType ut = (AUnionType) t.getType();
					// result.add("abstract sig "+node.getName().name+"{}");
					List<String> quotes = new Vector<String>();
					for (PType ute : ut.getTypes())
					{
						if (ute instanceof AQuoteType)
						{
							AQuoteType qt = (AQuoteType) ute;
							String name = qt.getValue().value.toUpperCase();
							quotes.add(name);
							// result.add("one sig " + name + "{}");// " extends "+node.getName().name+"{}");
							createType(ute);
						} else if (ute instanceof ANamedInvariantType)
						{
							ANamedInvariantType nit = (ANamedInvariantType) ute;
							quotes.add(nit.getName().name);
						}
					}
					result.add("sig " + node.getName().name + " in "
							+ toList(quotes, "+") + "{}");
					Sig s = new Sig(node.getName().name);
					s.setInTypes(quotes);
					ctxt.types.put(ut, s);
				}
					break;

				case SEQ:
				{
					SSeqType stype = (SSeqType) t.getType();
					ctxt.merge(createType(stype.getSeqof()));
					result.add("sig "
							+ node.getName().name
							+ " {\n\t /*This should actually use the list lib*/x : set "
							+ getTypeName(stype.getSeqof()) + "\n}");
					result.add("fact " + node.getName().name + "Set"
							+ "{all c1,c2 : " + node.getName().name
							+ " | c1.x = c2.x implies c1=c2}\n");

					Sig s = new Sig(node.getName().name);
					s.addField("x", new Sig.FieldType(getTypeName(stype.getSeqof()), Sig.FieldType.Prefix.seq));
					ctxt.types.put(stype, s);
					break;
				}

				case SET:
				{
					ASetType stype = (ASetType) t.getType();
					ctxt.merge(createType(stype.getSetof()));
					result.add("sig " + node.getName().name + " {\n\tx : set "
							+ getTypeName(stype.getSetof()) + "\n}");
					result.add("fact " + node.getName().name + "Set"
							+ "{all c1,c2 : " + node.getName().name
							+ " | c1.x = c2.x implies c1=c2}\n");
					Sig s = new Sig(node.getName().name);
					s.addField("x", new Sig.FieldType(getTypeName(stype.getSetof()), Sig.FieldType.Prefix.set));
					ctxt.types.put(stype, s);
					break;
				}
			}
		}
		return null;
	}

	private Context createType(PType type)
	{
		Context ctxt = new Context();
		switch (type.kindPType())
		{
			case QUOTE:
			{
				AQuoteType qt = (AQuoteType) type;
				String name = qt.getValue().value.toUpperCase();
				result.add("one sig " + name + "{}");// " extends "+node.getName().name+"{}");
				Sig s = new Sig(name);
				s.isOne = true;
				ctxt.types.put(qt, s);
				return ctxt;
			}
			case BASIC:
			{
				switch (((SBasicType) type).kindSBasicType())
				{
					case BOOLEAN:
						break;
					case CHAR:
						result.add("sig " + getTypeName(type) + " {}");
						ctxt.types.put(type, new Sig(getTypeName(type)));
					case NUMERIC:
						break;
					case TOKEN:
						break;

				}
				return ctxt;
			}

			case SEQ:
			{
				// SSeqType stype = (SSeqType) type;
				// result.add("sig "+getTypeName(type))
				return ctxt;
			}

			case PRODUCT:
			{
				Sig s = new Sig(getTypeName(type));
				String sig = "sig " + getTypeName(type) + "{";
				int i = 0;
				for (Iterator<PType> itr = ((AProductType) type).getTypes().iterator(); itr.hasNext();)
				{
					String fname = getTypeName(itr.next());
					s.addField("x" + i, new Sig.FieldType(fname));
					sig += "\n\tx" + i + " : " + fname;
					if (itr.hasNext())
					{
						sig += ",";
					}
					i++;
				}

				result.add(sig + "\n}");
				ctxt.types.put(type, s);
				return ctxt;
			}
		}
		return ctxt;
	}

	String getTypeName(PType type)
	{
		switch (type.kindPType())
		{
			case SEQ:
			{
				SSeqType stype = (SSeqType) type;
				return "seq " + getTypeName(stype.getSeqof());
			}
			case SET:
			{
				ASetType stype = (ASetType) type;
				return "set " + getTypeName(stype.getSetof());
			}
			case INVARIANT:
			{
				SInvariantType itype = (SInvariantType) type;
				switch (itype.kindSInvariantType())
				{
					case NAMED:
						return ((ANamedInvariantType) itype).getName().name;

					case RECORD:
						break;

				}
			}
			case BASIC:
			{
				return ((SBasicType) type).kindSBasicType().name();
			}

			case PRODUCT:
				AProductType pType = (AProductType) type;
				String name = "";
				for (PType t : pType.getTypes())
				{
					name += getTypeName(t);
				}
				return name;
		}
		return "unknownTypeName";
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
		String name = node.getName().name;
//		List<String> facts = new Vector<String>();
		Sig s = new Sig(name);

//		result.add("\nsig " + name + "{");

		for (Iterator<AFieldField> itr = node.getFields().iterator(); itr.hasNext();)
		{
			AFieldField f = itr.next();

			if (f.getType().kindPType() == EType.MAP)
			{
				SMapType ftype = (SMapType) f.getType();
//				String field = "\t" + f.getTag() + ": " + ftype.getFrom()
//						+ " -> "
//						+ (ftype.kindSMapType() == EMapType.MAP ? "one " : "")
//						+ ftype.getTo() + (itr.hasNext() ? "," : "");
//				result.add(field);
				s.addField(f.getTag(), new Sig.MapFieldType(ftype.getFrom().toString(), (ftype.kindSMapType() == EMapType.MAP?FieldType.Prefix.undefined:FieldType.Prefix.lone), new Sig.FieldType(ftype.getTo().toString(), FieldType.Prefix.lone)));
				switch (ftype.kindSMapType())
				{
					case INMAP:
						s.constraints.add("/*" + name + "." + f.getTag() + " is an INMAP */ "+ "injective["+f.getTag()+","+s.name+"] and functional["+f.getTag()+","+s.name+"]");
//						facts.add("/*" + name + "." + f.getTag()
//								+ " is an INMAP*/\n\t (some fe: " + name
//								+ " | ( all fs1,fs2 : fe." + f.getTag()
//								+ ".univ | fe." + f.getTag() + "[fs1] = fe."
//								+ f.getTag() + "[fs2] implies fs1=fs2 ))");
						
						// facts.add("injective["+f.getTag()+"] and surjective["+f.getTag()+"]");
						// or
						// facts.add("bijective["+f.getTag()+"]");
						break;
					case MAP:
//						facts.add("/*" + name + "." + f.getTag()
//								+ " is a MAP*/\n\t (some fe: " + name
//								+ " | ( all fs1,fs2 : fe." + f.getTag()
//								+ ".univ | fs1=fs2  implies fe." + f.getTag()
//								+ "[fs1] = fe." + f.getTag() + "[fs2] ))");
						// facts.add("surjective["+f.getTag()+"]");
						s.constraints.add("/*" + name + "." + f.getTag() + " is a MAP   */ "+ "functional["+f.getTag()+","+s.name+"]");
						break;

				}
			}
		}

//		result.add("}");
//		// INV
//		result.add("{");
		// expression = "";
		stateMap.clear();
		for (AFieldField f : node.getFields())
		{
			stateMap.put(f.getTag(), f.getTag());
			// stateMap.put(f + "~", preStateId + "." + f);
		}

		s.constraints.add( node.getInvExpression().apply(this, question).exp);
		result.add(s.toString());
//		result.add("}");

//		result.add("fact{");
//		for (Iterator<String> itr = facts.iterator(); itr.hasNext();)
//		{
//			result.add("\t" + itr.next());
//			if (itr.hasNext())
//			{
//				result.add("\tand");
//			}
//		}
//		result.add("}\n");
		return null;
	}

	static String toList(List<String> quotes, String seperator)
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
		String stateSigName = node.getState().getName().name;
		String preStateId = stateId;
		String postStateId = preStateId + "'";
		String arguments = preStateId + " : " + stateSigName + ", "
				+ postStateId + " : " + stateSigName;

		List<String> stateFields = new Vector<String>();

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
			if (framecondition.getMode().type == VDMToken.WRITE)
			{
				for (LexNameToken id : framecondition.getIdentifiers())
				{
					readOnlyState.remove(id.name);
				}

			}
		}

		result.add("pred " + node.getName().name + "(" + arguments + ")");
		result.add("{");

		if (!readOnlyState.isEmpty())
		{
			result.add("\t /* Frame conditions */");
			for (String id : readOnlyState)
			{
				result.add("\t" + postStateId + "." + id + " = " + preStateId
						+ "." + id);
			}
		}

		// expression = "";
		stateMap.clear();
		for (String f : stateFields)
		{
			stateMap.put(f, preStateId + "." + f);
		}

		result.add("\t /* Pre conditions */");
		result.add("\t" + node.getPrecondition().apply(this, question).exp);

		// expression = "";
		stateMap.clear();
		for (String f : stateFields)
		{
			stateMap.put(f, postStateId + "." + f);
			stateMap.put(f + "~", preStateId + "." + f);
		}

		result.add("\t /* Post conditions */");
		result.add("\t" + node.getPostcondition().apply(this, question).exp);

		result.add("}\n");

		result.add("run " + node.getName().name);
		return null;
	}

	@Override
	public AlloyPart caseAImplicitFunctionDefinition(
			AImplicitFunctionDefinition node, Context question)
			throws AnalysisException
	{
		if (node.getType().getResult() instanceof ABooleanBasicType)
		{
			String arguments = "";
			for (Iterator<APatternListTypePair> itr = node.getParamPatterns().iterator(); itr.hasNext();)
			{
				APatternListTypePair p = itr.next();
				arguments += p.getPatterns().get(0) + ": "
						+ getTypeName(p.getType());
			}

			result.add("pred " + node.getName().name + "(" + arguments + ")");
			result.add("{");

			// if (!readOnlyState.isEmpty())
			// {
			// result.add("\t /* Frame conditions */");
			// for (String id : readOnlyState)
			// {
			// result.add("\t" + postStateId + "." + id + " = " + preStateId
			// + "." + id);
			// }
			// }

			// expression = "";
			stateMap.clear();
			// for (String f : stateFields)
			// {
			// stateMap.put(f, preStateId + "." + f);
			// }

			result.add("\t /* Pre conditions */");
			result.add("\t" + node.getPrecondition().apply(this, question).exp);

			// expression = "";
			stateMap.clear();
			// for (String f : stateFields)
			// {
			// stateMap.put(f, postStateId + "." + f);
			// stateMap.put(f + "~", preStateId + "." + f);
			// }

			result.add("\t /* Post conditions */");
			result.add("\t" + node.getPostcondition().apply(this, question).exp);

			result.add("}\n");

			result.add("run " + node.getName().name);
		}
		return null;
	}

	@Override
	public AlloyPart caseAExplicitFunctionDefinition(
			AExplicitFunctionDefinition node, Context question)
			throws AnalysisException
	{
		String arguments = "";

		for (int i = 0; i < node.getType().getParameters().size(); i++)
		{
			// if (node.getParamPatternList().get(i).isEmpty())
			// {
			// return null;
			// }
			PPattern p = node.getParamPatternList().get(0).get(i);
			String pt = getTypeName(node.getType().getParameters().get(i));
			// int ii = 0;
			arguments += p + ": " + pt;
			question.variables.put(p.toString(), node.getType().getParameters().get(i));
			if (i < node.getType().getParameters().size() - 1)
			{
				arguments += ", ";
			}
		}
		if (node.getType().getResult() instanceof ABooleanBasicType)
		{
			result.add("pred " + node.getName().name + "(" + arguments + ")");
			result.add("{");

			stateMap.clear();

			result.add("\t /* Body */");
			result.add("\t" + node.getBody().apply(this, question).exp);

			result.add("}\n");
			result.add("run " + node.getName().name);
		} else
		{
			result.add("fun " + node.getName().name + "[" + arguments + "]:"
					+ getTypeName(node.getType().getResult()));
			result.add("{");

			stateMap.clear();

			result.add("\t /* Body */");
			result.add("\t" + node.getBody().apply(this, question).exp);

			result.add("}\n");
			result.add("run " + node.getName().name);
		}
		return null;
	}

	// @Override
	// public AlloyPart caseAInSetBinaryExp(AInSetBinaryExp node, Context question)
	// throws AnalysisException
	// {
	// node.getLeft().apply(this);
	// expression += " in ";
	// node.getRight().apply(this);
	// }

	public AlloyPart caseATupleExp(org.overture.ast.expressions.ATupleExp node,
			Context question) throws AnalysisException
	{
		if (node.getAncestor(AInSetBinaryExp.class) != null)
		{
			AlloyPart p = new AlloyPart();
			String predicate = "";

			boolean isLet = false;

			Sig s = question.getSig(node.getType());
			if (s.fieldNames.size() == node.getArgs().size())
			{
				String var = getNewVariableName();
				p.exp += var;

				for (int i = 0; i < node.getArgs().size(); i++)
				{

					PExp a = node.getArgs().get(i);
					AlloyPart ap = a.apply(this, question);
					if (!question.variables.containsKey(ap.exp))
					{
						isLet = true;
					}
					predicate += ap.exp + " = " + var + "."
							+ s.fieldNames.get(i);
					if (i < node.getArgs().size() - 1)
					{
						if (isLet)
						{
							predicate += ", ";
						} else
						{
							predicate += " and ";
						}
					}

				}

			} else
			{
				predicate = "/*Cannot match patterin in tuple" + node + "*/";
			}

			AlloyExp part = null;
			if (isLet)
			{
				part = new AlloyLetExp("let " + predicate + " | ");
			} else
			{
				part = new AlloyExp(predicate);
			}

			p.predicates.add(part);

			return p;
		}

		return defaultInPExp(node, question);
	};

	@Override
	public AlloyPart caseATuplePattern(ATuplePattern node, Context question)
			throws AnalysisException
	{
		ASetMultipleBind smb = null;
		if ((smb = node.getAncestor(ASetMultipleBind.class)) != null
				&& smb.getSet().getType() instanceof ANamedInvariantType)
		{
			AlloyPart p = new AlloyPart();
			String predicate = "";

			boolean isLet = false;
			Sig s = question.getSig(((ANamedInvariantType) smb.getSet().getType()).getName().name);
			if (s.fieldNames.size() == 1)
			{
				String field = s.fieldNames.iterator().next();
				// field.prefix has to be set or seq
				Sig fieldSig = question.getSig(s.getField(field).sigTypeName);

				if (fieldSig.fieldNames.size() == node.getPlist().size())
				{
					String var = getNewVariableName();
					p.exp += var;

					for (int i = 0; i < node.getPlist().size(); i++)
					{

						PPattern a = node.getPlist().get(i);
						AlloyPart ap = a.apply(this, question);
						if (!question.variables.containsKey(ap.exp))
						{
							isLet = true;
						}
						predicate += ap.exp + " = " + var + "."
								+ fieldSig.fieldNames.get(i);
						if (i < node.getPlist().size() - 1)
						{
							if (isLet)
							{
								predicate += ", ";
							} else
							{
								predicate += " and ";
							}
						}
					}
				}

			} else
			{
				predicate = "/*Cannot match patterin in tuple" + node + "*/";
			}

			AlloyExp part = null;
			if (isLet)
			{
				part = new AlloyLetExp("let " + predicate + " | ");
			} else
			{
				part = new AlloyExp(predicate);
			}

			p.predicates.add(part);

			return p;
		}
		return super.caseATuplePattern(node, question);
	}

	int varIndex = 0;

	private String getNewVariableName()
	{
		return "var" + varIndex++;
	}

	@Override
	public AlloyPart caseAVariableExp(AVariableExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart();
		String name = node.getName().name + (node.getName().isOld() ? "~" : "");
		if (stateMap.containsKey(name))
		{
			p.exp += stateMap.get(name);
		} else
		{
			p.exp += name;
		}
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

		String var = getNewVariableName();
		AlloyPart p = new AlloyPart("{ " + var);
		Sig eType = question.getSig(node.getExp().getType());
		Sig nestedT = question.getSig(eType.getField(eType.fieldNames.get(0)).sigTypeName);
		Sig nested2T = question.getSig(nestedT.getField(nestedT.fieldNames.get(0)).sigTypeName);
		p.exp += " : " + nested2T.name + " | " + var + " in (";
		p.merge(node.getExp().apply(this, question));
		p.exp += ")." + nestedT.fieldNames.get(0) + "}";
		return p;
	}

	// @Override
	// public AlloyPart caseARangeResToBinaryExp(ARangeResToBinaryExp node,
	// Context question) throws AnalysisException
	// {
	// expression += "(";
	// node.getLeft().apply(this, question);
	// expression += " :> ";
	// node.getRight().apply(this, question);
	// expression += ")";
	// }

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
		// TODO Auto-generated method stub
		return super.caseASetCompSetExp(node, question);
	}

	@Override
	public AlloyPart caseAQuoteLiteralExp(AQuoteLiteralExp node,
			Context question) throws AnalysisException
	{
		return new AlloyPart(node.getValue().value.toUpperCase());
	}

	// @Override
	// public AlloyPart caseAAndBooleanBinaryExp(AAndBooleanBinaryExp node,
	// Context question) throws AnalysisException
	// {
	// node.getLeft().apply(this, question);
	// expression += " and ";
	// node.getRight().apply(this);
	//
	// }

	@Override
	public AlloyPart defaultInINode(INode node, Context question)
			throws AnalysisException
	{
		switch (node.kindNode())
		{
			case EXP:
				return new AlloyPart(" /* NOT Translated("
						+ node.getClass().getSimpleName() + ")*/");
		}
		return null;
	}

	// @Override
	// public AlloyPart caseAEqualsBinaryExp(AEqualsBinaryExp node, Context question)
	// throws AnalysisException
	// {
	// AlloyPart p = new AlloyPart();
	// node.getLeft().apply(this, question);
	// expression += " = ";
	// node.getRight().apply(this, question);
	// }

	// @Override
	// public AlloyPart caseAPlusPlusBinaryExp(APlusPlusBinaryExp node, Context question)
	// throws AnalysisException
	// {
	// AlloyPart p = new AlloyPart();
	// p.merge(node.getLeft().apply(this, question));
	// p.exp += " ++ ";
	// p.merge(node.getRight().apply(this, question));
	// return p;
	// }

	public AlloyPart defaultSBinaryExp(SBinaryExp node, Context question)
			throws AnalysisException
	{
		AlloyPart p = new AlloyPart("(");
		p.merge(node.getLeft().apply(this, question));

		switch (node.kindSBinaryExp())
		{
			case BOOLEAN:
			{
				SBooleanBinaryExp exp = (SBooleanBinaryExp) node;
				switch (exp.kindSBooleanBinaryExp())
				{
					case AND:
						p.exp += " and ";
						break;
					case EQUIVALENT:
						p.exp += " = ";
						break;
					case IMPLIES:
						p.exp += " implies ";
						break;
					case OR:
						p.exp += " or ";
						break;

				}
			}
				break;
			case COMP:
				break;
			case DOMAINRESBY:
				break;
			case DOMAINRESTO:
				break;
			case EQUALS:
				p.exp += " = ";
				break;
			case INSET:
				// p.exp += " in ";
				throw new AnalysisException("should not go here");
			case MAPUNION:
				break;
			case NOTEQUAL:
				p.exp += " != ";
				break;
			case NOTINSET:
				break;
			case NUMERIC:
				break;
			case PLUSPLUS:
				p.exp += " ++ ";
				break;
			case PROPERSUBSET:
				break;
			case RANGERESBY:
				break;
			case RANGERESTO:
				p.exp += " :> ";
				break;
			case SEQCONCAT:
				break;
			case SETDIFFERENCE:
				break;
			case SETINTERSECT:
				p.exp += " & ";
				break;
			case SETUNION:
				break;
			case STARSTAR:
				break;
			case SUBSET:
				break;

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
		// p.merge(node.getRight().apply(this, question));
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
		// if (node.getRoot().kindPExp() == EExp.UNARY)
		// {
		// if (((SUnaryExp) node.getRoot()).kindSUnaryExp() == EUnaryExp.MAPINVERSE)
		// {
		// AMapInverseUnaryExp root = (AMapInverseUnaryExp) node.getRoot();
		//
		// node.getArgs().get(0).apply(this);
		// expression += ".(";
		// root.getExp().apply(this);
		// expression += ")";
		// return;
		// }
		// }

		if (node.getRoot().getType() instanceof SMapType
				&& node.getAncestor(AStateDefinition.class) == null)
		{
			AlloyPart p = new AlloyPart();
			p.merge(node.getArgs().get(0).apply(this, question));
			p.exp += " in (";
			p.merge(node.getRoot().apply(this, question));
			p.exp += ").univ";
			result.add("\t /*Map domain pre condition */\n\t" + p.exp);
		}

		AlloyPart p = new AlloyPart();
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
		AlloyPart p = new AlloyPart("all ");
		p.merge(node.getBindList().get(0).apply(this, question));// TODO
		p.exp += " | ";
		if (!p.predicates.isEmpty())
		{
			boolean hasLet = false;

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

		mergeReturns(p, node.getPredicate().apply(this, question));
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
				p.merge(e.apply(this, question));
				if (iterator.hasNext())
				{
					p.merge(new AlloyPart(", "));
				}
			}
		}
		p.exp += " : ";
		if (node.getSet() != null && !_visitedNodes.contains(node.getSet()))
		{
			mergeReturns(p, node.getSet().apply(this, question));
		}
		return p;
	}

	@Override
	public AlloyPart caseAIdentifierPattern(AIdentifierPattern node,
			Context question) throws AnalysisException
	{
		return new AlloyPart(node.getName().name);
	}

	// @Override
	// public AlloyPart caseAOrBooleanBinaryExp(AOrBooleanBinaryExp node,
	// Context question) throws AnalysisException
	// {
	// AlloyPart alloy = createNewReturnValue(node, question);
	// alloy.exp = "((";
	// mergeReturns(alloy, node.getLeft().apply(this, question));
	// alloy.exp += ") or (";
	// mergeReturns(alloy, node.getRight().apply(this, question));
	// alloy.exp += "))";
	// return alloy;
	// }

	@Override
	public AlloyPart mergeReturns(AlloyPart original, AlloyPart new_)
	{
		if (original == null || new_ == null)
		{
			return null;
		}
		original.exp += new_.exp;
		original.predicates.addAll(new_.predicates);
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
		if (node.getLeft().kindPExp() != EExp.TUPLE)
		{
			AlloyPart p = new AlloyPart("(");
			p.merge(node.getLeft().apply(this, question));
			p.exp += " in ";
			p.merge(node.getRight().apply(this, question));
			if (!p.predicates.isEmpty())
			{
				p.exp += " |";
			}
			p.appendPredicates();
			p.exp += ")";
			return p;
		}
		// return defaultSBinaryExp(node, question);
		AlloyPart p = new AlloyPart("(some ");
		p.merge(node.getLeft().apply(this, question));
		p.exp += " : ";
		p.merge(node.getRight().apply(this, question));
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

}
