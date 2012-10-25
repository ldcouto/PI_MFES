package org.overture.alloy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptor;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.expressions.AAndBooleanBinaryExp;
import org.overture.ast.expressions.AApplyExp;
import org.overture.ast.expressions.ADomainResByBinaryExp;
import org.overture.ast.expressions.AEqualsBinaryExp;
import org.overture.ast.expressions.AForAllExp;
import org.overture.ast.expressions.AInSetBinaryExp;
import org.overture.ast.expressions.AMapDomainUnaryExp;
import org.overture.ast.expressions.AMapEnumMapExp;
import org.overture.ast.expressions.AMapInverseUnaryExp;
import org.overture.ast.expressions.AMapletExp;
import org.overture.ast.expressions.AOrBooleanBinaryExp;
import org.overture.ast.expressions.APlusPlusBinaryExp;
import org.overture.ast.expressions.AQuoteLiteralExp;
import org.overture.ast.expressions.ARangeResToBinaryExp;
import org.overture.ast.expressions.ASetEnumSetExp;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.lex.VDMToken;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.ASetMultipleBind;
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

public class Alloy2VdmAnalysisOld extends DepthFirstAnalysisAdaptor
{
	private static final long serialVersionUID = 1L;
	public List<String> result = new Vector<String>();
	Set<INode> trnaslated = new HashSet<INode>();

	Map<String, String> stateMap = new HashMap<String, String>();
	String expression = "";
	private String moduleName;

	public Alloy2VdmAnalysisOld(String name)
	{
		this.moduleName = name;
	}

	@Override
	public void caseAModuleModules(AModuleModules node)
			throws AnalysisException
	{
		result.add("module " + moduleName + "\n");
		super.caseAModuleModules(node);
	}

	@Override
	public void caseATypeDefinition(ATypeDefinition node)
			throws AnalysisException
	{
		if (trnaslated.contains(node))
		{
			return;
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
							break;

					}
				}
					break;

				case QUOTE:

					break;

				case UNION:
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
					break;

				case SEQ:
				{
					SSeqType stype = (SSeqType) t.getType();
					createType(stype.getSeqof());
					result.add("sig "
							+ node.getName().name
							+ " {\n\t /*This should actually use the list lib*/x : set "
							+ getTypeName(stype.getSeqof()) + "\n}");
					result.add("fact " + node.getName().name + "Set"
							+ "{all c1,c2 : " + node.getName().name
							+ " | c1.x = c2.x implies c1=c2}\n");
					break;
				}

				case SET:
				{
					ASetType stype = (ASetType) t.getType();
					createType(stype.getSetof());
					result.add("sig " + node.getName().name + " {\n\tx : set "
							+ getTypeName(stype.getSetof()) + "\n}");
					result.add("fact " + node.getName().name + "Set"
							+ "{all c1,c2 : " + node.getName().name
							+ " | c1.x = c2.x implies c1=c2}\n");
					break;
				}
			}
		}
	}

	void createType(PType type)
	{
		switch (type.kindPType())
		{
			case QUOTE:
			{
				AQuoteType qt = (AQuoteType) type;
				String name = qt.getValue().value.toUpperCase();
				result.add("one sig " + name + "{}");// " extends "+node.getName().name+"{}");
				return;
			}
			case BASIC:
			{
				switch (((SBasicType) type).kindSBasicType())
				{
					case BOOLEAN:
						break;
					case CHAR:
						result.add("sig " + getTypeName(type) + " {}");
					case NUMERIC:
						break;
					case TOKEN:
						break;

				}
				return;
			}

			case SEQ:
			{
				// SSeqType stype = (SSeqType) type;
				// result.add("sig "+getTypeName(type))
				return;
			}

			case PRODUCT:
			{
				String sig = "sig " + getTypeName(type) + "{";
				int i = 0;
				for (Iterator<PType> itr = ((AProductType) type).getTypes().iterator(); itr.hasNext();)
				{
					sig += "\n\tx" + i + " : " + getTypeName(itr.next());
					if (itr.hasNext())
					{
						sig += ",";
					}
					i++;
				}
				result.add(sig + "\n}");
				return;
			}
		}
	}

	String getTypeName(PType type)
	{
		switch (type.kindPType())
		{
			case SEQ:
			{
				SSeqType stype = (SSeqType) type;
				return "SeqOf" + getTypeName(stype.getSeqof());
			}
			case SET:
			{
				SSeqType stype = (SSeqType) type;
				return "SetOf" + getTypeName(stype.getSeqof());
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
	public void caseAStateDefinition(AStateDefinition node)
			throws AnalysisException
	{
		if (trnaslated.contains(node))
		{
			return;
		}
		trnaslated.add(node);
		String name = node.getName().name;
		List<String> facts = new Vector<String>();
		result.add("\nsig " + name + "{");

		for (Iterator<AFieldField> itr = node.getFields().iterator(); itr.hasNext();)
		{
			AFieldField f = itr.next();

			if (f.getType().kindPType() == EType.MAP)
			{
				SMapType ftype = (SMapType) f.getType();
				String field = "\t" + f.getTag() + ": " + ftype.getFrom()
						+ " -> "
						+ (ftype.kindSMapType() == EMapType.MAP ? "one " : "")
						+ ftype.getTo() + (itr.hasNext() ? "," : "");
				result.add(field);
				switch (ftype.kindSMapType())
				{
					case INMAP:
						facts.add("/*" + name + "." + f.getTag()
								+ " is an INMAP*/\n\t (some fe: " + name
								+ " | ( all fs1,fs2 : fe." + f.getTag()
								+ ".univ | fe." + f.getTag() + "[fs1] = fe."
								+ f.getTag() + "[fs2] implies fs1=fs2 ))");
						break;
					case MAP:
						facts.add("/*" + name + "." + f.getTag()
								+ " is a MAP*/\n\t (some fe: " + name
								+ " | ( all fs1,fs2 : fe." + f.getTag()
								+ ".univ | fs1=fs2  implies fe." + f.getTag()
								+ "[fs1] = fe." + f.getTag() + "[fs2] ))");
						break;

				}
			}
		}

		result.add("}");
		// INV
		result.add("{");
		expression = "";
		stateMap.clear();
		for (AFieldField f : node.getFields())
		{
			stateMap.put(f.getTag(), f.getTag());
			// stateMap.put(f + "~", preStateId + "." + f);
		}
		node.getInvExpression().apply(this);
		result.add("\t" + expression);
		result.add("}");

		result.add("fact{");
		for (Iterator<String> itr = facts.iterator(); itr.hasNext();)
		{
			result.add("\t" + itr.next());
			if (itr.hasNext())
			{
				result.add("\tand");
			}
		}
		result.add("}\n");
	}

	private String toList(List<String> quotes, String seperator)
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
	public void caseAImplicitOperationDefinition(
			AImplicitOperationDefinition node) throws AnalysisException
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

		expression = "";
		stateMap.clear();
		for (String f : stateFields)
		{
			stateMap.put(f, preStateId + "." + f);
		}
		node.getPrecondition().apply(this);
		result.add("\t /* Pre conditions */");
		result.add("\t" + expression);

		expression = "";
		stateMap.clear();
		for (String f : stateFields)
		{
			stateMap.put(f, postStateId + "." + f);
			stateMap.put(f + "~", preStateId + "." + f);
		}
		node.getPostcondition().apply(this);
		result.add("\t /* Post conditions */");
		result.add("\t" + expression);

		result.add("}\n");

		result.add("run " + node.getName().name);
	}

	@Override
	public void caseAImplicitFunctionDefinition(AImplicitFunctionDefinition node)
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

			expression = "";
			stateMap.clear();
			// for (String f : stateFields)
			// {
			// stateMap.put(f, preStateId + "." + f);
			// }
			node.getPrecondition().apply(this);
			result.add("\t /* Pre conditions */");
			result.add("\t" + expression);

			expression = "";
			stateMap.clear();
			// for (String f : stateFields)
			// {
			// stateMap.put(f, postStateId + "." + f);
			// stateMap.put(f + "~", preStateId + "." + f);
			// }
			node.getPostcondition().apply(this);
			result.add("\t /* Post conditions */");
			result.add("\t" + expression);

			result.add("}\n");

			result.add("run " + node.getName().name);
		}
	}

	@Override
	public void caseAExplicitFunctionDefinition(AExplicitFunctionDefinition node)
			throws AnalysisException
	{
		if (node.getType().getResult() instanceof ABooleanBasicType)
		{
			String arguments = "";

			for (int i = 0; i < node.getParamPatternList().size(); i++)
			{
				if (node.getParamPatternList().get(i).isEmpty())
				{
					return;
				}
				PPattern p = node.getParamPatternList().get(i).get(0);
				String pt = getTypeName(node.getType().getParameters().get(i));
				// int ii = 0;
				arguments += p + ": " + pt;
				if (i < node.getParamPatternList().size() - 1)
				{
					arguments += ", ";
				}
			}

			result.add("pred " + node.getName().name + "(" + arguments + ")");
			result.add("{");

			expression = "";
			stateMap.clear();
			node.getBody().apply(this);
			result.add("\t /* Body */");
			result.add("\t" + expression);

			result.add("}\n");

			result.add("run " + node.getName().name);
		}
	}

	@Override
	public void caseAInSetBinaryExp(AInSetBinaryExp node)
			throws AnalysisException
	{
		node.getLeft().apply(this);
		expression += " in ";
		node.getRight().apply(this);
	}

	@Override
	public void caseAVariableExp(AVariableExp node) throws AnalysisException
	{
		String name = node.getName().name + (node.getName().isOld() ? "~" : "");
		if (stateMap.containsKey(name))
		{
			expression += stateMap.get(name);
		} else
		{
			expression += name;
		}
	}

	@Override
	public void caseAMapDomainUnaryExp(AMapDomainUnaryExp node)
			throws AnalysisException
	{
		expression += "(";
		node.getExp().apply(this);
		expression += ").univ";
	}

	@Override
	public void caseARangeResToBinaryExp(ARangeResToBinaryExp node)
			throws AnalysisException
	{
		expression += "(";
		node.getLeft().apply(this);
		expression += " :> ";
		node.getRight().apply(this);
		expression += ")";
	}

	@Override
	public void caseASetEnumSetExp(ASetEnumSetExp node)
			throws AnalysisException
	{
		expression += "(";
		for (Iterator<PExp> itr = node.getMembers().iterator(); itr.hasNext();)
		{
			itr.next().apply(this);
			if (itr.hasNext())
			{
				expression += " + ";
			}

		}
		expression += ")";
	}

	@Override
	public void caseAQuoteLiteralExp(AQuoteLiteralExp node)
			throws AnalysisException
	{
		expression += node.getValue().value.toUpperCase();
	}

	@Override
	public void caseAAndBooleanBinaryExp(AAndBooleanBinaryExp node)
			throws AnalysisException
	{
		node.getLeft().apply(this);
		expression += " and ";
		node.getRight().apply(this);

	}

	@Override
	public void defaultInINode(INode node) throws AnalysisException
	{
		switch (node.kindNode())
		{
			case EXP:
				expression += " /* NOT Translated("
						+ node.getClass().getSimpleName() + ")*/";
				break;
		}
	}

	@Override
	public void caseAEqualsBinaryExp(AEqualsBinaryExp node)
			throws AnalysisException
	{
		node.getLeft().apply(this);
		expression += " = ";
		node.getRight().apply(this);
	}

	@Override
	public void caseAPlusPlusBinaryExp(APlusPlusBinaryExp node)
			throws AnalysisException
	{
		node.getLeft().apply(this);
		expression += " ++ ";
		node.getRight().apply(this);
	}

	@Override
	public void caseAMapEnumMapExp(AMapEnumMapExp node)
			throws AnalysisException
	{
		expression += "(";

		for (Iterator<AMapletExp> itr = node.getMembers().iterator(); itr.hasNext();)
		{
			AMapletExp maplet = itr.next();

			maplet.getLeft().apply(this);
			expression += " -> ";
			maplet.getRight().apply(this);
			if (itr.hasNext())
			{
				expression += " + ";
			}
		}

		expression += ")";
	}

	@Override
	public void caseADomainResByBinaryExp(ADomainResByBinaryExp node)
			throws AnalysisException
	{
		expression += "(";
		node.getRight().apply(this);
		expression += ".univ -";
		node.getLeft().apply(this);
		expression += ")";
		expression += " <: ";
		node.getRight().apply(this);
	}

	@Override
	public void caseAApplyExp(AApplyExp node) throws AnalysisException
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
			String tmp = expression;
			expression = "";
			node.getArgs().get(0).apply(this);
			expression += " in (";
			node.getRoot().apply(this);
			expression += ").univ";
			result.add("\t /*Map domain pre condition */\n\t" + expression);
			expression = tmp;
		}

		node.getRoot().apply(this);
		expression += "[";
		for (Iterator<PExp> itr = node.getArgs().iterator(); itr.hasNext();)
		{
			itr.next().apply(this);
			if (itr.hasNext())
			{
				expression += ", ";
			}
		}
		expression += "] ";
	}

	@Override
	public void caseAMapInverseUnaryExp(AMapInverseUnaryExp node)
			throws AnalysisException
	{
		// Only supported if directly before an apply
		expression += "~(";
		node.getExp().apply(this);
		expression += ")";

	}

	@Override
	public void caseAForAllExp(AForAllExp node) throws AnalysisException
	{
		expression += "all ";
		node.getBindList().get(0).apply(this);// TODO
		expression += " | ";
		node.getPredicate().apply(this);
	}

	@Override
	public void caseASetMultipleBind(ASetMultipleBind node)
			throws AnalysisException
	{

		{
			List<PPattern> copy = new ArrayList<PPattern>(node.getPlist());
			for (PPattern e : copy)
			{
				if (!_visitedNodes.contains(e))
				{
					e.apply(this);
				}
			}
		}
		expression += " : ";
		if (node.getSet() != null && !_visitedNodes.contains(node.getSet()))
		{
			node.getSet().apply(this);
		}

	}

	@Override
	public void caseAIdentifierPattern(AIdentifierPattern node)
			throws AnalysisException
	{
		expression += node.getName().name;
	}

	@Override
	public void caseAOrBooleanBinaryExp(AOrBooleanBinaryExp node)
			throws AnalysisException
	{
		expression += "((";
		node.getLeft().apply(this);
		expression += ") or (";
		node.getRight().apply(this);
		expression += "))";
	}
}
