/**
 * Gui2InfoFactory.java
 * Copyright James Dempsey, 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 07/02/2011 7:13:32 PM
 *
 * $Id$
 */
package pcgen.gui2.facade;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import pcgen.base.formula.Formula;
import pcgen.base.lang.StringUtil;
import pcgen.base.util.Indirect;
import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.CDOMReference;
import pcgen.cdom.base.ChooseInformation;
import pcgen.cdom.base.Constants;
import pcgen.cdom.content.CNAbility;
import pcgen.cdom.content.CNAbilityFactory;
import pcgen.cdom.content.HitDie;
import pcgen.cdom.content.LevelCommandFactory;
import pcgen.cdom.content.fact.FactDefinition;
import pcgen.cdom.content.factset.FactSetDefinition;
import pcgen.cdom.enumeration.AspectName;
import pcgen.cdom.enumeration.FactKey;
import pcgen.cdom.enumeration.FactSetKey;
import pcgen.cdom.enumeration.FormulaKey;
import pcgen.cdom.enumeration.IntegerKey;
import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.enumeration.MapKey;
import pcgen.cdom.enumeration.Nature;
import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.enumeration.RaceSubType;
import pcgen.cdom.enumeration.RaceType;
import pcgen.cdom.enumeration.SourceFormat;
import pcgen.cdom.enumeration.StringKey;
import pcgen.cdom.helper.Aspect;
import pcgen.cdom.reference.ReferenceUtilities;
import pcgen.cdom.util.ControlUtilities;
import pcgen.core.Ability;
import pcgen.core.BenefitFormatting;
import pcgen.core.BonusManager.TempBonusInfo;
import pcgen.core.Deity;
import pcgen.core.Domain;
import pcgen.core.Equipment;
import pcgen.core.EquipmentModifier;
import pcgen.core.Globals;
import pcgen.core.Kit;
import pcgen.core.Movement;
import pcgen.core.PCClass;
import pcgen.core.PCStat;
import pcgen.core.PCTemplate;
import pcgen.core.PlayerCharacter;
import pcgen.core.Race;
import pcgen.core.SettingsHandler;
import pcgen.core.Skill;
import pcgen.core.SpecialProperty;
import pcgen.core.SubClass;
import pcgen.core.WeaponProf;
import pcgen.core.analysis.BonusCalc;
import pcgen.core.analysis.OutputNameFormatting;
import pcgen.core.analysis.SkillInfoUtilities;
import pcgen.core.bonus.BonusObj;
import pcgen.core.character.CharacterSpell;
import pcgen.core.character.SpellBook;
import pcgen.core.character.SpellInfo;
import pcgen.core.character.WieldCategory;
import pcgen.core.display.CharacterDisplay;
import pcgen.core.display.DescriptionFormatting;
import pcgen.core.display.SkillCostDisplay;
import pcgen.core.display.TemplateModifier;
import pcgen.core.display.VisionDisplay;
import pcgen.core.kit.BaseKit;
import pcgen.core.prereq.PrerequisiteUtilities;
import pcgen.core.spell.Spell;
import pcgen.facade.core.AbilityFacade;
import pcgen.facade.core.ClassFacade;
import pcgen.facade.core.DeityFacade;
import pcgen.facade.core.DomainFacade;
import pcgen.facade.core.EquipModFacade;
import pcgen.facade.core.EquipmentFacade;
import pcgen.facade.core.InfoFacade;
import pcgen.facade.core.InfoFactory;
import pcgen.facade.core.KitFacade;
import pcgen.facade.core.RaceFacade;
import pcgen.facade.core.SkillFacade;
import pcgen.facade.core.SpellFacade;
import pcgen.facade.core.TempBonusFacade;
import pcgen.facade.core.TemplateFacade;
import pcgen.gui2.util.HtmlInfoBuilder;
import pcgen.io.exporttoken.EqToken;
import pcgen.io.exporttoken.WeaponToken;
import pcgen.rules.context.LoadContext;
import pcgen.system.LanguageBundle;
import pcgen.system.PCGenSettings;
import pcgen.util.Delta;
import pcgen.util.Logging;
import pcgen.util.enumeration.Tab;
import pcgen.util.enumeration.View;

/**
 * The Class <code>Gui2InfoFactory</code> provides character related information 
 * on various facade objects. The information is displayed to the user via the 
 * new user interface. 
 *
 * <br>
 * Last Editor: $Author$
 * Last Edited: $Date$
 * 
 * @author James Dempsey &lt;jdempsey@users.sourceforge.net&gt;
 * @version $Revision$
 */
public class Gui2InfoFactory implements InfoFactory
{
	/** A default return value for an invalid request. */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static NumberFormat ADJ_FMT = new DecimalFormat("+0;-0"); //$NON-NLS-1$
	private static NumberFormat COST_FMT = new DecimalFormat("#,##0.##"); //$NON-NLS-1$

	/** Constant for 2 spaces in HTML */
	public static final String TWO_SPACES = " &nbsp;"; //$NON-NLS-1$
	/** Constant for HTML bold start tag */
	public static final String BOLD = "<b>"; //$NON-NLS-1$
	/** Constant for HTML bold end tag */
	public static final String END_BOLD = "</b>"; //$NON-NLS-1$

	private final PlayerCharacter pc;
	private final CharacterDisplay charDisplay;
	
	/**
	 * Create a new Gui2InfoFactory instance for the character.
	 * @param pc The character
	 */
	public Gui2InfoFactory(PlayerCharacter pc)
	{
		this.pc = pc;
		this.charDisplay = pc ==  null ? null : pc.getDisplay();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getFavoredClass(pcgen.core.facade.RaceFacade)
	 */
	@Override
	public String getFavoredClass(RaceFacade race)
	{
		if (!(race instanceof Race))
		{
			return EMPTY_STRING;
		}
		String[] favClass =
				Globals.getContext().unparseSubtoken((Race) race, "FAVCLASS");
		return StringUtil.join(favClass, ", ");
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.RaceFacade)
	 */
	@Override
	public String getHTMLInfo(RaceFacade raceFacade)
	{
		if (!(raceFacade instanceof Race))
		{
			return EMPTY_STRING;
		}
		Race race = (Race) raceFacade;
		
		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();

		if (!race.getKeyName().startsWith("<none"))
		{
			infoText.appendTitleElement(OutputNameFormatting.piString(race, false));

			infoText.appendLineBreak();
			RaceType rt = race.get(ObjectKey.RACETYPE);
			if (rt != null)
			{
				infoText.appendI18nElement("in_irInfoRaceType", rt.toString()); //$NON-NLS-1$
			}

			List<RaceSubType> rst = race.getListFor(ListKey.RACESUBTYPE);
			if (rst != null)
			{
				infoText.appendSpacer();
				infoText.appendI18nElement("in_irInfoSubType", StringUtil.join(rst, ", ")); //$NON-NLS-1$
			}
			if (race.getType().length() > 0)
			{
				infoText.appendSpacer();
				infoText.appendI18nElement("in_irInfoType", race.getType()); //$NON-NLS-1$
			}

			appendFacts(infoText, race);

			infoText.appendLineBreak();
			String size = race.getSize();
			if (StringUtils.isNotEmpty(size))
			{
				infoText.appendI18nElement("in_size", size); //$NON-NLS-1$
			}
			String movement = getMovement(raceFacade);
			if (movement.length() > 0)
			{
				infoText.appendSpacer();
				infoText.appendI18nElement("in_movement", movement); //$NON-NLS-1$
			}
			String vision = getVision(raceFacade);
			if (vision.length() > 0)
			{
				infoText.appendSpacer();
				infoText.appendI18nElement("in_vision", vision); //$NON-NLS-1$
			}

			String bString = PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
					race.getPrerequisiteList(), false);
			if (bString.length() > 0)
			{
				infoText.appendLineBreak();
				infoText.appendI18nElement("in_requirements", bString); //$NON-NLS-1$
			}
			String desc = pc.getDescription(race);
			if (desc.length() > 0)
			{
				infoText.appendLineBreak();
				infoText.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
						DescriptionFormatting.piWrapDesc(race, desc, false));
			}
			
			String statAdjustments = getStatAdjustments(raceFacade);
			if (StringUtils.isNotEmpty(statAdjustments))
			{
				infoText.appendLineBreak();
				infoText.appendI18nElement("in_irTableStat", statAdjustments); //$NON-NLS-1$
			}
			
			LevelCommandFactory levelCommandFactory = race.get(ObjectKey.MONSTER_CLASS);
			if (levelCommandFactory != null)
			{
				infoText.appendLineBreak();
				infoText.appendI18nFormattedElement("in_irInfoMonsterClass", //$NON-NLS-1$
					String.valueOf(levelCommandFactory.getLevelCount()),
					OutputNameFormatting.piString(levelCommandFactory.getPCClass(), false));
				
			}
			String favoredClass = getFavoredClass(raceFacade);
			if (StringUtils.isNotEmpty(favoredClass))
			{
				infoText.appendLineBreak();
				infoText.appendI18nElement("in_favoredClass", favoredClass); //$NON-NLS-1$
			}
			bString = race.getSource();
			if (bString.length() > 0)
			{
				infoText.appendLineBreak();
				infoText.appendI18nElement("in_sourceLabel", bString); //$NON-NLS-1$
			}
		}

		return infoText.toString();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.ClassFacade, pcgen.core.facade.ClassFacade)
	 */
	@Override
	public String getHTMLInfo(ClassFacade classFacade,
		ClassFacade parentClassFacade)
	{
		if (!(classFacade instanceof PCClass))
		{
			return EMPTY_STRING;
		}
		PCClass aClass = (PCClass) classFacade;
		PCClass parentClass = aClass;

		String aString;
		boolean isSubClass = aClass instanceof SubClass;
		if (isSubClass && parentClassFacade != null)
		{
			parentClass = (PCClass) parentClassFacade;
		}

		final HtmlInfoBuilder b =
				new HtmlInfoBuilder(OutputNameFormatting
					.piString(aClass, false));
		b.appendLineBreak();

		// Subclass cost - at the top to make choices easier
		if (isSubClass && aClass.getSafe(IntegerKey.COST) != 0)
		{
			b.appendI18nElement("in_clInfoCost", String.valueOf(aClass.getSafe(IntegerKey.COST))); //$NON-NLS-1$
			b.appendLineBreak();
		}
		
		// Type
		aString = aClass.getType();
		if (isSubClass && (aString.length() == 0))
		{
			aString = parentClass.getType();
		}
		b.appendI18nElement("in_clInfoType", aString); //$NON-NLS-1$

		// Hit Die
		HitDie hitDie = aClass.getSafe(ObjectKey.LEVEL_HITDIE);
		if (isSubClass && HitDie.ZERO.equals(hitDie))
		{
			hitDie = parentClass.getSafe(ObjectKey.LEVEL_HITDIE);
		}
		if (!HitDie.ZERO.equals(hitDie))
		{
			b.appendSpacer();
			b.appendI18nElement("in_clInfoHD", "d" + hitDie.getDie()); //$NON-NLS-1$  //$NON-NLS-2$
		}

		appendFacts(b, aClass);

		if (SettingsHandler.getGame().getTabShown(Tab.SPELLS))
		{
			FactKey<String> fk = FactKey.valueOf("SpellType");
			aString = aClass.getResolved(fk);

			if (isSubClass && aString == null)
			{
				aString = parentClass.getSpellType();
			}

			b.appendSpacer();
			b.appendI18nElement("in_clInfoSpellType", aString); //$NON-NLS-1$

			aString = aClass.getSpellBaseStat();

			/*
			 * CONSIDER This test here is the ONLY place where the "magical"
			 * value of null is tested for in getSpellBaseStat(). This is
			 * currently set by SubClass and SubstititionClass, so it IS
			 * used, but the question is: Is there a better method for
			 * identifying this special deferral to the "parentClass" other
			 * than null SpellBaseStat? - thpr 11/9/06
			 */
			if (isSubClass && ((aString == null) || (aString.length() == 0)))
			{
				aString = parentClass.getSpellBaseStat();
			}

			b.appendSpacer();
			b.appendI18nElement("in_clInfoBaseStat", aString); //$NON-NLS-1$
		}

		// Prereqs
		aString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, null, aClass
					.getPrerequisiteList(), false);
		if (isSubClass && (aString.length() == 0))
		{
			aString =
					PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
						parentClass.getPrerequisiteList(), false);
		}
		if (aString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_requirements", aString); //$NON-NLS-1$
		}
		//Description
		String desc = pc.getDescription(aClass);
		if (desc.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
					DescriptionFormatting.piWrapDesc(aClass, desc, false));
		}
		// Sub class extra info
		if (isSubClass)
		{
			int specialtySpells = aClass.getSafe(IntegerKey.KNOWN_SPELLS_FROM_SPECIALTY);
			b.appendLineBreak();
			b.appendI18nElement("in_clSpecialtySpells", Delta.toString(specialtySpells)); //$NON-NLS-1$
			b.appendSpacer();
			b.appendI18nElement("in_clSpecialty", ((SubClass) aClass).getChoice()); //$NON-NLS-1$
		}
		
		// Source
		aString = aClass.getSource();
		if (isSubClass && (aString.length() == 0))
		{
			aString = parentClass.getSource();
		}
		if (aString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_source", aString); //$NON-NLS-1$
		}

		return b.toString();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.SkillFacade)
	 */
	@Override
	public String getHTMLInfo(SkillFacade skillFacade)
	{
		if (!(skillFacade instanceof Skill))
		{
			return EMPTY_STRING;
		}
		Skill skill = (Skill) skillFacade;

		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();

		infoText.appendTitleElement(OutputNameFormatting.piString(skill, false));

		infoText.appendLineBreak();
		String typeString = StringUtil.join(skill.getTrueTypeList(true), ". ");
		if (StringUtils.isNotBlank(typeString))
		{
			infoText.appendI18nElement("in_igInfoLabelTextType", //$NON-NLS-1$
				typeString);
			infoText.appendLineBreak();
		}

		appendFacts(infoText, skill);

		String aString = SkillInfoUtilities.getKeyStatFromStats(pc, skill);
		if (aString.length() != 0)
		{
			infoText.appendI18nElement("in_iskKEY_STAT", aString); //$NON-NLS-1$
		}
		infoText.appendLineBreak();
		infoText.appendI18nElement("in_iskUntrained", //$NON-NLS-1$
			skill.getSafe(ObjectKey.USE_UNTRAINED) ? LanguageBundle
				.getString("in_yes") : LanguageBundle.getString("in_no"));
		infoText.appendLineBreak();
		infoText.appendI18nElement("in_iskEXCLUSIVE", //$NON-NLS-1$
			skill.getSafe(ObjectKey.EXCLUSIVE) ? LanguageBundle
				.getString("in_yes") : LanguageBundle.getString("in_no"));

		String bString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, null, skill
					.getPrerequisiteList(), false);
		if (bString.length() > 0)
		{
			infoText.appendI18nFormattedElement("in_InfoRequirements", //$NON-NLS-1$
				bString);
		}

		//Description
		String desc = pc.getDescription(skill);
		if (desc.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
					DescriptionFormatting.piWrapDesc(skill, desc, false));
		}
		
		bString = skill.getSource();
		if (bString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_iskSource", bString); //$NON-NLS-1$
		}

		if (PCGenSettings.OPTIONS_CONTEXT.getBoolean(
			PCGenSettings.OPTION_SHOW_SKILL_MOD_BREAKDOWN, false))
		{
			bString = SkillCostDisplay.getModifierExplanation(skill, pc, false);
			if (bString.length() != 0)
			{
				infoText.appendLineBreak();
				infoText.appendI18nFormattedElement("in_iskHtml_PcMod", //$NON-NLS-1$
					bString);
			}
		}

		if (PCGenSettings.OPTIONS_CONTEXT.getBoolean(
			PCGenSettings.OPTION_SHOW_SKILL_RANK_BREAKDOWN, false))
		{
			bString = SkillCostDisplay.getRanksExplanation(pc, skill);
			if (bString.length() == 0)
			{
				bString = LanguageBundle.getString("in_none"); //$NON-NLS-1$
			}
			infoText.appendLineBreak();
			infoText.appendI18nFormattedElement("in_iskHtml_Ranks", //$NON-NLS-1$
				bString);
		}

		return infoText.toString();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.AbilityFacade)
	 */
	@Override
	public String getHTMLInfo(AbilityFacade abilityFacade)
	{
		if (!(abilityFacade instanceof Ability))
		{
			return EMPTY_STRING;
		}
		Ability ability = (Ability) abilityFacade;

		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();
		infoText.appendTitleElement(OutputNameFormatting.piString(ability, false));
		infoText.appendLineBreak();

		infoText.appendI18nFormattedElement("Ability.Info.Type", //$NON-NLS-1$
			StringUtil.join(ability.getTrueTypeList(true), ". ")); //$NON-NLS-1$

		BigDecimal costStr = ability.getSafe(ObjectKey.SELECTION_COST);
		if (!costStr.equals(BigDecimal.ONE)) //$NON-NLS-1$
		{
			infoText.appendI18nFormattedElement("Ability.Info.Cost", //$NON-NLS-1$
				COST_FMT.format(costStr));
		}

		if (ability.getSafe(ObjectKey.MULTIPLE_ALLOWED))
		{
			infoText.appendSpacer();
			infoText.append(LanguageBundle.getString("Ability.Info.Multiple")); //$NON-NLS-1$
		}

		if (ability.getSafe(ObjectKey.STACKS))
		{
			infoText.appendSpacer();
			infoText.append(LanguageBundle.getString("Ability.Info.Stacks")); //$NON-NLS-1$
		}

		appendFacts(infoText, ability);

		final String cString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
					ability.getPrerequisiteList(), false);
		if (cString.length() > 0)
		{
			infoText.appendI18nFormattedElement("in_InfoRequirements", //$NON-NLS-1$
				cString);
		}

		infoText.appendLineBreak();
		infoText.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
			getDescription(abilityFacade));

		List<CNAbility> wrappedAbility = getWrappedAbility(ability);

		if (ability.getSafeSizeOfMapFor(MapKey.ASPECT) > 0)
		{
			Set<AspectName> aspectKeys = ability.getKeysFor(MapKey.ASPECT);
			StringBuilder buff = new StringBuilder(100);
			for (AspectName key : aspectKeys)
			{
				if (buff.length() > 0)
				{
					buff.append(", ");
				}
				//Assert here that the actual text displayed is not critical
				buff.append(Aspect.printAspect(pc, key, wrappedAbility));
			}
			infoText.appendLineBreak();
			infoText.appendI18nFormattedElement("Ability.Info.Aspects", //$NON-NLS-1$
				buff.toString());
		}
		
		final String bene = BenefitFormatting.getBenefits(pc, wrappedAbility);
		if (bene != null && bene.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nFormattedElement("Ability.Info.Benefit", //$NON-NLS-1$
				BenefitFormatting.getBenefits(pc, wrappedAbility));
		}

		infoText.appendLineBreak();
		infoText.appendI18nFormattedElement("in_InfoSource", //$NON-NLS-1$
			ability.getSource());

		return infoText.toString();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.DeityFacade)
	 */
	@Override
	public String getHTMLInfo(DeityFacade deityFacade)
	{
		if (!(deityFacade instanceof Deity))
		{
			return EMPTY_STRING;
		}
		Deity aDeity = (Deity) deityFacade;
		
		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();
		if (aDeity != null)
		{
			infoText.appendTitleElement(OutputNameFormatting.piString(aDeity, false));
			infoText.appendLineBreak();

			infoText
				.appendI18nFormattedElement(
					"in_InfoDescription", DescriptionFormatting.piWrapDesc(aDeity, pc.getDescription(aDeity), false)); //$NON-NLS-1$
			
			appendFacts(infoText, aDeity);

			String aString = getPantheons(aDeity);
			if (aString != null)
			{
				infoText.appendSpacer();
				infoText.appendI18nElement(
						"in_pantheon", aString); //$NON-NLS-1$
			}

			infoText.appendSpacer();
			infoText.appendI18nElement(
				"in_domains", getDomains(aDeity)); //$NON-NLS-1$

			List<CDOMReference<WeaponProf>> dwp = aDeity.getListFor(
					ListKey.DEITYWEAPON);
			if (dwp != null)
			{
				infoText.appendSpacer();
				infoText.appendI18nFormattedElement(
					"in_deityFavWeap", //$NON-NLS-1$
					ReferenceUtilities.joinLstFormat(dwp, "|"));
			}

			aString = PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
			aDeity.getPrerequisiteList(), false);
			if (aString.length() != 0)
			{
				infoText.appendSpacer();
				infoText.appendI18nFormattedElement("in_InfoRequirements", //$NON-NLS-1$
					aString);
			}

			aString = aDeity.getSource();
			if (aString.length() > 0)
			{
				infoText.appendSpacer();
				infoText.appendI18nFormattedElement("in_InfoSource", //$NON-NLS-1$
					aString);
			}

		}
		return infoText.toString();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.DomainFacade)
	 */
	@Override
	public String getHTMLInfo(DomainFacade domainFacade)
	{
		if (!(domainFacade instanceof DomainFacadeImpl))
		{
			return EMPTY_STRING;
		}
		DomainFacadeImpl domainFI = (DomainFacadeImpl) domainFacade;
		Domain aDomain = domainFI.getRawObject();
		
		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();

		if (aDomain != null)
		{
			infoText.appendTitleElement(OutputNameFormatting.piString(aDomain, false));

			appendFacts(infoText, aDomain);

			String aString = pc.getDescription(aDomain);
			if (aString.length() != 0)
			{
				infoText.appendLineBreak();
				infoText.appendI18nFormattedElement("in_domainGrant", //$NON-NLS-1$
					aString);
			}

			aString =
					PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
						aDomain.getPrerequisiteList(), false);
			if (aString.length() != 0)
			{
				infoText.appendI18nFormattedElement("in_InfoRequirements", //$NON-NLS-1$
					aString);
			}
			
			aString =
					PrerequisiteUtilities.preReqHTMLStringsForList(pc, aDomain,
						domainFI.getPrerequisiteList(), false);
			if (aString.length() != 0)
			{
				infoText.appendLineBreak();
				infoText.appendI18nFormattedElement(
					"in_domainRequirements", //$NON-NLS-1$
					aString);
			}

			aString =
					SourceFormat.getFormattedString(aDomain, Globals
						.getSourceDisplay(), true);
			if (aString.length() > 0)
			{
				infoText.appendI18nFormattedElement("in_InfoSource", //$NON-NLS-1$
					aString);
			}
			

		}

		return infoText.toString();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.EquipmentFacade)
	 */
	@Override
	public String getHTMLInfo(EquipmentFacade equipFacade)
	{
		if (equipFacade == null || !(equipFacade instanceof Equipment))
		{
			return EMPTY_STRING;
		}
		
		Equipment equip = (Equipment) equipFacade;

		final HtmlInfoBuilder b = getEquipmentHtmlInfo(equip);

		String bString = equip.getSource();
		if (bString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextSource", bString); //$NON-NLS-1$
		}
		b.appendLineBreak();

		return b.toString();
	}

	private HtmlInfoBuilder getEquipmentHtmlInfo(Equipment equip)
	{
		final StringBuilder title = new StringBuilder(50);
		title.append(OutputNameFormatting.piString(equip, false));

		if (!equip.longName().equals(equip.getName()))
		{
			title.append("(").append(equip.longName()).append(")");
		}

		final HtmlInfoBuilder b = new HtmlInfoBuilder(null, false);
		File icon = equip.getIcon();
		if (icon != null)
		{
			b.appendIconElement(icon.toURI().toString());
		}
		b.appendTitleElement(title.toString());
		b.appendLineBreak();

		String baseName = equip.getBaseItemName();
		if (StringUtils.isNotEmpty(baseName) && !baseName.equals(equip.getName()))
		{
			b.appendI18nElement("in_igInfoLabelTextBaseItem", //$NON-NLS-1$
				baseName);
			b.appendLineBreak();
		}
		
		b.appendI18nElement("in_igInfoLabelTextType", //$NON-NLS-1$
			StringUtil.join(equip.getTrueTypeList(true), ". "));

		appendFacts(b, equip);
		
		//
		// Should only be meaningful for weapons, but if included on some other piece of
		// equipment, show it anyway
		//
		if (equip.isWeapon() || equip.get(ObjectKey.WIELD) != null)
		{
			b.appendLineBreak();
			final WieldCategory wCat = equip.getEffectiveWieldCategory(pc);
			if (wCat != null)
			{
				b.appendI18nElement("in_igInfoLabelTextWield", //$NON-NLS-1$
					wCat.getDisplayName());
			}
		}

		//
		// Only meaningful for weapons, armor and shields
		//
		if (equip.isWeapon() || equip.isArmor() || equip.isShield())
		{
			b.appendLineBreak();
			final String value =
					(pc.isProficientWith(equip) && equip.meetsPreReqs(pc))
						? LanguageBundle.getString("in_igInfoLabelTextYes") //$NON-NLS-1$
						: (SettingsHandler.getPrereqFailColorAsHtmlStart()
							+ LanguageBundle.getString("in_igInfoLabelTextNo") + //$NON-NLS-1$
						SettingsHandler.getPrereqFailColorAsHtmlEnd());
			b.appendI18nElement("in_igInfoLabelTextProficient", value); //$NON-NLS-1$
		}

		final String cString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, null, equip
					.getPrerequisiteList(), false);

		if (cString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextReq", cString); //$NON-NLS-1$
		}

		BigDecimal cost = equip.getCost(pc);
		if (cost != BigDecimal.ZERO)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igEqModelColCost", COST_FMT.format(cost.doubleValue())); //$NON-NLS-1$
			b.append(" ");
			b.append(SettingsHandler.getGame().getCurrencyDisplay());
		}
		
		String bString =
				Globals.getGameModeUnitSet().displayWeightInUnitSet(
					equip.getWeight(pc).doubleValue());

		if (bString.length() > 0)
		{
			b.appendLineBreak();
			bString += Globals.getGameModeUnitSet().getWeightUnit();
			b.appendI18nElement("in_igInfoLabelTextWeight", bString); //$NON-NLS-1$

		}

		Integer a = EqToken.getMaxDexTokenInt(pc, equip);

		if (a.intValue() != Constants.MAX_MAXDEX)
		{
			b.appendSpacer();
			b.appendI18nElement("in_igInfoLabelTextMaxDex", a.toString()); //$NON-NLS-1$
		}

		a = equip.acCheck(pc);

		if (equip.isArmor() || equip.isShield() || (a.intValue() != 0))
		{
			b.appendSpacer();
			b.appendI18nElement("in_igInfoLabelTextAcCheck", a.toString()); //$NON-NLS-1$
		}

		if (SettingsHandler.getGame().getACText().length() != 0)
		{
			a = equip.getACBonus(pc);

			if (equip.isArmor() || equip.isShield() || (a.intValue() != 0))
			{
				b.appendSpacer();
				b.appendElement(LanguageBundle.getFormattedString(
					"in_igInfoLabelTextAcBonus", //$NON-NLS-1$
					SettingsHandler.getGame().getACText()), a.toString());
			}
		}

		if (SettingsHandler.getGame().getTabShown(Tab.SPELLS))
		{
			a = EqToken.getSpellFailureTokenInt(pc, equip);

			if (equip.isArmor() || equip.isShield() || (a.intValue() != 0))
			{
				b.appendSpacer();
				b.appendI18nElement(
					"in_igInfoLabelTextArcaneFailure", a.toString()); //$NON-NLS-1$
			}
		}

		bString = SettingsHandler.getGame().getDamageResistanceText();

		if (bString.length() != 0)
		{
			a = EqToken.getEdrTokenInt(pc, equip);

			if (equip.isArmor() || equip.isShield() || (a.intValue() != 0))
			{
				b.appendSpacer();
				b.appendElement(bString, a.toString());
			}
		}

		bString = equip.moveString();

		if (bString.length() > 0)
		{
			b.appendSpacer();
			b.appendI18nElement("in_igInfoLabelTextMove", bString); //$NON-NLS-1$
		}

		bString = equip.getSize();

		if (bString.length() > 0)
		{
			b.appendSpacer();
			b.appendI18nElement("in_igInfoLabelTextSize", bString); //$NON-NLS-1$
		}

		bString = equip.getDamage(pc);

		if (bString.length() > 0)
		{

			if (equip.isDouble())
			{
				bString += "/" + equip.getAltDamage(pc); //$NON-NLS-1$
			}

			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextDamage", bString); //$NON-NLS-1$
		}

		String critRangeVar =
				ControlUtilities.getControlToken(Globals.getContext(),
					"CRITRANGE");
		if (critRangeVar == null)
		{
			int critrange = EqToken.getOldBonusedCritRange(pc, equip, true);
			int altcritrange = EqToken.getOldBonusedCritRange(pc, equip, false);
			bString = critrange == 0 ? EMPTY_STRING : Integer.toString(critrange);
			if (equip.isDouble() && critrange != altcritrange)
			{
				bString +=
						"/" //$NON-NLS-1$
							+ (altcritrange == 0 ? EMPTY_STRING : Integer
								.toString(altcritrange));
			}
		}
		else
		{
			bString =
					WeaponToken.getNewCritRangeString(pc, equip, critRangeVar);
		}

		if (bString.length() > 0)
		{
			b.appendSpacer();
			b.appendI18nElement("in_ieInfoLabelTextCritRange", bString); //$NON-NLS-1$
		}

		String critMultVar =
				ControlUtilities.getControlToken(Globals.getContext(),
					"CRITMULT");
		if (critMultVar == null)
		{
			bString = EqToken.multAsString(equip.getCritMultiplier());
			if (equip.isDouble()
				&& !(equip.getCritMultiplier() == equip.getAltCritMultiplier()))
			{
				bString += "/" + EqToken.multAsString(equip.getAltCritMultiplier()); //$NON-NLS-1$
			}
		}
		else
		{
			bString =
					WeaponToken.getNewCritMultString(pc, equip, critMultVar);
		}

		if (bString.length() > 0)
		{
			b.appendSpacer();
			b.appendI18nElement("in_igInfoLabelTextCritMult", bString); //$NON-NLS-1$
		}

		if (equip.isWeapon())
		{
			bString =
					Globals.getGameModeUnitSet().displayDistanceInUnitSet(
						EqToken.getRange(pc, equip).intValue());

			if (bString.length() > 0)
			{
				b.appendSpacer();
				b.appendI18nElement("in_igInfoLabelTextRange", bString + //$NON-NLS-1$
					Globals.getGameModeUnitSet().getDistanceUnit());
			}
		}

		bString = equip.getContainerCapacityString();

		if (bString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextContainer", bString); //$NON-NLS-1$
		}

		bString = equip.getContainerContentsString();

		if (bString.length() > 0)
		{
			b.appendSpacer();
			b.appendI18nElement("in_igInfoLabelTextCurrentlyContains", bString); //$NON-NLS-1$
		}

		final int charges = equip.getRemainingCharges();

		if (charges >= 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextCharges", Integer.valueOf( //$NON-NLS-1$
				charges).toString());
		}

		Map<String, String> qualityMap = equip.getMapFor(MapKey.QUALITY);
		if (qualityMap != null && !qualityMap.isEmpty())
		{
			Set<String> qualities = new TreeSet<String>();
			for (Map.Entry<String, String> me : qualityMap.entrySet())
			{
				qualities.add(new StringBuilder(50).append(me.getKey()).append(
					": ").append(me.getValue()).toString());
			}

			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextQualities", StringUtil.join( //$NON-NLS-1$
				qualities, ", ")); //$NON-NLS-2$
		}
		//Description
		String desc = pc.getDescription(equip);
		if (desc.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
					DescriptionFormatting.piWrapDesc(equip, desc, false));
		}
		String IDS = equip.getInterestingDisplayString(pc);
		if (IDS.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextProp", IDS); //$NON-NLS-1$
		}

		String note = equip.getNote();
		if (note.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextNote", note); //$NON-NLS-1$
		}

		return b;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHTMLInfo(EquipModFacade equipModFacade, EquipmentFacade equipFacade)
	{
		if (equipModFacade == null
			|| !(equipModFacade instanceof EquipmentModifier)
			|| equipFacade == null || !(equipFacade instanceof Equipment))
		{
			return EMPTY_STRING;
		}
		
		EquipmentModifier equipMod = (EquipmentModifier) equipModFacade;
		Equipment equip = (Equipment) equipFacade;

		final StringBuilder title = new StringBuilder(50);
		title.append(OutputNameFormatting.piString(equipMod, false));

		final HtmlInfoBuilder b = new HtmlInfoBuilder(null, false);
		b.appendTitleElement(title.toString());
		b.appendLineBreak();

		b.appendI18nElement("in_igInfoLabelTextType", //$NON-NLS-1$
			StringUtil.join(equipMod.getTrueTypeList(true), ". "));

		// Various cost types
		int iPlus = equipMod.getSafe(IntegerKey.PLUS);
		if (iPlus != 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextPlus", String.valueOf(iPlus));
		}
		Formula baseCost = equipMod.getSafe(FormulaKey.BASECOST);
		if (!"0".equals(baseCost.toString()))
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextPrecost", String.valueOf(baseCost));
		}
		Formula cost = equipMod.getSafe(FormulaKey.COST);
		if (!"0".equals(cost.toString()))
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igEqModelColCost", String.valueOf(cost));
		}
		
		//Description
		String desc = pc.getDescription(equipMod);
		if (desc.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
					DescriptionFormatting.piWrapDesc(equipMod, desc, false));
		}
		
		// Special properties
		StringBuilder sb = new StringBuilder(100);
		boolean first = true;
		for (SpecialProperty sp : equipMod.getSafeListFor(ListKey.SPECIAL_PROPERTIES))
		{
			if (!first)
			{
				sb.append(", ");
			}
			first = false;
			sb.append(sp.getDisplayName());
		}
		if (sb.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextSprop", sb.toString());
		}
		
		final String cString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, equip, equipMod
					.getPrerequisiteList(), false);
		if (cString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextReq", cString); //$NON-NLS-1$
		}

		String bString = equipMod.getSource();
		if (bString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_igInfoLabelTextSource", bString); //$NON-NLS-1$
		}
		b.appendLineBreak();

		return b.toString();
	}
	

	/**
	 * @param equipMod
	 * @return Object
	 */
	protected String getCostValue(EquipmentModifier equipMod)
	{
		int iPlus = equipMod.getSafe(IntegerKey.PLUS);
		StringBuilder eCost = new StringBuilder(20);

		if (iPlus != 0)
		{
			eCost.append("Plus:").append(iPlus);
		}

		Formula baseCost = equipMod.getSafe(FormulaKey.BASECOST);

		if (!"0".equals(baseCost.toString()))
		{
			if (eCost.length() != 0)
			{
				eCost.append(", ");
			}

			eCost.append("Precost:").append(baseCost);
		}

		Formula cost = equipMod.getSafe(FormulaKey.BASECOST);

		if (!"0".equals(cost.toString()))
		{
			if (eCost.length() != 0)
			{
				eCost.append(", ");
			}

			eCost.append("Cost:").append(cost);
		}

		String sRet = eCost.toString();
		return sRet;
	}
	
	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getHTMLInfo(pcgen.core.facade.TemplateFacade)
	 */
	@Override
	public String getHTMLInfo(TemplateFacade templateFacade)
	{
		if (templateFacade == null)
		{
			return EMPTY_STRING;
		}

		PCTemplate template = (PCTemplate) templateFacade;

		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();

		infoText.appendTitleElement(OutputNameFormatting.piString(template,
			false));

		appendFacts(infoText, template);

		RaceType rt = template.get(ObjectKey.RACETYPE);
		if (rt != null)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_irInfoRaceType", rt.toString()); //$NON-NLS-1$
		}

		if (template.getType().length() > 0)
		{
			infoText.appendSpacer();
			infoText.appendI18nElement("in_irInfoType", template.getType()); //$NON-NLS-1$
		}

		String aString = pc.getDescription(template);
		if (aString.length() != 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
				aString);
		}

		aString = TemplateModifier.modifierString(template, pc);
		if (aString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_modifier", aString); //$NON-NLS-1$
		}

		aString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
					template.getPrerequisiteList(), false);
		if (aString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_requirements", aString); //$NON-NLS-1$
		}

		aString = template.getSource();
		if (aString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_sourceLabel", aString); //$NON-NLS-1$
		}

		return infoText.toString();
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHTMLInfo(KitFacade kitFacade)
	{
		if (kitFacade == null)
		{
			return EMPTY_STRING;
		}

		Kit kit = (Kit) kitFacade;

		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();

		infoText.appendTitleElement(OutputNameFormatting.piString(kit, false));

		appendFacts(infoText, kit);

		String aString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
					kit.getPrerequisiteList(), false);
		if (aString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_requirements", aString); //$NON-NLS-1$
		}

		List<BaseKit> sortedObjects = new ArrayList<BaseKit>();
		sortedObjects.addAll(kit.getSafeListFor(ListKey.KIT_TASKS));
		Collections.sort(sortedObjects, new ObjectTypeComparator());

		String lastObjectName = EMPTY_STRING;
		for (BaseKit bk : sortedObjects)
		{
			String objName = bk.getObjectName();
			if (!objName.equals(lastObjectName))
			{
				if (!EMPTY_STRING.equals(lastObjectName))
				{
					infoText.append("; ");
				}
				else
				{
					infoText.appendLineBreak();
				}
				infoText.append("  <b>" + objName + "</b>: ");
				lastObjectName = objName;
			}
			else
			{
				infoText.append(", ");
			}
			infoText.append(bk.toString());
		}

		BigDecimal totalCost = kit.getTotalCost(pc);
		if (totalCost != null)
		{
			infoText.appendLineBreak();
			infoText.appendI18nFormattedElement("in_kitInfo_TotalCost", //$NON-NLS-1$
					COST_FMT.format(totalCost),
					SettingsHandler.getGame().getCurrencyDisplay());
		}

		String desc = pc.getDescription(kit);
		if (desc.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nFormattedElement("in_InfoDescription", //$NON-NLS-1$
					DescriptionFormatting.piWrapDesc(kit, desc, false));
		}

		aString = kit.getSource();
		if (aString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_sourceLabel", aString); //$NON-NLS-1$
		}
		//TODO ListKey.KIT_TASKS
		return infoText.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHTMLInfo(TempBonusFacade tempBonusFacade)
	{
		if (tempBonusFacade == null)
		{
			return EMPTY_STRING;
		}

		if (!(tempBonusFacade instanceof TempBonusFacadeImpl))
		{
			final HtmlInfoBuilder infoText = new HtmlInfoBuilder();
			infoText.appendTitleElement(tempBonusFacade.toString());
			return infoText.toString();
		}

		TempBonusFacadeImpl tempBonus = (TempBonusFacadeImpl) tempBonusFacade;
		CDOMObject originObj = tempBonus.getOriginObj();

		final HtmlInfoBuilder infoText;
		if (originObj instanceof Equipment)
		{
			infoText = getEquipmentHtmlInfo((Equipment) originObj);
		}
		else
		{
			infoText = new HtmlInfoBuilder();
			infoText.appendTitleElement(OutputNameFormatting.piString(originObj, false));
			infoText.append(" (").append(tempBonus.getOriginType()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (tempBonus.getTarget() != null)
		{
			String targetName = charDisplay.getName();
			if (tempBonus.getTarget() instanceof CDOMObject)
			{
				targetName = ((CDOMObject) tempBonus.getTarget()).getKeyName();
			}

			infoText.appendLineBreak();
			infoText.appendI18nElement("in_itmInfoLabelTextTarget", targetName); //$NON-NLS-1$

			StringBuilder bonusValues = new StringBuilder(100);
			Map<BonusObj, TempBonusInfo> bonusMap = pc.getTempBonusMap(originObj.getKeyName(), targetName);
			boolean first = true;
			List<BonusObj> bonusList = new ArrayList<BonusObj>(bonusMap.keySet());
			Collections.sort(bonusList, new BonusComparator());
			for (BonusObj bonusObj : bonusList)
			{
				if (!first)
				{
					bonusValues.append(", "); //$NON-NLS-1$
				}
				first = false;
				String adj = ADJ_FMT.format(bonusObj.resolve(pc, "")); //$NON-NLS-1$
				bonusValues.append(adj + " " + bonusObj.getDescription());  //$NON-NLS-1$
			}
			if (bonusValues.length() > 0)
			{
				infoText.appendLineBreak();
				infoText.appendI18nElement(
					"in_itmInfoLabelTextEffect", //$NON-NLS-1$
					bonusValues.toString());
			}
		}

		if (originObj instanceof Spell)
		{
			Spell aSpell = (Spell) originObj; 
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_spellDuration", //$NON-NLS-1$
				aSpell.getListAsString(ListKey.DURATION));
			infoText.appendSpacer();
			infoText.appendI18nElement("in_spellRange", //$NON-NLS-1$
				aSpell.getListAsString(ListKey.RANGE));
			infoText.appendSpacer();
			infoText.appendI18nElement("in_spellTarget", //$NON-NLS-1$
				aSpell.getSafe(StringKey.TARGET_AREA));
		}

		String aString = originObj.getSafe(StringKey.TEMP_DESCRIPTION);
		if (StringUtils.isEmpty(aString) && originObj instanceof Spell)
		{
			Spell sp = (Spell) originObj;
			aString = DescriptionFormatting.piWrapDesc(sp, pc.getDescription(sp),
						false);
		}
		else if (StringUtils.isEmpty(aString) && originObj instanceof Ability)
		{
			Ability ab = (Ability) originObj;
			List<CNAbility> wrappedAbility =
					Collections.singletonList(CNAbilityFactory.getCNAbility(ab
						.getCDOMCategory(), Nature.NORMAL, ab));
			aString = DescriptionFormatting.piWrapDesc(ab,
						pc.getDescription(wrappedAbility), false);
		}
		if (aString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_itmInfoLabelTextDesc", aString); //$NON-NLS-1$
		}
		
		aString =
				PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
					originObj.getPrerequisiteList(), false);
		if (aString.length() > 0)
		{
			infoText.appendLineBreak();
			infoText.appendI18nElement("in_requirements", aString); //$NON-NLS-1$
		}

		infoText.appendLineBreak();
		infoText.appendI18nElement(
			"in_itmInfoLabelTextSource", //$NON-NLS-1$
			SourceFormat.getFormattedString(originObj,
				Globals.getSourceDisplay(), true));

		return infoText.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHTMLInfo(InfoFacade facade)
	{
		if (facade == null)
		{
			return EMPTY_STRING;
		}
		
		// Use a more detailed info if we can
		if (facade instanceof AbilityFacade)
		{
			return getHTMLInfo((AbilityFacade) facade);
		}
		if (facade instanceof ClassFacade)
		{
			return getHTMLInfo((ClassFacade) facade, null);
		}
		if (facade instanceof DeityFacade)
		{
			return getHTMLInfo((DeityFacade) facade);
		}
		if (facade instanceof DomainFacade)
		{
			return getHTMLInfo((DomainFacade) facade);
		}
		if (facade instanceof EquipmentFacade)
		{
			return getHTMLInfo((EquipmentFacade) facade);
		}
		if (facade instanceof KitFacade)
		{
			return getHTMLInfo((KitFacade) facade);
		}
		if (facade instanceof RaceFacade)
		{
			return getHTMLInfo((RaceFacade) facade);
		}
		if (facade instanceof SkillFacade)
		{
			return getHTMLInfo((SkillFacade) facade);
		}
		if (facade instanceof SpellFacade)
		{
			return getHTMLInfo((SpellFacade) facade);
		}
		if (facade instanceof TempBonusFacade)
		{
			return getHTMLInfo((TempBonusFacade) facade);
		}
		if (facade instanceof TemplateFacade)
		{
			return getHTMLInfo((TemplateFacade) facade);
		}

		final HtmlInfoBuilder infoText = new HtmlInfoBuilder();
		infoText.appendTitleElement(facade.toString());
		infoText.appendLineBreak();

		if (facade.getType().length() > 0)
		{
			infoText.appendI18nElement("in_irInfoType", facade.getType()); //$NON-NLS-1$
			infoText.appendLineBreak();
		}
		
		infoText.appendI18nElement(
			"in_itmInfoLabelTextSource", //$NON-NLS-1$
			facade.getSource());
		
		return infoText.toString();
	}

	private static class ObjectTypeComparator implements Comparator<BaseKit>
	{
		@Override
		public int compare(BaseKit bk1, BaseKit bk2)
		{
			String name1 = bk1.getObjectName();
			String name2 = bk2.getObjectName();
			return name1.compareTo(name2);
		}
	}

	private static class BonusComparator implements Comparator<BonusObj>
	{
		@Override
		public int compare(BonusObj bo1, BonusObj bo2)
		{
			String type1 = bo1.getTypeOfBonus();
			String type2 = bo2.getTypeOfBonus();
			if (!type1.equals(type2))
			{
				return type1.compareTo(type2);
			}
			return bo1.getBonusInfo().compareTo(bo2.getBonusInfo());
		}
	}
	
	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getLevelAdjustment(pcgen.core.facade.RaceFacade)
	 */
	@Override
	public String getLevelAdjustment(RaceFacade raceFacade)
	{
		if (!(raceFacade instanceof Race))
		{
			return EMPTY_STRING;
		}
		Race race = (Race) raceFacade;
		return ADJ_FMT.format(race.getSafe(FormulaKey.LEVEL_ADJUSTMENT)
			.resolve(pc, EMPTY_STRING));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumMonsterClassLevels(RaceFacade raceFacade)
	{
		if (!(raceFacade instanceof Race))
		{
			return 0;
		}
		Race race = (Race) raceFacade;
		LevelCommandFactory levelCommandFactory =
				race.get(ObjectKey.MONSTER_CLASS);
		if (levelCommandFactory == null)
		{
			return 0;
		}
		return levelCommandFactory.getLevelCount().resolve(pc, EMPTY_STRING)
			.intValue();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getPreReqHTML(pcgen.core.facade.RaceFacade)
	 */
	@Override
	public String getPreReqHTML(RaceFacade race)
	{
		if (!(race instanceof Race))
		{
			return EMPTY_STRING;
		}
		return PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
			((Race) race).getPrerequisiteList(), true);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getStatAdjustments(pcgen.core.facade.RaceFacade)
	 */
	@Override
	public String getStatAdjustments(RaceFacade raceFacade)
	{
		if (!(raceFacade instanceof Race))
		{
			return EMPTY_STRING;
		}
		Race race = (Race) raceFacade;
		final StringBuilder retString = new StringBuilder(100);

		for (PCStat stat : charDisplay.getStatSet())
		{
			if (charDisplay.isNonAbility(stat))
			{
				if (retString.length() > 0)
				{
					retString.append(' ');
				}

				retString.append(stat.getKeyName() + ":Nonability");
			}
			else
			{
				if (BonusCalc.getStatMod(race, stat, pc) != 0)
				{
					if (retString.length() > 0)
					{
						retString.append(' ');
					}

					retString.append(stat.getKeyName() + ":"
						+ BonusCalc.getStatMod(race, stat, pc));
				}
			}
		}

		return retString.toString();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getVision(pcgen.core.facade.RaceFacade)
	 */
	@Override
	public String getVision(RaceFacade race)
	{
		if (!(race instanceof Race))
		{
			return EMPTY_STRING;
		}
		return VisionDisplay.getVision(pc, (Race) race);
	}

	@Override
	public float getCost(EquipmentFacade equipment)
	{
		if (equipment instanceof Equipment)
		{
			return ((Equipment) equipment).getCost(pc).floatValue();
		}
		return 0;
	}

	@Override
	public float getWeight(EquipmentFacade equipment)
	{
		if (equipment instanceof Equipment)
		{
			Float weight = ((Equipment) equipment).getWeight(pc);
			return (float) Globals.getGameModeUnitSet().convertWeightToUnitSet(weight);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getLevelAdjustment(pcgen.core.facade.TemplateFacade)
	 */
	@Override
	public String getLevelAdjustment(TemplateFacade templateFacade)
	{
		if (!(templateFacade instanceof PCTemplate))
		{
			return EMPTY_STRING;
		}
		PCTemplate template = (PCTemplate) templateFacade;
		return ADJ_FMT.format(template.getSafe(FormulaKey.LEVEL_ADJUSTMENT)
			.resolve(pc, EMPTY_STRING));
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getModifier(pcgen.core.facade.TemplateFacade)
	 */
	@Override
	public String getModifier(TemplateFacade templateFacade)
	{
		if (!(templateFacade instanceof PCTemplate))
		{
			return EMPTY_STRING;
		}
		PCTemplate template = (PCTemplate) templateFacade;
		return TemplateModifier.modifierString(template, pc);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.InfoFactory#getPreReqHTML(pcgen.core.facade.TemplateFacade)
	 */
	@Override
	public String getPreReqHTML(TemplateFacade template)
	{
		if (!(template instanceof PCTemplate))
		{
			return EMPTY_STRING;
		}
		return PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
			((PCTemplate) template).getPrerequisiteList(), true);
	}

	@Override
	public String getHTMLInfo(SpellFacade spell)
	{
		if (spell == null || !(spell instanceof SpellFacadeImplem))
		{
			return EMPTY_STRING;
		}

		SpellFacadeImplem sfi = (SpellFacadeImplem) spell;
		CharacterSpell cs = sfi.getCharSpell();
		SpellInfo si = sfi.getSpellInfo();
		Spell aSpell = cs.getSpell();

		if (aSpell == null)
		{
			return EMPTY_STRING;
		}
		final HtmlInfoBuilder b =
				new HtmlInfoBuilder(OutputNameFormatting.piString(aSpell, false));

		if (si != null)
		{
			final String addString = si.toString(); // would add [featList]
			if (addString.length() > 0)
			{
				b.append(" ").append(addString); //$NON-NLS-1$
			}
			b.appendLineBreak();
			b.appendI18nElement("InfoSpells.level.title", Integer.toString(si.getOriginalLevel())); //$NON-NLS-1$
		}
		b.appendLineBreak();
		
		String classlevels = aSpell.getListAsString(ListKey.SPELL_CLASSLEVEL);
		if (StringUtils.isNotEmpty(classlevels))
		{
			b.appendI18nElement("in_clClass", classlevels);
			b.appendLineBreak();
		}
		String domainlevels = aSpell.getListAsString(ListKey.SPELL_DOMAINLEVEL);
		if (StringUtils.isNotEmpty(domainlevels))
		{
			b.appendI18nElement("in_domains", domainlevels);
			b.appendLineBreak();
		}
		
		b.appendI18nElement("in_spellSchool",
			aSpell.getListAsString(ListKey.SPELL_SCHOOL));

		String subSchool = aSpell.getListAsString(ListKey.SPELL_SUBSCHOOL);
		if (StringUtils.isNotEmpty(subSchool))
		{
			b.append(" (").append(subSchool).append(")");
		}
		String spellDescriptor =
				aSpell.getListAsString(ListKey.SPELL_DESCRIPTOR);
		if (StringUtils.isNotEmpty(spellDescriptor))
		{
			b.append(" [").append(spellDescriptor).append("]");
		}
		b.appendLineBreak();

		appendFacts(b, aSpell);
		b.appendLineBreak();

		b.appendI18nElement("in_spellComponents",
			aSpell.getListAsString(ListKey.COMPONENTS));
		b.appendLineBreak();

		b.appendI18nElement("in_spellCastTime",
			aSpell.getListAsString(ListKey.CASTTIME));
		b.appendLineBreak();

		b.appendI18nElement("in_spellDuration",
			pc.parseSpellString(cs, aSpell.getListAsString(ListKey.DURATION)));
		b.appendLineBreak();

		b.appendI18nElement("in_spellRange", pc.getSpellRange(cs, si));
		b.appendSpacer();
		b.appendI18nElement("in_spellTarget",
			pc.parseSpellString(cs, aSpell.getSafe(StringKey.TARGET_AREA)));
		b.appendLineBreak();

		b.appendI18nElement("in_spellSavingThrow",
			aSpell.getListAsString(ListKey.SAVE_INFO));
		b.appendSpacer();
		b.appendI18nElement("in_spellSpellResist",
			aSpell.getListAsString(ListKey.SPELL_RESISTANCE));
		b.appendLineBreak();
		
		b.appendLineBreak();
		b.appendI18nElement("in_descrip", pc.parseSpellString(cs,  //$NON-NLS-1$
			pc.getDescription(aSpell)));

		final String cString = PrerequisiteUtilities.preReqHTMLStringsForList(pc, null,
		aSpell.getPrerequisiteList(), false);
		if (cString.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_requirements", cString); //$NON-NLS-1$
		}
		b.appendLineBreak();

		String spellSource = SourceFormat.getFormattedString(aSpell,
		Globals.getSourceDisplay(), true);
		if (spellSource.length() > 0)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_source", spellSource); //$NON-NLS-1$
		}

		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSpellBookInfo(String name)
	{
		SpellBook book = charDisplay.getSpellBookByName(name);
		if (book == null)
		{
			return EMPTY_STRING;
		}
		
		switch (book.getType())
		{
			case SpellBook.TYPE_PREPARED_LIST:
				return produceSpellListInfo(book);

			case SpellBook.TYPE_SPELL_BOOK:
				return produceSpellBookInfo(book);
				
			default:
				return EMPTY_STRING;
		}
	}

	/**
	 * Produce the HTML info label for a prepared spell list.
	 * @param book The spell list being output.
	 * @return The HTML info for the list.
	 */
	private String produceSpellListInfo(SpellBook spelllist)
	{
		final HtmlInfoBuilder b =
				new HtmlInfoBuilder(spelllist.getName());

		b.append(" ("); //$NON-NLS-1$
		b.append(spelllist.getTypeName());
		b.append(")"); //$NON-NLS-1$
		b.appendLineBreak();

		if (spelllist.getDescription() != null)
		{
			b.appendI18nElement("in_descrip", spelllist.getDescription()); //$NON-NLS-1$
			b.appendLineBreak();
		}
		
		// Look at each spell on each spellcasting class
		for (PCClass pcClass : charDisplay.getClassSet())
		{
			Map<Integer, Integer> spellCountMap = new TreeMap<Integer, Integer>();
			int highestSpellLevel = -1;
			Collection<? extends CharacterSpell> sp = charDisplay.getCharacterSpells(pcClass);
			List<CharacterSpell> classSpells = new ArrayList<CharacterSpell>(sp);
			// Add in the spells granted by objects
			pc.addBonusKnownSpellsToList(pcClass, classSpells);
			for (CharacterSpell charSpell : classSpells)
			{
				for (SpellInfo spellInfo : charSpell.getInfoList())
				{
					if (!spelllist.getName().equals(spellInfo.getBook()))
					{
						continue;
					}
					int level = spellInfo.getActualLevel();
					
					int count = spellCountMap.containsKey(level) ? spellCountMap.get(level) : 0;
					count += spellInfo.getTimes();
					spellCountMap.put(level, count);
					if (level > highestSpellLevel)
					{
						highestSpellLevel = level;
					}
				}
			}

			if (!spellCountMap.isEmpty())
			{
				b.append("<table border=1><tr><td><font size=-1><b>"); //$NON-NLS-1$
				b.append(OutputNameFormatting.piString(pcClass, false));
				b.append("</b></font></td>"); //$NON-NLS-1$

				for (int i = 0; i <= highestSpellLevel; ++i)
				{
					b.append("<td><font size=-2><b><center>&nbsp;"); //$NON-NLS-1$
					b.append(String.valueOf(i));
					b.append("&nbsp;</b></center></font></td>"); //$NON-NLS-1$
				}

				b.append("</tr>"); //$NON-NLS-1$
				b.append("<tr><td><font size=-1><b>Prepared</b></font></td>"); //$NON-NLS-1$

				for (int i = 0; i <= highestSpellLevel; ++i)
				{
					b.append("<td><font size=-1><center>"); //$NON-NLS-1$
					b.append(String.valueOf(spellCountMap.get(i) == null ? 0
						: spellCountMap.get(i)));
					b.append("</center></font></td>"); //$NON-NLS-1$
				}
				b.append("</tr></table>"); //$NON-NLS-1$
				b.appendLineBreak();
			}
			
		}
		
		return b.toString();
	}

	/**
	 * Produce the HTML info label for a spell book.
	 * @param book The spell book being output.
	 * @return The HTML info for the book.
	 */
	private String produceSpellBookInfo(SpellBook book)
	{
		final HtmlInfoBuilder b =
				new HtmlInfoBuilder(book.getName());

		b.append(" ("); //$NON-NLS-1$
		b.append(book.getTypeName());
		if (book.getName().equals(charDisplay.getSpellBookNameToAutoAddKnown()))
		{
			b.append(TWO_SPACES).append(BOLD);
			b.append(
				LanguageBundle.getString("InfoSpellsSubTab.DefaultKnownBook")) //$NON-NLS-1$
				.append(END_BOLD);
		}
		b.append(")"); //$NON-NLS-1$
		b.appendLineBreak();
		
		b.append(LanguageBundle.getFormattedString(
			"InfoSpells.html.spellbook.details", //$NON-NLS-1$
			new Object[]{
				book.getNumPages(),
				book.getNumPagesUsed(),
				book.getPageFormula(),
				book.getNumSpells()}));

		if (book.getDescription() != null)
		{
			b.appendLineBreak();
			b.appendI18nElement("in_descrip", book.getDescription()); //$NON-NLS-1$
		}
		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(AbilityFacade ability)
	{
		if (ability == null || !(ability instanceof Ability))
		{
			return EMPTY_STRING;
		}

		try
		{
			Ability a = (Ability) ability;
			List<CNAbility> wrappedAbility = getWrappedAbility(a);
			return DescriptionFormatting.piWrapDesc(a, pc.getDescription(wrappedAbility), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + ability, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(RaceFacade raceFacade)
	{
		if (raceFacade == null || !(raceFacade instanceof Race))
		{
			return EMPTY_STRING;
		}
		try
		{
			Race race = (Race) raceFacade;
			return DescriptionFormatting.piWrapDesc(race, pc.getDescription(race), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + raceFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(TemplateFacade templateFacade)
	{
		if(templateFacade == null || !(templateFacade instanceof PCTemplate)){
			return EMPTY_STRING;
		}
		try
		{
			PCTemplate template = (PCTemplate) templateFacade;
			return DescriptionFormatting.piWrapDesc(template, pc.getDescription(template), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + templateFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(ClassFacade classFacade)
	{
		if(classFacade == null || !(classFacade instanceof PCClass)){
			return EMPTY_STRING;
		}
		try
		{
			PCClass pcClass = (PCClass) classFacade;
			return DescriptionFormatting.piWrapDesc(pcClass, pc.getDescription(pcClass), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + classFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(SkillFacade skillFacade)
	{
		if (skillFacade == null || !(skillFacade instanceof Skill))
		{
			return EMPTY_STRING;
		}
		try
		{
			Skill skill = (Skill) skillFacade;
			return DescriptionFormatting.piWrapDesc(skill, pc.getDescription(skill), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + skillFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(EquipmentFacade equipFacade)
	{
		if (equipFacade == null || !(equipFacade instanceof Equipment))
		{
			return EMPTY_STRING;
		}
		try
		{
			Equipment equip = (Equipment) equipFacade;
			return DescriptionFormatting.piWrapDesc(equip, pc.getDescription(equip), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + equipFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(KitFacade kitFacade)
	{
		if (kitFacade == null || !(kitFacade instanceof Kit))
		{
			return EMPTY_STRING;
		}
		try
		{
			Kit kit = (Kit) kitFacade;
			return DescriptionFormatting.piWrapDesc(kit, pc.getDescription(kit), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + kitFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(DeityFacade deityFacade)
	{
		if(deityFacade == null || !(deityFacade instanceof Deity)){
			return EMPTY_STRING;
		}
		try
		{
			Deity deity = (Deity) deityFacade;
			return DescriptionFormatting.piWrapDesc(deity, pc.getDescription(deity), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + deityFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(DomainFacade domainFacade)
	{
		if(domainFacade == null || !(domainFacade instanceof DomainFacadeImpl)){
			return EMPTY_STRING;
		}
		try
		{
			DomainFacadeImpl domain = (DomainFacadeImpl) domainFacade;
			Domain dom = domain.getRawObject();
			if(dom == null){
				return EMPTY_STRING;
			}
			return DescriptionFormatting.piWrapDesc(dom, pc.getDescription(dom), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + domainFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(SpellFacade spellFacade)
	{
		if (spellFacade == null || !(spellFacade instanceof SpellFacadeImplem))
		{
			return EMPTY_STRING;
		}
		try
		{
			SpellFacadeImplem spell = (SpellFacadeImplem) spellFacade;
			Spell aSpell = spell.getSpell();
			if (aSpell == null)
			{
				return EMPTY_STRING;
			}
			return DescriptionFormatting.piWrapDesc(aSpell, pc.getDescription(aSpell), false);
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + spellFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription(TempBonusFacade tempBonusFacade)
	{
		if (tempBonusFacade == null || !(tempBonusFacade instanceof TempBonusFacadeImpl))
		{
			return EMPTY_STRING;
		}
		try
		{
			TempBonusFacadeImpl tempBonus = (TempBonusFacadeImpl) tempBonusFacade;
			CDOMObject originObj = tempBonus.getOriginObj();
			String desc = originObj.getSafe(StringKey.TEMP_DESCRIPTION);
			if (StringUtils.isEmpty(desc))
			{
				if (originObj instanceof Spell)
				{
					Spell sp = (Spell) originObj;
					desc = DescriptionFormatting.piWrapDesc(sp, pc.getDescription(sp),
							false);
				}
				else if (originObj instanceof Ability)
				{
					Ability ab = (Ability) originObj;
					List<CNAbility> wrappedAbility
							= Collections.singletonList(CNAbilityFactory.getCNAbility(ab
									.getCDOMCategory(), Nature.NORMAL, ab));
					desc = DescriptionFormatting.piWrapDesc(ab,
							pc.getDescription(wrappedAbility), false);
				}
			}
			return desc;
		}
		catch (Exception e)
		{
			Logging.errorPrint("Failed to get description for " + tempBonusFacade, e); //$NON-NLS-1$
			return EMPTY_STRING;
		}
	}

	/**
	 * Retrieve a wrapped instance of the provided ability, whether the 
	 * character has the ability or not. This will either be a list of the 
	 * specific occurrences of the ability the character, or a list of a new 
	 * CNAbility if the character does not possess the ability.
	 *   
	 * @param a The ability to be wrapped.
	 * @return The list of wrapped abilities.
	 */
	private List<CNAbility> getWrappedAbility(Ability a)
	{
		/*
		 * TODO this is probably a problem in that it is doing a target (all
		 * associations for an ability, not related to the current CNAbility
		 * selected in the UI)
		 */
		List<CNAbility> wrappedAbility = pc.getMatchingCNAbilities(a);
		if (wrappedAbility.isEmpty())
		{
			CNAbility cna = CNAbilityFactory.getCNAbility(a.getCDOMCategory(), Nature.NORMAL, a);
			wrappedAbility.add(cna);
		}
		return wrappedAbility;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDomains(DeityFacade deityFacade)
	{
		if (deityFacade == null || !(deityFacade instanceof Deity))
		{
			return EMPTY_STRING;
		}
		Deity deity = (Deity) deityFacade;
		Set<String> set = new TreeSet<String>();
		for (CDOMReference<Domain> ref : deity.getSafeListMods(Deity.DOMAINLIST))
		{
			for (Domain d : ref.getContainedObjects())
			{
				set.add(OutputNameFormatting.piString(d, false));
			}
		}
		final StringBuilder piString = new StringBuilder(100);
		//piString.append("<html>"); //$NON-NLS-1$
		piString.append(StringUtil.joinToStringBuilder(set, ", ")); //$NON-NLS-1$
		//piString.append("</html>"); //$NON-NLS-1$
		return piString.toString();
		
	}
	
	@Override
	public String getPantheons(DeityFacade deityFacade)
	{
		if (deityFacade == null || !(deityFacade instanceof Deity))
		{
			return EMPTY_STRING;
		}
		Deity deity = (Deity) deityFacade;
		Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if (deity != null)
		{
			FactSetKey<String> fk = FactSetKey.valueOf("Pantheon");
			for (Indirect<String> indirect : deity.getSafeSetFor(fk))
			{
				set.add(indirect.get());
			}
		}
		final StringBuilder piString = new StringBuilder(100);
		piString.append(StringUtil.joinToStringBuilder(set, ",")); //$NON-NLS-1$
		return piString.toString();
	
	}

	/**
	 * Get a display string of the deity's favored weapons.
	 * @param deityFacade The deity to be output.
	 * @return The comma separated list of weapons.
	 */
	@Override
	public String getFavoredWeapons(DeityFacade deityFacade)
	{
		if (deityFacade == null || !(deityFacade instanceof Deity))
		{
			return EMPTY_STRING;
		}
		Deity deity = (Deity) deityFacade;
		List<CDOMReference<WeaponProf>> wpnList =
				deity.getSafeListFor(ListKey.DEITYWEAPON);
		return ReferenceUtilities.joinLstFormat(wpnList, ",");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getChoices(AbilityFacade abilityFacade)
	{
		if (abilityFacade == null || !(abilityFacade instanceof Ability))
		{
			return EMPTY_STRING;
		}
		final Ability ability = (Ability) abilityFacade;
		final StringBuilder result = new StringBuilder(100);

		Collection<CNAbility> targetAbilities = pc.getMatchingCNAbilities(ability);
		if (ability.getSafe(ObjectKey.MULTIPLE_ALLOWED))
		{
			ChooseInformation<?> chooseInfo =
					ability.get(ObjectKey.CHOOSE_INFO);
			processAbilities(result, targetAbilities, chooseInfo);
		}
		return result.toString();
	}

	private <T> void processAbilities(final StringBuilder result,
		Collection<CNAbility> targetAbilities, ChooseInformation<T> chooseInfo)
	{
		if (chooseInfo == null)
		{
			return;
		}

		List<T> choices = new ArrayList<T>();
		for (CNAbility ab : targetAbilities)
		{
			List<? extends T> sel =
					(List<? extends T>) pc.getDetailedAssociations(ab);
			if (sel != null)
			{
				choices.addAll(sel);
			}
		}

		String choiceInfo = chooseInfo.composeDisplay(choices).toString();
		if (choiceInfo.length() > 0)
		{
			result.append(choiceInfo);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTempBonusTarget(TempBonusFacade tempBonusFacade)
	{
		if (tempBonusFacade == null || !(tempBonusFacade instanceof TempBonusFacadeImpl))
		{
			return EMPTY_STRING;
		}
		
		TempBonusFacadeImpl tempBonus = (TempBonusFacadeImpl) tempBonusFacade;

		Set<String> targetSet = new HashSet<String>();
		if (TempBonusHelper.hasCharacterTempBonus(tempBonus.getOriginObj(), pc))
		{
			targetSet.add(LanguageBundle
				.getString("in_itmBonModelTargetTypeCharacter")); //$NON-NLS-1$
		}
		if (TempBonusHelper.hasEquipmentTempBonus(tempBonus.getOriginObj(), pc))
		{
			targetSet.addAll(TempBonusHelper.getEquipmentApplyString(tempBonus.getOriginObj(), pc));
		}
		StringBuilder target = new StringBuilder(40);
		for (String string : targetSet)
		{
			target.append(string).append(";"); //$NON-NLS-1$
		}
		if (target.length() > 0)
		{
			target.deleteCharAt(target.length()-1);
		}
		return target.toString();
	}

	@Override
	public String getMovement(RaceFacade race)
	{
		if (!(race instanceof Race))
		{
			return EMPTY_STRING;
		}
		List<Movement> movements = ((Race) race).getListFor(ListKey.BASE_MOVEMENT);
		if (movements != null && !movements.isEmpty())
		{
			return movements.get(0).toString();
		}
		return EMPTY_STRING;
	}

	private void appendFacts(HtmlInfoBuilder infoText, CDOMObject cdo)
	{
		Class<? extends CDOMObject> cl = cdo.getClass();
		LoadContext context = Globals.getContext();
		Collection<FactDefinition> defs =
				context.getReferenceContext().getConstructedCDOMObjects(
					FactDefinition.class);
		for (FactDefinition<?, ?> def : defs)
		{
			if (def.getUsableLocation().isAssignableFrom(cl))
			{
				if (def.getVisibility().isVisibleTo(View.VISIBLE_DISPLAY))
				{
					FactKey<?> fk = def.getFactKey();
					Indirect<?> fact = cdo.get(fk);
					if (fact != null)
					{
						infoText.appendSpacer();
						infoText.append("<b>");
						infoText.append(fk.toString());
						infoText.append(":</b>&nbsp;");
						infoText.append(fact.getUnconverted());
					}
				}
			}
		}
		Collection<FactSetDefinition> setdefs =
				context.getReferenceContext().getConstructedCDOMObjects(
					FactSetDefinition.class);
		for (FactSetDefinition<?, ?> def : setdefs)
		{
			if (def.getUsableLocation().isAssignableFrom(cl))
			{
				if (def.getVisibility().isVisibleTo(View.VISIBLE_DISPLAY))
				{
					FactSetKey<?> fk = def.getFactSetKey();
					String s = getSetString(cdo, fk);
					if (s != null)
					{
						infoText.appendSpacer();
						infoText.append("<b>");
						infoText.append(fk.toString());
						infoText.append(":</b>&nbsp;");
						infoText.append(s);
					}
				}
			}
		}
		
	}

	private <T> String getSetString(CDOMObject cdo, FactSetKey<T> fk)
	{
		List<Indirect<T>> set = cdo.getSetFor(fk);
		if (set == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Indirect<T> indirect : set)
		{
			if (!first)
			{
				sb.append(Constants.COMMA);
			}
			sb.append(indirect.get());
			first = false;
		}
		return sb.toString();
	}

}
