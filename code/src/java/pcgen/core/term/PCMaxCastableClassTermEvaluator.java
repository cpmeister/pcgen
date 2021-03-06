/**
 * pcgen.core.term.PCMaxCastableClassTermEvaluator.java
 * Copyright (c) 2008 Andrew Wilson <nuance@users.sourceforge.net>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created 09-Aug-2008 23:14:10
 *
 * Current Ver: $Revision:$
 * Last Editor: $Author:$
 * Last Edited: $Date:$
 *
 */

package pcgen.core.term;

import java.util.List;

import pcgen.cdom.base.CDOMList;
import pcgen.cdom.list.ClassSpellList;
import pcgen.core.Globals;
import pcgen.core.PCClass;
import pcgen.core.PlayerCharacter;
import pcgen.core.spell.Spell;

public class PCMaxCastableClassTermEvaluator 
		extends BasePCTermEvaluator implements TermEvaluator
{

	private ClassSpellList spellList;

	public PCMaxCastableClassTermEvaluator(String originalText, String classKey)
	{
		this.originalText = originalText;
		this.spellList = Globals.getContext().getReferenceContext()
				.silentlyGetConstructedCDOMObject(ClassSpellList.class,
						classKey);
		// TODO Warning if null? or is null gate in resolve not necessary?
	}

	@Override
	public Float resolve(PlayerCharacter pc)
	{
		Float max = -1f;
		for (PCClass spClass : pc.getDisplay().getClassSet())
		{
			List<? extends CDOMList<Spell>> lists = pc.getDisplay().getSpellLists(spClass);
			if (spellList != null && lists.contains(spellList))
			{
				int cutoff = pc.getSpellSupport(spClass).getHighestLevelSpell();
				if (pc.getSpellSupport(spClass).hasCastList())
				{
					for (int i = 0; i < cutoff; i++)
					{
						if (pc.getSpellSupport(spClass).getCastForLevel(i, pc) != 0)
						{
							max = Math.max(max, i);
						}
					}
				}
				else
				{
					for (int i = 0; i < cutoff; i++)
					{
						if (pc.getSpellSupport(spClass).getKnownForLevel(i, pc) != 0)
						{
							max = Math.max(max, i);
						}
					}
				}
			}
		}
		return max;
	}

	@Override
	public boolean isSourceDependant()
	{
		return true;
	}

	public boolean isStatic()
	{
		return false;
	}
}
