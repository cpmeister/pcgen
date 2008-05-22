package plugin.lsttokens.template;

import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.enumeration.RaceType;
import pcgen.core.PCTemplate;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.AbstractToken;
import pcgen.rules.persistence.token.CDOMPrimaryToken;

/**
 * Class deals with RACETYPE Token
 */
public class RacetypeToken extends AbstractToken implements
		CDOMPrimaryToken<PCTemplate>
{

	@Override
	public String getTokenName()
	{
		return "RACETYPE";
	}

	public boolean parse(LoadContext context, PCTemplate template, String value)
	{
		if (isEmpty(value))
		{
			return false;
		}
		context.getObjectContext().put(template, ObjectKey.RACETYPE,
				RaceType.getConstant(value));
		return true;
	}

	public String[] unparse(LoadContext context, PCTemplate pct)
	{
		RaceType raceType = context.getObjectContext().getObject(pct,
				ObjectKey.RACETYPE);
		if (raceType == null)
		{
			return null;
		}
		return new String[] { raceType.toString() };
	}

	public Class<PCTemplate> getTokenClass()
	{
		return PCTemplate.class;
	}
}
