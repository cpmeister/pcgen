/*
 * NameToken.java
 * Copyright 2006 (C) Aaron Divinsky <boomer70@yahoo.com>
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
 * Created on March 3, 2006
 *
 * Current Ver: $Revision$
 * Last Editor: $Author$
 * Last Edited: $Date$
 */

package plugin.lsttokens.kit;

import pcgen.core.kit.KitBio;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.AbstractNonEmptyToken;
import pcgen.rules.persistence.token.CDOMSecondaryParserToken;
import pcgen.rules.persistence.token.ParseResult;

/**
 * Handles the NAME tag for a Kit. Also can accept a GENDER tag on the same line
 * for historical reasons.
 */
public class NameToken extends AbstractNonEmptyToken<KitBio> implements
		CDOMSecondaryParserToken<KitBio>
{
	/**
	 * Gets the name of the tag this class will parse.
	 * 
	 * @return Name of the tag this class handles
	 */
	@Override
	public String getTokenName()
	{
		return "NAME";
	}

	public Class<KitBio> getTokenClass()
	{
		return KitBio.class;
	}

	public String getParentToken()
	{
		return "*KITTOKEN";
	}

	@Override
	protected ParseResult parseNonEmptyToken(LoadContext context, KitBio kitName,
		String value)
	{
		kitName.setCharacterName(value);
		return ParseResult.SUCCESS;
	}

	public String[] unparse(LoadContext context, KitBio kitName)
	{
		String bd = kitName.getCharacterName();
		if (bd == null)
		{
			return null;
		}
		return new String[] { bd };
	}

}
